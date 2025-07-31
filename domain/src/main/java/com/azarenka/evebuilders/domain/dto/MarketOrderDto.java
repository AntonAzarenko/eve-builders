package com.azarenka.evebuilders.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class MarketOrderDto {

    @JsonProperty("type_id")
    private Integer typeId;

    @JsonProperty("is_buy_order")
    private boolean isBuyOrder;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("volume_remain")
    private Long volumeRemain;

    @JsonProperty("issued")
    private String issued;

    @JsonProperty("system_id")
    private Long systemId;

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public boolean isBuyOrder() {
        return isBuyOrder;
    }

    public void setBuyOrder(boolean buyOrder) {
        isBuyOrder = buyOrder;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getVolumeRemain() {
        return volumeRemain;
    }

    public void setVolumeRemain(Long volumeRemain) {
        this.volumeRemain = volumeRemain;
    }

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }
}
