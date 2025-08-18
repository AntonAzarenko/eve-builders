package com.azarenka.evebuilders.service.impl.market;

import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.MarketItemType;
import com.azarenka.evebuilders.domain.db.MarketOffer;
import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.dto.market.MarketRowDTO;
import com.azarenka.evebuilders.domain.enums.MarketOfferStatus;
import com.azarenka.evebuilders.domain.exeptions.ForbiddenException;
import com.azarenka.evebuilders.domain.exeptions.NotFoundException;
import com.azarenka.evebuilders.domain.exeptions.ValidationException;
import com.azarenka.evebuilders.repository.database.market.MarketItemTypeRepository;
import com.azarenka.evebuilders.repository.database.market.MarketOfferRepository;
import com.azarenka.evebuilders.repository.database.properties.IDestinationRepository;
import com.azarenka.evebuilders.service.api.market.IMarketOfferService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.transaction.Transactional;

@Service
public class MarketOfferService implements IMarketOfferService {

    @Autowired
    private MarketOfferRepository offerRepo;
    @Autowired
    private MarketItemTypeRepository itemTypeRepo;
    @Autowired
    private IDestinationRepository locationRepo;

    @Override
    @Transactional
    public String createDraft(String sellerUsername, String marketItemTypeId, String locationId, long qtyTotal,
                              BigDecimal pricePerUnit, LocalDate expiresOn) {
        if (qtyTotal <= 0) {
            throw new ValidationException("qtyTotal must be > 0");
        }
        if (pricePerUnit == null || pricePerUnit.signum() <= 0) {
            throw new ValidationException("pricePerUnit must be > 0");
        }
        var itemType = getItemTypeOrThrow(marketItemTypeId);
        var location = getLocationOrThrow(locationId);
        var offer = new MarketOffer();
        offer.setId(java.util.UUID.randomUUID().toString());
        offer.setItemType(itemType);
        offer.setSellerUsername(sellerUsername);
        offer.setLocation(location);
        offer.setPricePerUnit(pricePerUnit);
        offer.setQtyTotal(qtyTotal);
        offer.setQtyAvailable(qtyTotal);
        offer.setStatus(MarketOfferStatus.DRAFT);
        offer.setExpiresOn(expiresOn);
        offer.setCreatedOn(LocalDate.now());
        offer.setUpdatedOn(LocalDate.now());
        offerRepo.save(offer);
        return offer.getId();
    }

    @Override
    @Transactional
    public void activate(String offerId, String actorUsername) {
        var offer = getOfferOrThrow(offerId);
        ensureActorIsSeller(offer, actorUsername);

        if (offer.getStatus() != MarketOfferStatus.DRAFT && offer.getStatus() != MarketOfferStatus.CANCELLED) {
            throw new ValidationException("Only DRAFT/CANCELLED offers can be activated");
        }
        if (offer.getQtyTotal() <= 0 || offer.getQtyAvailable() <= 0) {
            throw new ValidationException("qty must be > 0 for activation");
        }
        if (offer.getExpiresOn() != null && offer.getExpiresOn().isBefore(LocalDate.now())) {
            throw new ValidationException("expiresOn is in the past");
        }

        offer.setStatus(MarketOfferStatus.ACTIVE);
        offer.setUpdatedOn(LocalDate.now());
        offerRepo.save(offer);
    }

    @Override
    @Transactional
    public void update(String offerId,
                       String actorUsername,
                       BigDecimal pricePerUnit,
                       Long qtyTotal,
                       LocalDate expiresOn) {

        var offer = getOfferOrThrow(offerId);
        ensureActorIsSeller(offer, actorUsername);

        // Разрешим апдейт в DRAFT/ACTIVE
        if (offer.getStatus() != MarketOfferStatus.DRAFT && offer.getStatus() != MarketOfferStatus.ACTIVE) {
            throw new ValidationException("Only DRAFT/ACTIVE offer can be updated");
        }
        if (pricePerUnit != null) {
            if (pricePerUnit.signum() <= 0) throw new ValidationException("pricePerUnit must be > 0");
            // TODO: запрет апдейта цены при активных резервах (когда они будут)
            offer.setPricePerUnit(pricePerUnit);
        }
        if (qtyTotal != null) {
            if (qtyTotal <= 0) throw new ValidationException("qtyTotal must be > 0");
            // Корректируем доступное количество, если общее уменьшили ниже уже зарезервированного/проданного
            long soldOrReserved = offer.getQtyTotal() - offer.getQtyAvailable();
            if (qtyTotal < soldOrReserved) {
                throw new ValidationException("qtyTotal less than already reserved/sold");
            }
            offer.setQtyTotal(qtyTotal);
            offer.setQtyAvailable(qtyTotal - soldOrReserved);
        }
        if (expiresOn != null) {
            if (expiresOn.isBefore(LocalDate.now())) throw new ValidationException("expiresOn is in the past");
            offer.setExpiresOn(expiresOn);
        }
        offer.setUpdatedOn(LocalDate.now());
        offerRepo.save(offer);
    }

    @Override
    @Transactional
    public void cancel(String offerId, String actorUsername, boolean force) {
        var offer = getOfferOrThrow(offerId);

        if (!force) {
            ensureActorIsSeller(offer, actorUsername);
            // TODO: проверить отсутствие активных резервов
        }
        if (offer.getStatus() == MarketOfferStatus.COMPLETED || offer.getStatus() == MarketOfferStatus.CANCELLED) {
            return; // идемпотентность
        }
        offer.setStatus(MarketOfferStatus.CANCELLED);
        offer.setUpdatedOn(LocalDate.now());
        offerRepo.save(offer);
    }


    @Override
    @Transactional()
    public Page<MarketRowDTO> search(MaterialType materialType,
                                     MarketOfferStatus[] statuses,
                                     String locationId,
                                     BigDecimal minPrice,
                                     BigDecimal maxPrice,
                                     Long minQty,
                                     Long maxQty,
                                     Pageable pageable) {
        var statusList = (statuses == null || statuses.length == 0)
            ? java.util.List.of(MarketOfferStatus.ACTIVE)
            : java.util.List.of(statuses);
        Page<MarketOffer> page = offerRepo.findByStatusIn(statusList, pageable);
        return page.map(offer -> new MarketRowDTO(
            "SELL",
            offer.getId(),
            /* resourceName */ "TODO: name via invTypes(" + offer.getItemType().getTypeId() + ")",
            offer.getItemType().getTypeId(),
            offer.getPricePerUnit(),
            offer.getQtyAvailable(),
            offer.getLocation().getDestination(),
            offer.getExpiresOn(),
            "RESERVE"
        ));
    }

    private MarketOffer getOfferOrThrow(String offerId) {
        return offerRepo.findById(offerId)
            .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));
    }

    private MarketItemType getItemTypeOrThrow(String id) {
        return itemTypeRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("MarketItemType not found: " + id));
    }

    private Destination getLocationOrThrow(String id) {
        return locationRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Location not found: " + id));
    }

    private void ensureActorIsSeller(MarketOffer offer, String actorUsername) {
        if (!offer.getSellerUsername().equals(actorUsername)) {
            throw new ForbiddenException("Only seller can modify this offer");
        }
    }
}
