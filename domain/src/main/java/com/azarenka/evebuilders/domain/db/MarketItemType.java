package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.dto.MaterialType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "market_item_types",  schema = "builders",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_market_item_types_type_id",
        columnNames = "type_id"
    ),
    indexes = {
        @Index(name = "ix_market_item_types_material_type", columnList = "material_type"),
        @Index(name = "ix_market_item_types_active", columnList = "is_active")
    }
)
public class MarketItemType {

    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "type_id", nullable = false, length = 64)
    private String typeId;
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false)
    private MaterialType materialType;
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
