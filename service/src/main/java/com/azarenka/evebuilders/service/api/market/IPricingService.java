package com.azarenka.evebuilders.service.api.market;

import java.math.BigDecimal;

public interface IPricingService {

    BigDecimal getReferencePricePerUnit(String typeId);

    boolean isPriceOutlier(String typeId, BigDecimal pricePerUnit, double thresholdPercent);
}
