package com.azarenka.evebuilders.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ContractItem {

    @JsonProperty("type_id")
    private int typeId;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("is_included")
    private boolean included;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContractItem that = (ContractItem) o;

        return new EqualsBuilder().append(typeId, that.typeId)
            .append(quantity, that.quantity)
            .append(included, that.included)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(typeId).append(quantity).append(included).toHashCode();
    }
}
