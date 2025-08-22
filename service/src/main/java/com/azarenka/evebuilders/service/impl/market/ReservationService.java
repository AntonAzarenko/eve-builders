package com.azarenka.evebuilders.service.impl.market;

import com.azarenka.evebuilders.domain.db.MarketOffer;
import com.azarenka.evebuilders.domain.db.PurchaseRequest;
import com.azarenka.evebuilders.domain.db.Reservation;
import com.azarenka.evebuilders.domain.enums.MarketOfferStatus;
import com.azarenka.evebuilders.domain.enums.MarketRequestStatus;
import com.azarenka.evebuilders.domain.enums.MarketReservationStatus;
import com.azarenka.evebuilders.domain.exeptions.ForbiddenException;
import com.azarenka.evebuilders.domain.exeptions.NotFoundException;
import com.azarenka.evebuilders.domain.exeptions.ValidationException;
import com.azarenka.evebuilders.repository.database.market.MarketOfferRepository;
import com.azarenka.evebuilders.repository.database.market.PurchaseRequestRepository;
import com.azarenka.evebuilders.repository.database.market.ReservationRepository;
import com.azarenka.evebuilders.service.api.market.IReservationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import jakarta.transaction.Transactional;

@Service
public class ReservationService implements IReservationService {

    @Autowired
    private ReservationRepository reservationRepo;
    @Autowired
    private MarketOfferRepository offerRepo;
    @Autowired
    private PurchaseRequestRepository requestRepo;

