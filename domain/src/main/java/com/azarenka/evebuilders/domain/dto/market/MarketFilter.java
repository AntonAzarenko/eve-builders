package com.azarenka.evebuilders.domain.dto.market;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MarketFilter {

    private String resource;
    private String location;
    private Long minQty;
    private Long maxQty;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private LocalDate minDeadline;
    private LocalDate maxDeadline;
    private String status;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getMinQty() {
        return minQty;
    }

    public void setMinQty(Long minQty) {
        this.minQty = minQty;
    }

    public Long getMaxQty() {
        return maxQty;
    }

    public void setMaxQty(Long maxQty) {
        this.maxQty = maxQty;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public LocalDate getMinDeadline() {
        return minDeadline;
    }

    public void setMinDeadline(LocalDate minDeadline) {
        this.minDeadline = minDeadline;
    }

    public LocalDate getMaxDeadline() {
        return maxDeadline;
    }

    public void setMaxDeadline(LocalDate maxDeadline) {
        this.maxDeadline = maxDeadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
