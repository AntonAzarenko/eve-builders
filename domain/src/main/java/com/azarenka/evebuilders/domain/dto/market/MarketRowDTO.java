package com.azarenka.evebuilders.domain.dto.market;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketRowDTO(String side,             // BUY | SELL
                           String id,               // offerId | requestId
                           String resourceName,     // из invTypes по typeId
                           String typeId,
                           BigDecimal pricePerUnit,
                           long quantity,
                           String locationName,
                           LocalDate expiresOnOrDeadline,
                           String action ) {
}
