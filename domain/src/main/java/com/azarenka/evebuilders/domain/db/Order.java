package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "orders", schema = "builders")
public class Order {

    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "category")
    private String category;
    @Column(name = "group_name")
    private String groupName;
    @Column(name = "order_number", unique = true)
    private String orderNumber;
    @Column(name = "ship_name")
    private String shipName;
    @Column(name = "count")
    private Integer count;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "order_type")
    private String orderType;
    @Column(name = "destination")
    private String destination;
    @Column(name = "receiver")
    private String receiver;
    @Column(name = "priority")
    private String priority;
    @Column(name = "blue_print")
    private boolean bluePrint;
    @Column(name = "status")
    private OrderStatusEnum orderStatus;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_date")
    private LocalDate updatedDate;
    @Column(name = "fit_id")
    private String fitId;
    @Column(name = "request_id")
    private String requestId;
    @Column(name = "order_rights")
    private OrderRights orderRights;
    @Column(name = "rightsholder")
    private String rightsholder;
    @Column(name = "in_progress_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer inProgressCount;
    @Column(name = "ready", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer countReady;
    @Column(name = "finish_by")
    private LocalDate finishBy;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String group) {
        this.groupName = group;
    }

    public LocalDate getFinishBy() {
        return finishBy;
    }

    public void setFinishBy(LocalDate finishBy) {
        this.finishBy = finishBy;
    }

    public Integer getCountReady() {
        return countReady;
    }

    public void setCountReady(Integer countReady) {
        this.countReady = countReady;
    }

    public Integer getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(Integer inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isBluePrint() {
        return bluePrint;
    }

    public void setBluePrint(boolean bluePrint) {
        this.bluePrint = bluePrint;
    }

    public String getFit() {
        return fitId;
    }

    public void setFit(String fit) {
        this.fitId = fit;
    }

    public OrderStatusEnum getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatusEnum orderStatus) {
        this.orderStatus = orderStatus;
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

    public String getFitId() {
        return fitId;
    }

    public void setFitId(String fitId) {
        this.fitId = fitId;
    }

    public OrderRights getOrderRights() {
        return orderRights;
    }

    public void setOrderRights(OrderRights orderRights) {
        this.orderRights = orderRights;
    }

    public String getRightsholder() {
        return rightsholder;
    }

    public void setRightsholder(String rightsholder) {
        this.rightsholder = rightsholder;
    }
}
