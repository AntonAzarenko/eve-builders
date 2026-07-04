package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.enums.ReceiverTargetType;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "order_preset_defaults_history", schema = "builders")
public class OrderPresetDefaultsHistory {

    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "preset_id", nullable = false)
    private String presetId;
    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;
    @Column(name = "order_type", nullable = false)
    private String orderType;
    @Column(name = "receiver_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReceiverTargetType receiverType;
    @Column(name = "receiver_ref_id", nullable = false)
    private String receiverRefId;
    @Column(name = "receiver_name", nullable = false)
    private String receiverName;
    @Column(name = "priority", nullable = false)
    private String priority;
    @Column(name = "blue_print", nullable = false)
    private boolean bluePrint;
    @Column(name = "order_rights", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderRights orderRights;
    @Column(name = "rightsholder", nullable = false)
    private String rightsholder;
    @Column(name = "changed_by", nullable = false)
    private String changedBy;
    @Column(name = "changed_date", nullable = false)
    private LocalDate changedDate;
    @Column(name = "change_reason")
    private String changeReason;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPresetId() {
        return presetId;
    }

    public void setPresetId(String presetId) {
        this.presetId = presetId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isBluePrint() {
        return bluePrint;
    }

    public void setBluePrint(boolean bluePrint) {
        this.bluePrint = bluePrint;
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

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
}
