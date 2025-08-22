package com.azarenka.evebuilders.domain.dto.market;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderRowDTO(String orderId,
                          String buyerUsername,
                          String sellerUsername,
                          String resourceName,
                          String typeId,
                          long qty,
                          BigDecimal pricePerUnit,
                          BigDecimal totalPrice,
                          String locationName,
                          String status,
                          LocalDate createdOn) {
}
