package com.azarenka.evebuilders.service.util;

import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.domain.mysql.Order;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.math.BigDecimal;

public class TelegramMessageCreatorService implements LocaleChangeObserver {

    private static final String FORMAT = "* %s:* %s\n";

    public static String createTakeOrderMessage(ShipOrderDto shipOrder, int count,String username) {
        var texrt = escapeMarkdownV2(
                String.format("* Заказ:* %s отдан в работу - %s", shipOrder.getOrderNumber(), username) +
                        String.format(FORMAT, "Наименование", shipOrder.getShipName()) +
                        String.format(FORMAT, "Количество", count) +
                        String.format(FORMAT, "Остаток свободных кораблей в заказе", shipOrder.getCount()
                                - shipOrder.getInProgressCount()) +
                        String.format(FORMAT, "Цена за единицу", shipOrder.getPrice()) +
                        String.format(FORMAT, "Цена за все",
                                shipOrder.getPrice().multiply(new BigDecimal(shipOrder.getCount()))) +
                        String.format(FORMAT, "Срок сдачи до", shipOrder.getFinishBy()));
        return texrt.replace(".", "\\.");
    }

    public static String createOrderMessage(Order order) {
        return escapeMarkdownV2(String.format(FORMAT, "Заказ", order.getOrderNumber()) +
                String.format(FORMAT, "Наименование", order.getShipName()) +
                String.format(FORMAT, "Количество", order.getCount()) +
                String.format(FORMAT, "Цена за единицу", order.getPrice()) +
                String.format(FORMAT, "Приоритет", order.getPriority()) +
                String.format(FORMAT, "Оснастка", "[Открыть заказ](https://www.google.com)") +
                String.format(FORMAT, "Срок сдачи до", order.getFinishBy()));

    }

    private static String escapeMarkdownV2(String text) {
        return text
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {

    }
}
