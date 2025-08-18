package com.azarenka.evebuilders.domain.dto.market;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestRowDTO(String requestId, String requesterUsername, String resourceName, String typeId,
                            long qtyNeeded, long qtyRemaining, BigDecimal pricePerUnit, String locationName,
                            LocalDate deadline, String status) {
}

