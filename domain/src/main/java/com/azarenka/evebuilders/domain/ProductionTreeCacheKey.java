package com.azarenka.evebuilders.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ProductionTreeCacheKey {

    private final String typeName;
    private final int quantity;

    public ProductionTreeCacheKey(String typeName, int quantity) {
        this.typeName = typeName;
        this.quantity = quantity;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProductionTreeCacheKey that = (ProductionTreeCacheKey) o;

        return new EqualsBuilder()
                .append(quantity, that.quantity)
                .append(typeName, that.typeName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(typeName)
                .append(quantity)
                .toHashCode();
    }
}
