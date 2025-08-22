package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.enums.MarketDeliveryStatus;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "deliveries", schema = "builders")
public class Delivery {

    @Id
    @Column(length = 64)
    private String id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private TradeOrder order;
    @Column(nullable = false)
    private long qty;

    private String proofRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketDeliveryStatus status = MarketDeliveryStatus.PENDING;

    private LocalDate deliveredOn;

    @PrePersist
    void prePersist() {
        deliveredOn = LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TradeOrder getOrder() {
        return order;
    }

    public void setOrder(TradeOrder order) {
        this.order = order;
    }

    public long getQty() {
        return qty;
    }

    public void setQty(long qty) {
        this.qty = qty;
    }

    public String getProofRef() {
        return proofRef;
    }

    public void setProofRef(String proofRef) {
        this.proofRef = proofRef;
    }

    public MarketDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(MarketDeliveryStatus status) {
        this.status = status;
    }

    public LocalDate getDeliveredOn() {
        return deliveredOn;
    }

    public void setDeliveredOn(LocalDate deliveredOn) {
        this.deliveredOn = deliveredOn;
    }
}