    private MarketOffer getOfferOrThrow(String offerId) {
        return offerRepo.findById(offerId)
            .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));
    }

    private PurchaseRequest getRequestOrThrow(String requestId) {
        return requestRepo.findById(requestId)
            .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
    }

    private void ensureOfferActive(MarketOffer offer) {
        if (offer.getStatus() != MarketOfferStatus.ACTIVE) {
            throw new ValidationException("Offer must be ACTIVE to reserve");
        }
        if (offer.getExpiresOn() != null && offer.getExpiresOn().isBefore(LocalDate.now())) {
            throw new ValidationException("Offer is expired");
        }
    }

    private void ensureRequestActive(PurchaseRequest req) {
        if (req.getStatus() != MarketRequestStatus.ACTIVE && req.getStatus() != MarketRequestStatus.PARTIALLY_FILLED) {
            throw new ValidationException("Request must be ACTIVE/PARTIALLY_FILLED to reserve");
        }
        if (req.getDeadline() != null && req.getDeadline().isBefore(LocalDate.now())) {
            throw new ValidationException("Request is expired");
        }
    }

    @Override
    @Transactional
    public String reserveFromOffer(String offerId, String reserverUsername, long qty, LocalDate reservedUntil) {
        var offer = getOfferOrThrow(offerId);
        ensureOfferActive(offer);
        if (qty <= 0) {
            throw new ValidationException("qty must be > 0");
        }
        if (qty > offer.getQtyAvailable()) {
            throw new ValidationException("qty exceeds available");
        }
        if (reservedUntil != null && reservedUntil.isBefore(LocalDate.now())) {
            throw new ValidationException("reservedUntil is in the past");
        }
        if (offer.getSellerUsername().equals(reserverUsername)) {
            throw new ForbiddenException("Seller cannot reserve own offer");
        }
        var res = new Reservation();
        res.setId(java.util.UUID.randomUUID().toString());
        res.setStatus(MarketReservationStatus.ACTIVE);
        res.setReserverUsername(reserverUsername);
        res.setOffer(offer);
        res.setQty(qty);
        res.setReservedUntil(reservedUntil);
        res.setCreatedOn(LocalDate.now());
        offer.setQtyAvailable(offer.getQtyAvailable() - qty);
        if (offer.getQtyAvailable() == 0) {
            offer.setStatus(MarketOfferStatus.RESERVED);
        }
        reservationRepo.save(res);
        offerRepo.save(offer);

        return res.getId();
    }

    @Override
    @Transactional
    public String reserveAgainstRequest(String requestId, String reserverUsername, long qty, LocalDate reservedUntil) {
        var req = getRequestOrThrow(requestId);
        ensureRequestActive(req);

        if (qty <= 0) {
            throw new ValidationException("qty must be > 0");
        }
        if (qty > req.getQtyRemaining()) {
            throw new ValidationException("qty exceeds remaining");
        }
        if (reservedUntil != null && reservedUntil.isBefore(LocalDate.now())) {
            throw new ValidationException("reservedUntil is in the past");
        }
        if (req.getRequesterUsername().equals(reserverUsername)) {
            throw new ForbiddenException("Requester cannot fulfill own request");
        }

        var res = new Reservation();
        res.setId(java.util.UUID.randomUUID().toString());
        res.setStatus(MarketReservationStatus.ACTIVE);
        res.setReserverUsername(reserverUsername);
        res.setRequest(req);
        res.setQty(qty);
        res.setReservedUntil(reservedUntil);
        res.setCreatedOn(LocalDate.now());
        req.setQtyRemaining(req.getQtyRemaining() - qty);
        if (req.getQtyRemaining() == 0) {
            req.setStatus(
                MarketRequestStatus.PARTIALLY_FILLED); // или сразу COMPLETED после конверсии — зависит от бизнес-правил
        } else {
            req.setStatus(MarketRequestStatus.PARTIALLY_FILLED);
        }

        reservationRepo.save(res);
        requestRepo.save(req);

        return res.getId();
    }

    @Override
    @Transactional
    public void cancel(String reservationId, String actorUsername) {
        var res = reservationRepo.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
        if (res.getStatus() != MarketReservationStatus.ACTIVE) {
            return; // идемпотентность
        }
        if (res.getOffer() != null) {
            var offer = res.getOffer();
            if (!res.getReserverUsername().equals(actorUsername)) {
                throw new ForbiddenException("Only reserver can cancel reservation");
            }
            offer.setQtyAvailable(offer.getQtyAvailable() + res.getQty());
            if (offer.getStatus() == MarketOfferStatus.RESERVED) {
                offer.setStatus(MarketOfferStatus.ACTIVE); // снова активен
            }
            offerRepo.save(offer);
        } else if (res.getRequest() != null) {
            var req = res.getRequest();
            if (!res.getReserverUsername().equals(actorUsername)) {
                throw new ForbiddenException("Only reserver can cancel reservation");
            }
            req.setQtyRemaining(req.getQtyRemaining() + res.getQty());
            // статус можно вернуть в ACTIVE, если не осталось других броней/закрытий ???
            if (req.getStatus() == MarketRequestStatus.PARTIALLY_FILLED) {
                // точную логику нужно доработать после учета других резервов
            }
            requestRepo.save(req);
        }
        res.setStatus(MarketReservationStatus.CANCELLED);
        reservationRepo.save(res);
    }

    @Override
    @Transactional
    public int expireOverdue(LocalDate today) {
        var list = reservationRepo.findByStatusAndReservedUntilBefore(MarketReservationStatus.ACTIVE, today);
        int count = 0;
        for (var res : list) {
            if (res.getOffer() != null) {
                var offer = res.getOffer();
                offer.setQtyAvailable(offer.getQtyAvailable() + res.getQty());
                if (offer.getStatus() == MarketOfferStatus.RESERVED) {
                    offer.setStatus(MarketOfferStatus.ACTIVE);
                }
                offerRepo.save(offer);
            } else if (res.getRequest() != null) {
                var req = res.getRequest();
                req.setQtyRemaining(req.getQtyRemaining() + res.getQty());
                requestRepo.save(req);
            }
            res.setStatus(MarketReservationStatus.EXPIRED);
            reservationRepo.save(res);
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public void setStatus(String reservationId, MarketReservationStatus status, String actorUsername) {
        // TODO: опционально — проверка прав координатора
        var res = reservationRepo.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
        res.setStatus(status);
        reservationRepo.save(res);
    }
}
