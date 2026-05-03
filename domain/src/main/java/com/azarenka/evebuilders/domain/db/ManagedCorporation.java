package com.azarenka.evebuilders.domain.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "managed_corporation", schema = "builders")
public class ManagedCorporation {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "eve_corporation_id", nullable = false)
    private Long eveCorporationId;

    @Column(name = "corporation_name", nullable = false)
    private String corporationName;

    @Column(name = "corporation_ticker")
    private String corporationTicker;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
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

    public Long getEveCorporationId() {
        return eveCorporationId;
    }

    public void setEveCorporationId(Long eveCorporationId) {
        this.eveCorporationId = eveCorporationId;
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName;
    }

    public String getCorporationTicker() {
        return corporationTicker;
    }

    public void setCorporationTicker(String corporationTicker) {
        this.corporationTicker = corporationTicker;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
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
