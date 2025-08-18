package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.enums.MarketReservationStatus;

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
import jakarta.persistence.Table;

@Entity
@Table(
    name = "reservations",
    indexes = @Index(name = "ix_res_status", columnList = "status")
    , schema = "builders"
)
public class Reservation {

    @Id
    @Column(length = 64)
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketReservationStatus status = MarketReservationStatus.ACTIVE;
    @Column(name = "reserver_username", nullable = false, length = 64)
    private String reserverUsername;
    @ManyToOne
    @JoinColumn(name = "offer_id")
    private MarketOffer offer;
    @ManyToOne
    @JoinColumn(name = "request_id")
    private PurchaseRequest request;
    @Column(nullable = false)
    private long qty;
    private LocalDate reservedUntil;
    private LocalDate createdOn;
    @PrePersist
    void prePersist() {
        createdOn = LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MarketReservationStatus getStatus() {
        return status;
    }

    public void setStatus(MarketReservationStatus status) {
        this.status = status;
    }

    public String getReserverUsername() {
        return reserverUsername;
    }

    public void setReserverUsername(String reserverUsername) {
        this.reserverUsername = reserverUsername;
    }

    public MarketOffer getOffer() {
        return offer;
    }

    public void setOffer(MarketOffer offer) {
        this.offer = offer;
    }

    public PurchaseRequest getRequest() {
        return request;
    }

    public void setRequest(PurchaseRequest request) {
        this.request = request;
    }

    public long getQty() {
        return qty;
    }

    public void setQty(long qty) {
        this.qty = qty;
    }

    public LocalDate getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(LocalDate reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDate createdOn) {
        this.createdOn = createdOn;
    }
}
