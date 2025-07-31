package com.azarenka.evebuilders.domain.dto.file;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivityProduct {

    @JsonProperty("typeID")
    private int typeID;
    @JsonProperty("activityID")
    private int activityID;
    @JsonProperty("productTypeID")
    private int productTypeID;
    @JsonProperty("quantity")
    private int quantity;

    public int getActivityID() {
        return activityID;
    }

    public void setActivityID(int activityID) {
        this.activityID = activityID;
    }

    public int getProductTypeID() {
        return productTypeID;
    }

    public void setProductTypeID(int productTypeID) {
        this.productTypeID = productTypeID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }
}
