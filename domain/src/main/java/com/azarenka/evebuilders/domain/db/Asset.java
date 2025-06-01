package com.azarenka.evebuilders.domain.db;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Asset {
    @JsonProperty("type_id")
    private Integer typeId;

    @JsonProperty("location_id")
    private Long locationId;

    @JsonProperty("quantity")
    private Integer quantity;

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "typeId=" + typeId +
                ", locationId=" + locationId +
                ", quantity=" + quantity +
                '}';
    }
}
