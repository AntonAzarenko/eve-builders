package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.OrderStatusEnum;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "disctribuited_order_audit", schema = "builders")
public class DistributedOrderAudit {

    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "order_number")
    private String orderNumber;
    @Column(name = "status")
    private OrderStatusEnum status;
    @Column(name = "reason")
    private String reason;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DistributedOrderAudit that = (DistributedOrderAudit) o;

        return new EqualsBuilder().append(id, that.id)
            .append(status, that.status)
            .append(reason, that.reason)
            .append(createdBy, that.createdBy)
            .append(createdDate, that.createdDate)
            .append(updatedBy, that.updatedBy)
            .append(updatedDate, that.updatedDate)
            .append(orderNumber, that.orderNumber)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id)
            .append(status)
            .append(reason)
            .append(createdBy)
            .append(createdDate)
            .append(updatedBy)
            .append(updatedDate)
            .append(orderNumber)
            .toHashCode();
    }
}
