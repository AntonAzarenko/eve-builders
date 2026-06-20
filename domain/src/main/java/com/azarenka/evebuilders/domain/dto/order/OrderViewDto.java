package com.azarenka.evebuilders.domain.dto.order;

import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderViewDto(
    String id,
    String orderNumber,
    String itemName,
    Integer count,
    Integer inProgressCount,
    Integer freeCount,
    Integer countReady,
    BigDecimal price,
    String orderType,
    String destination,
    String receiver,
    String priority,
    boolean bluePrint,
    OrderStatusEnum orderStatus,
    String createdBy,
    LocalDate createdDate,
    String updatedBy,
    LocalDate updatedDate,
    String fitId,
    OrderRights orderRights,
    String rightsholder,
    String category,
    LocalDate finishDate,
    String distributionStatus,
    Integer daysToFinish,
    Integer progressPercent
) {
}
