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
@Table(name = "order_preset_defaults", schema = "builders")
public class OrderPresetDefaults {

    @Id
    @Column(name = "id", nullable = false)
    private String id;
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
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_date")
    private LocalDate createdDate;
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }
}
