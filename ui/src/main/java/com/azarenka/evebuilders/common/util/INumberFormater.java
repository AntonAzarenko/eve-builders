package com.azarenka.evebuilders.common.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public interface INumberFormater {

    default String formatNumber(int value) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);
        return decimalFormat.format(value);
    }

    default String formatNumber(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);
        return decimalFormat.format(value);
    }

    default String formatNumber(BigDecimal value) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);
        return decimalFormat.format(value);
    }
}
