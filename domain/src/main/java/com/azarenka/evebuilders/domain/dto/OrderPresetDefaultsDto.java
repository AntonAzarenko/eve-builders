package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.db.BlueprintOption;
import com.azarenka.evebuilders.domain.db.OrderType;
import com.azarenka.evebuilders.domain.db.PriorityOption;
import com.azarenka.evebuilders.domain.enums.ReceiverTargetType;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;

public class OrderPresetDefaultsDto {

    private OrderType orderType;
    private ReceiverTargetType receiverType;
    private String receiverRefId;
    private String receiverName;
    private PriorityOption priority;
    private BlueprintOption blueprint;
    private OrderRights orderRights;
    private String rightsholder;
    private boolean receiverMissing;

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public ReceiverTargetType getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(ReceiverTargetType receiverType) {
        this.receiverType = receiverType;
    }

    public String getReceiverRefId() {
        return receiverRefId;
    }

    public void setReceiverRefId(String receiverRefId) {
        this.receiverRefId = receiverRefId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public PriorityOption getPriority() {
        return priority;
    }

    public void setPriority(PriorityOption priority) {
        this.priority = priority;
    }

    public BlueprintOption getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(BlueprintOption blueprint) {
        this.blueprint = blueprint;
    }

    public OrderRights getOrderRights() {
        return orderRights;
    }

    public void setOrderRights(OrderRights orderRights) {
        this.orderRights = orderRights;
    }

    public String getRightsholder() {
        return rightsholder;
    }

    public void setRightsholder(String rightsholder) {
        this.rightsholder = rightsholder;
    }

    public boolean isReceiverMissing() {
        return receiverMissing;
    }

    public void setReceiverMissing(boolean receiverMissing) {
        this.receiverMissing = receiverMissing;
    }
}
