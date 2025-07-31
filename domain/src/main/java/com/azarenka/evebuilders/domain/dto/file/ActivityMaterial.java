package com.azarenka.evebuilders.domain.dto.file;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivityMaterial {

    @JsonProperty("typeID")
    private int typeID;
    @JsonProperty("activityID")
    private int activityID;
    @JsonProperty("materialTypeID")
    private int materialTypeID;
    @JsonProperty("quantity")
    private int quantity;

    public int getActivityID() {
        return activityID;
    }

    public void setActivityID(int activityID) {
        this.activityID = activityID;
    }

    public int getMaterialTypeID() {
        return materialTypeID;
    }

    public void setMaterialTypeID(int materialTypeID) {
        this.materialTypeID = materialTypeID;
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
