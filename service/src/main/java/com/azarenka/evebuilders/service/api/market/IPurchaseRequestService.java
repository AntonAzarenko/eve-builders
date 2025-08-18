package com.azarenka.evebuilders.service.api.market;

import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.dto.market.RequestRowDTO;
import com.azarenka.evebuilders.domain.enums.MarketRequestStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IPurchaseRequestService {
    /** Создать заявку в статусе DRAFT */
    String createDraft(String requesterUsername,
                       String marketItemTypeId,
                       String locationId,
                       long qtyNeeded,
                       BigDecimal pricePerUnit,
                       LocalDate deadline);

    /** Активировать заявку (qtyRemaining>0, не EXPIRED) */
    void activate(String requestId, String actorUsername);

    /** Обновить заявку (цена/дедлайн/qtyNeeded — в рамках правил) */
    void update(String requestId,
                String actorUsername,
                BigDecimal pricePerUnit,
                Long qtyNeeded,
                LocalDate deadline);

    /** Отменить заявку */
    void cancel(String requestId, String actorUsername, boolean force);

    /** Поиск/листинг заявок */
    Page<RequestRowDTO> search(
        MaterialType materialType,
        MarketRequestStatus[] statuses,
        String locationId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Long minQty,
        Long maxQty,
        LocalDate minDeadline,
        LocalDate maxDeadline,
        Pageable pageable);
}
