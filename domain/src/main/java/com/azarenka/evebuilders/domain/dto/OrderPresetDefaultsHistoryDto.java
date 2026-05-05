package com.azarenka.evebuilders.domain.dto;

import java.time.LocalDate;

public class OrderPresetDefaultsHistoryDto {

    private String changedBy;
    private LocalDate changedDate;
    private String orderType;
    private String receiverType;
    private String receiverName;
    private String receiverRefId;
    private String priority;
    private String blueprint;
    private String orderRights;
    private String rightsholder;

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDate getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(LocalDate changedDate) {
        this.changedDate = changedDate;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverRefId() {
        return receiverRefId;
    }

    public void setReceiverRefId(String receiverRefId) {
        this.receiverRefId = receiverRefId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(String blueprint) {
        this.blueprint = blueprint;
    }

    public String getOrderRights() {
        return orderRights;
    }

    public void setOrderRights(String orderRights) {
        this.orderRights = orderRights;
    }

    public String getRightsholder() {
        return rightsholder;
    }

    public void setRightsholder(String rightsholder) {
        this.rightsholder = rightsholder;
    }
}
