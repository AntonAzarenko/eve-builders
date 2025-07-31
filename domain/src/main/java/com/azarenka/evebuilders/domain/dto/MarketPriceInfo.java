package com.azarenka.evebuilders.domain.dto;

import java.math.BigDecimal;

public class MarketPriceInfo {

    private Integer typeId;
    private BigDecimal buyPrice;     // highest buy
    private BigDecimal sellPrice;    // lowest sell

    public MarketPriceInfo(Integer typeId, BigDecimal buyPrice, BigDecimal sellPrice) {
        this.typeId = typeId;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }
}
