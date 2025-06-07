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
@Table(name = "distributed_oder", schema = "builders")
public class DistributedOrder {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "order_number")
    private String orderNumber;
    @Column(name = "ship_name")
    private String shipName;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "count")
    private Integer count;
    @Column(name = "fit_id")
    private String fitId;
    @Column(name = "order_rights")
    private OrderRights orderRights;
    @Column(name = "ready", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer countReady;
    @Column(name="status")
    private OrderStatusEnum orderStatus;
    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();
    @Column(name = "finished_date")
    private LocalDate finishedDate;
    @Column(name = "category")
    private String category;
    @Column(name = "price")
    private BigDecimal price;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(LocalDate finishedDate) {
        this.finishedDate = finishedDate;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
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

    public Integer getCountReady() {
        return countReady;
    }

    public void setCountReady(Integer countReady) {
        this.countReady = countReady;
    }

    public OrderStatusEnum getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatusEnum orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}
