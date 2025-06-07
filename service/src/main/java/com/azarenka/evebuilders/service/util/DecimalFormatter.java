package com.azarenka.evebuilders.service.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

public class DecimalFormatter {

    public static String formatIsk(BigDecimal value) {
        if (Objects.isNull(value)) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "RU"));
        symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
        return df.format(value) + " ISK";
    }

    public static String maybeToText(BigDecimal value) {
        if (value == null) {
            return "0";
        }

        BigDecimal billion = new BigDecimal("1000000000");
        BigDecimal million = new BigDecimal("1000000");
        BigDecimal thousand = new BigDecimal("1000");

        if (value.compareTo(billion) >= 0) {
            BigDecimal amount = value.divide(billion, 1, RoundingMode.DOWN);
            return format(amount) + " " + plural(amount, "миллиард", "миллиарда", "миллиардов");
        } else if (value.compareTo(million) >= 0) {
            BigDecimal amount = value.divide(million, 1, RoundingMode.DOWN);
            return format(amount) + " " + plural(amount, "миллион", "миллиона", "миллионов");
        } else if (value.compareTo(thousand) >= 0) {
            BigDecimal amount = value.divide(thousand, 1, RoundingMode.DOWN);
            return format(amount) + " " + plural(amount, "тысяча", "тысячи", "тысяч");
        } else {
            return format(value);
        }
    }

    private static String format(BigDecimal value) {
        value = value.stripTrailingZeros();
        if (value.scale() <= 0) {
            return value.setScale(0, RoundingMode.DOWN).toPlainString();
        } else {
            return value.setScale(1, RoundingMode.DOWN).toPlainString();
        }
    }

    private static String plural(BigDecimal number, String one, String few, String many) {
        int n = number.setScale(0, RoundingMode.DOWN).intValue();
        int lastDigit = n % 10;
        int lastTwoDigits = n % 100;

        if (lastDigit == 1 && lastTwoDigits != 11) {
            return one;
        } else if (lastDigit >= 2 && lastDigit <= 4 && !(lastTwoDigits >= 12 && lastTwoDigits <= 14)) {
            return few;
        } else {
            return many;
        }
    }
}
