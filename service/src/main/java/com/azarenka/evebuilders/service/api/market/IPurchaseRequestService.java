package com.azarenka.evebuilders.service.api.market;

import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.dto.market.DemandRowDTO;
import com.azarenka.evebuilders.domain.enums.MarketRequestStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IPurchaseRequestService {

    String createDraft(String requesterUsername, String marketItemTypeId, String locationId, long qtyNeeded,
                       BigDecimal pricePerUnit, LocalDate deadline);

    void activate(String requestId, String actorUsername);

    void update(String requestId, String actorUsername, BigDecimal pricePerUnit, Long qtyNeeded, LocalDate deadline);

    void cancel(String requestId, String actorUsername, boolean force);


    Page<DemandRowDTO> search(MaterialType materialType, MarketRequestStatus[] statuses, String locationId,
                              BigDecimal minPrice, BigDecimal maxPrice, Long minQty, Long maxQty,
                              LocalDate minDeadline, LocalDate maxDeadline, Pageable pageable);
}
