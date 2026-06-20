package com.azarenka.evebuilders.domain.dto.order;

import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DistributedOrderViewDto(
    String id,
    String orderNumber,
    String shipName,
    String userName,
    Integer count,
    Integer countReady,
    String fitId,
    OrderRights orderRights,
    OrderStatusEnum orderStatus,
    LocalDate createdDate,
    LocalDate appliedDate,
    LocalDate finishedDate,
    String category,
    BigDecimal price,
    boolean isAssembly
) {
}
