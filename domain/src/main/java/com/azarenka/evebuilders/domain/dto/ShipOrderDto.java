package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.sqllite.OrderRights;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ShipOrderDto {

    private String id;
    private String orderNumber;
    private String shipName;
    private Integer count;
    private BigDecimal price;
    private String orderType;
    private String destination;
    private String receiver;
    private String priority;
    private boolean bluePrint;
    private OrderStatusEnum orderStatus;
    private String createdBy;
    private LocalDate createdDate = LocalDate.now();
    private String updatedBy;
    private LocalDate updatedDate;
    private String fitId;
    private OrderRights orderRights;
    private String rightsholder;
    private String category;
    private Integer inProgressCount = 0;
    private Integer countReady;
    private LocalDate finishBy;

    public ShipOrderDto(Order order) {
        this.id = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.shipName = order.getShipName();
        this.count = order.getCount();
        this.price = order.getPrice();
        this.orderType = order.getOrderType();
        this.destination = order.getDestination();
        this.receiver = order.getReceiver();
        this.priority = order.getPriority();
        this.bluePrint = order.isBluePrint();
        this.orderStatus = order.getOrderStatus();
        this.createdBy = order.getCreatedBy();
        this.createdDate = order.getCreatedDate();
        this.updatedBy = order.getUpdatedBy();
        this.updatedDate = order.getUpdatedDate();
        this.fitId = order.getFitId();
        this.orderRights = order.getOrderRights();
        this.rightsholder = order.getRightsholder();
        this.inProgressCount = order.getInProgressCount();
        this.countReady = order.getCountReady();
        this.category = order.getCategory();
        this.finishBy = order.getFinishBy();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getItemName() {
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
