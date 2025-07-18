package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_filter", schema = "builders")
public class OrderFilter {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String userId;
    @Column(name = "statuses")
    @Enumerated(EnumType.STRING)
    private List<OrderStatusEnum> statuses;
    @Column(name = "types")
    private List<String> orderTypes;
    @Column(name = "min_count")
    private Integer minFreeCount;
    @Column(name = "is_distributed")
    private Boolean isDistributed;

    public OrderFilter() {
    }

    public OrderFilter(OrderFilter orderFilter) {
        this.userId = orderFilter.getUserId();
        this.statuses = orderFilter.getStatuses();
        this.orderTypes = orderFilter.getOrderTypes();
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

    public List<String> getOrderTypes() {
        return orderTypes;
    }

    public void setOrderTypes(List<String> shipType) {
        orderTypes = shipType;
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
                .append(getOrderTypes(), that.orderTypes).append(minFreeCount, that.minFreeCount).append(isDistributed,
                        that.isDistributed).append(statuses, that.statuses).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(statuses).append(orderTypes).append(minFreeCount).append(isDistributed).toHashCode();
    }
}
