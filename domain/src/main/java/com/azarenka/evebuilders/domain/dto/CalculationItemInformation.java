package com.azarenka.evebuilders.domain.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;

public class CalculationItemInformation {

    private Integer typeID;
    private String typeName;
    //clear value
    private double requiredQuantity;
    //value per batch
    private double productPerBatch;
    private double producedQuantity;
    private double excessQuantity;

    private double hasQuantity;

    private BigDecimal jitaSellPrice;
    private BigDecimal jitaBuyPrice;
    private BigDecimal jitaSplitPrice;

    private ItemDto itemDto;

    public ItemDto getItemDto() {
        return itemDto;
    }

    public void setItemDto(ItemDto itemDto) {
        this.itemDto = itemDto;
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

    public double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public double getProductPerBatch() {
        return productPerBatch;
    }

    public void setProductPerBatch(double productPerBatch) {
        this.productPerBatch = productPerBatch;
    }

    public double getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(double producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public double getExcessQuantity() {
        return excessQuantity;
    }

    public void setExcessQuantity(double excessQuantity) {
        this.excessQuantity = excessQuantity;
    }

    public double getHasQuantity() {
        return hasQuantity;
    }

    public void setHasQuantity(double hasQuantity) {
        this.hasQuantity = hasQuantity;
    }

    public BigDecimal getJitaSellPrice() {
        return jitaSellPrice;
    }

    public void setJitaSellPrice(BigDecimal jitaSellPrice) {
        this.jitaSellPrice = jitaSellPrice;
    }

    public BigDecimal getJitaBuyPrice() {
        return jitaBuyPrice;
    }

    public void setJitaBuyPrice(BigDecimal jitaBuyPrice) {
        this.jitaBuyPrice = jitaBuyPrice;
    }

    public BigDecimal getJitaSplitPrice() {
        return jitaSplitPrice;
    }

    public void setJitaSplitPrice(BigDecimal jitaSplitPrice) {
        this.jitaSplitPrice = jitaSplitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CalculationItemInformation that = (CalculationItemInformation) o;

        return new EqualsBuilder().append(requiredQuantity, that.requiredQuantity)
            .append(productPerBatch, that.productPerBatch)
            .append(producedQuantity, that.producedQuantity)
            .append(excessQuantity, that.excessQuantity)
            .append(hasQuantity, that.hasQuantity)
            .append(typeID, that.typeID)
            .append(typeName, that.typeName)
            .append(jitaSellPrice, that.jitaSellPrice)
            .append(jitaBuyPrice, that.jitaBuyPrice)
            .append(jitaSplitPrice, that.jitaSplitPrice)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(typeID)
            .append(typeName)
            .append(requiredQuantity)
            .append(productPerBatch)
            .append(producedQuantity)
            .append(excessQuantity)
            .append(hasQuantity)
            .append(jitaSellPrice)
            .append(jitaBuyPrice)
            .append(jitaSplitPrice)
            .toHashCode();
    }
}
