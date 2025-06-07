package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.GroupTypeEnum;
import com.azarenka.evebuilders.domain.OrderStatusEnum;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class OrderFilter {

    private String userId;
    private List<OrderStatusEnum> statuses;
    private List<String> typeOrder;
    private Integer minFreeCount;
    private Boolean isDistributed;

    public OrderFilter() {
    }

    public OrderFilter(OrderFilter orderFilter) {
        this.userId = orderFilter.getUserId();
        this.statuses = orderFilter.getStatuses();
        this.typeOrder = orderFilter.getTypesOrder();
        this.minFreeCount = orderFilter.getMinFreeCount();
        this.isDistributed = orderFilter.isDistributed();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderStatusEnum> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<OrderStatusEnum> statuses) {
        this.statuses = statuses;
    }

    public List<String> getTypesOrder() {
        return typeOrder;
    }

    public void setShipType(List<String> shipType) {
        typeOrder = shipType;
    }

    public Integer getMinFreeCount() {
        return minFreeCount;
    }

    public void setMinFreeCount(Integer minFreeCount) {
        this.minFreeCount = minFreeCount;
    }

    public Boolean isDistributed() {
        return isDistributed;
    }

    public void setDistributed(Boolean distributed) {
        isDistributed = distributed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        OrderFilter that = (OrderFilter) o;

        return new EqualsBuilder()
                .append(getTypesOrder(), that.typeOrder).append(minFreeCount, that.minFreeCount).append(isDistributed,
                        that.isDistributed).append(statuses, that.statuses).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(statuses).append(typeOrder).append(minFreeCount).append(isDistributed).toHashCode();
    }
}
