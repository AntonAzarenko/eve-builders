package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.enums.MarketOfferStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "market_offer", schema = "builders")
public class MarketOffer {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "market_item_type_id", nullable = false)
    private MarketItemType itemType;
    @Column(name = "seller_username", nullable = false)
    private String sellerUsername;
    @ManyToOne(optional = false)
    private Destination location;
    @Column(nullable = false)
    private BigDecimal pricePerUnit;
    @Column(nullable = false)
    private long qtyTotal;
    @Column(nullable = false)
    private long qtyAvailable;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketOfferStatus status = MarketOfferStatus.DRAFT;

    private LocalDate expiresOn;
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

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
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

    public long getQtyTotal() {
        return qtyTotal;
    }

    public void setQtyTotal(long qtyTotal) {
        this.qtyTotal = qtyTotal;
    }

    public long getQtyAvailable() {
        return qtyAvailable;
    }

    public void setQtyAvailable(long qtyAvailable) {
        this.qtyAvailable = qtyAvailable;
    }

    public MarketOfferStatus getStatus() {
        return status;
    }

    public void setStatus(MarketOfferStatus status) {
        this.status = status;
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(LocalDate expiresOn) {
        this.expiresOn = expiresOn;
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
