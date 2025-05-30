package com.azarenka.evebuilders.domain.sqllite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dgmTypeAttributes")
public class DgmTypeAttribute {

    @Id
    @Column(name = "typeID")
    private Integer typeId;

    @Column(name = "attributeID")
    private Integer attributeId;

    @Column(name = "value")
    private Double value;

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
