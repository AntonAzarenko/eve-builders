package com.azarenka.evebuilders.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContractItem {

    @JsonProperty("type_id")
    private int typeId;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("is_included")
    private boolean included;
}
