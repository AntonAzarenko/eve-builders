package com.azarenka.evebuilders.service.api.market;

import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.dto.market.MarketRowDTO;
import com.azarenka.evebuilders.domain.enums.MarketOfferStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IMarketOfferService {

    String createDraft(String sellerUsername, String marketItemTypeId, String locationId, long qtyTotal,
                       BigDecimal pricePerUnit, LocalDate expiresOn);

    void activate(String offerId, String actorUsername);

    void update(String offerId, String actorUsername, BigDecimal pricePerUnit, Long qtyTotal, LocalDate expiresOn);

    void cancel(String offerId, String actorUsername, boolean force);

    Page<MarketRowDTO> search(MaterialType materialType, MarketOfferStatus[] statuses, String locationId,
                              BigDecimal minPrice, BigDecimal maxPrice, Long minQty, Long maxQty, Pageable pageable);
}
