package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.enums.MarketRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "purchase_requests",
    indexes = @Index(name = "ix_pr_status", columnList = "status")
    , schema = "builders"
)
public class PurchaseRequest {

    @Id
    @Column(length = 64)
    private String id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "market_item_type_id", nullable = false)
    private MarketItemType itemType;
    @Column(name = "requester_username", nullable = false, length = 64)
    private String requesterUsername;
    @ManyToOne(optional = false)
    private Destination location;
    @Column(nullable = false)
    private BigDecimal pricePerUnit;
    @Column(nullable = false)
    private long qtyNeeded;
    @Column(nullable = false)
    private long qtyRemaining;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketRequestStatus status = MarketRequestStatus.DRAFT;

    private LocalDate deadline;
    private LocalDate createdOn;
    private LocalDate updatedOn;

    @PrePersist
    void prePersist() {
        LocalDate today = LocalDate.now();
        createdOn = today;
        updatedOn = today;
    }

    @PreUpdate
    void preUpdate() {
        updatedOn = LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MarketItemType getItemType() {
        return itemType;
    }

    public void setItemType(MarketItemType itemType) {
        this.itemType = itemType;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public Destination getLocation() {
        return location;
    }

    public void setLocation(Destination location) {
        this.location = location;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public long getQtyNeeded() {
        return qtyNeeded;
    }

    public void setQtyNeeded(long qtyNeeded) {
        this.qtyNeeded = qtyNeeded;
    }

    public long getQtyRemaining() {
        return qtyRemaining;
    }

    public void setQtyRemaining(long qtyRemaining) {
        this.qtyRemaining = qtyRemaining;
    }

    public MarketRequestStatus getStatus() {
        return status;
    }

    public void setStatus(MarketRequestStatus status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDate createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDate getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDate updatedOn) {
        this.updatedOn = updatedOn;
    }
}
