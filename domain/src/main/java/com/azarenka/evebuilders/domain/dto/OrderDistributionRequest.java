package com.azarenka.evebuilders.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderDistributionRequest(
    @JsonProperty("order_number") String orderNumber,
    @JsonProperty("count") int count
) {
}
