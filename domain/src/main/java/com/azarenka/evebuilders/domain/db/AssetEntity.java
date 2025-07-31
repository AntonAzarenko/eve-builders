package com.azarenka.evebuilders.domain.db;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assets", schema = "builders")
public class AssetEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id; // Генерируется заранее

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "type_id", nullable = false)
    private Integer typeId;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public AssetEntity() {
    }

    public AssetEntity(String id, String userName,Integer typeId, Long locationId, Integer quantity,
                       LocalDateTime fetchedAt) {
        this.id = id;
        this.userName = userName;
        this.locationId = locationId;
        this.quantity = quantity;
        this.typeId = typeId;
        this.fetchedAt = fetchedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getType() {
        return typeId;
    }

    public void setTypeId(Integer type) {
        this.typeId = type;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
