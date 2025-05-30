package com.azarenka.evebuilders.domain.sqllite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "InvTypes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvType {

    @Id
    @Column(name = "typeID", updatable = false, insertable = false)
    private Integer typeID;

    @Column(name = "typeName", updatable = false, insertable = false)
    private String typeName;

    @Column(name = "groupID", updatable = false, insertable = false)
    private Integer groupId;

    @Column(name = "description", updatable = false, insertable = false)
    private String description;

    @Column(name = "mass", updatable = false, insertable = false)
    private Double mass;

    @Column(name = "volume", updatable = false, insertable = false)
    private Double volume;

    @Column(name = "capacity", updatable = false, insertable = false)
    private Double capacity;

    @Column(name = "portionSize", updatable = false, insertable = false)
    private Integer portionSize;

    @Column(name = "raceID", updatable = false, insertable = false)
    private String raceID;

    @Column(name = "basePrice", updatable = false, insertable = false)
    private String basePrice;

    @Column(name = "marketGroupID", updatable = false, insertable = false)
    private Integer marketGroupId;

    @Column(name = "iconID", updatable = false, insertable = false)
    @JsonIgnore
    private Integer iconID;

    public InvType() {
    }

    public InvType(Integer typeID, String typeName, Integer groupId, String description, Double mass, Double volume,
                   Double capacity, Integer portionSize, String raceID, String basePrice, Integer marketGroupId, Integer iconID) {
        this.typeID = typeID;
        this.typeName = typeName;
        this.groupId = groupId;
        this.description = description;
        this.mass = mass;
        this.volume = volume;
        this.capacity = capacity;
        this.portionSize = portionSize;
        this.raceID = raceID;
        this.basePrice = basePrice;
        this.marketGroupId = marketGroupId;
        this.iconID = iconID;
    }

    public Integer getTypeID() {
        return typeID;
    }

    public void setTypeID(Integer typeID) {
        this.typeID = typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMass() {
        return mass;
    }

    public void setMass(Double mass) {
        this.mass = mass;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Integer getPortionSize() {
        return portionSize;
    }

    public void setPortionSize(Integer portionSize) {
        this.portionSize = portionSize;
    }

    public String getRaceID() {
        return raceID;
    }

    public void setRaceID(String raceId) {
        this.raceID = raceId;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getMarketGroupId() {
        return marketGroupId;
    }

    public void setMarketGroupId(Integer marketGroupId) {
        this.marketGroupId = marketGroupId;
    }

    public Integer getIconID() {
        return iconID;
    }

    public void setIconID(Integer iconId) {
        this.iconID = iconId;
    }

    // equals и hashCode (важно для JPA)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvType InvType = (InvType) o;

        return typeID != null ? typeID.equals(InvType.typeID) : InvType.typeID == null;
    }

    @Override
    public int hashCode() {
        return typeID != null ? typeID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "EnvType{" +
                "typeId=" + typeID +
                ", typeName='" + typeName + '\'' +
                ", groupId=" + groupId +
                ", description='" + description + '\'' +
                ", mass=" + mass +
                ", volume=" + volume +
                ", capacity=" + capacity +
                ", portionSize=" + portionSize +
                ", raceId=" + raceID +
                ", basePrice=" + basePrice +
                ", marketGroupId=" + marketGroupId +
                ", iconId=" + iconID +
                '}';
    }
}
