package com.azarenka.evebuilders.service.util;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class TelegramMessageCreatorService implements LocaleChangeObserver {

    private static final String FORMAT = "* %s:* %s\n";

    public static String createTakeOrderMessage(ShipOrderDto orderDto, int count, String username) {
        BigDecimal total = getOutcome(orderDto.getPrice(), count);
        var text = escapeMarkdownV2(
                String.format("* Заказ:* %s отдан в работу - %s", orderDto.getOrderNumber(), username + "\n") +
                        String.format(FORMAT, "Наименование", orderDto.getItemName()) +
                        String.format(FORMAT, "Количество", count) +
                        String.format(FORMAT, "Остаток свободных позиций в заказе", orderDto.getCount()
                                - orderDto.getInProgressCount()) +
                        String.format(FORMAT, "Цена за единицу", DecimalFormatter.formatIsk(orderDto.getPrice()) +
                                " " + DecimalFormatter.maybeToText(orderDto.getPrice())) +
                        String.format(FORMAT, "Цена за все", DecimalFormatter.formatIsk(total) +
                                " " + DecimalFormatter.maybeToText(total)) +
                        String.format(FORMAT, "Срок сдачи до", orderDto.getFinishBy()));
        return text.replace(".", "\\.");
    }

    public static String createFinishOrderMessage(DistributedOrder distributedOrder, int count, String username) {
        BigDecimal total = getOutcome(distributedOrder.getPrice(), count);
        StringBuilder builder = new StringBuilder();
        builder
                .append(String.format("* Отчет от - %s*\n", username))
                .append(String.format("* Заказ: %s*\n", distributedOrder.getOrderNumber()))
                .append(String.format(FORMAT, "Наименование", distributedOrder.getShipName()))
                .append(String.format("* Контракт на %s позиций*\n", count))
                .append(String.format("* Контракт %s*\n", DecimalFormatter.formatIsk(total) +
                        " " + DecimalFormatter.maybeToText(total)));
        if (distributedOrder.getCountReady().equals(distributedOrder.getCount())) {
            builder.append(String.format("* Заказ завершен полностью - %s шт*", distributedOrder.getCountReady()));
        } else {
            builder.append(String.format("* Остаток по заказу - %s шт*",
                    distributedOrder.getCount() - distributedOrder.getCountReady()));
        }
        var text = escapeMarkdownV2(builder.toString());
        return text.replace(".", "\\.");
    }

    public static String createOrderMessage(Order order) {
        return escapeMarkdownV2(String.format(FORMAT, "Заказ", order.getOrderNumber()) +
                String.format(FORMAT, "Наименование", order.getShipName()) +
                String.format(FORMAT, "Количество", order.getCount()) +
                String.format(FORMAT, "Цена за единицу", DecimalFormatter.formatIsk(order.getPrice())) +
                String.format(FORMAT, "Приоритет", order.getPriority()) +
                String.format(FORMAT, "Оснастка", "[Открыть заказ](https://industry.scan-stakan.com/)") +
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

    private static BigDecimal getOutcome(BigDecimal price, Integer count) {
        return price.multiply(new BigDecimal(count));
    }

    public static String createDiscardOrderMessage(DistributedOrder distributedOrder, String userName) {
        BigDecimal total = getOutcome(distributedOrder.getPrice(), distributedOrder.getCount());
        StringBuilder builder = new StringBuilder();
        builder
                .append(String.format("* Отчет от - %s*\n", userName))
                .append(String.format("* Заказ: %s - Отменен*\n", distributedOrder.getOrderNumber()))
                .append(String.format(FORMAT, "Наименование", distributedOrder.getShipName()))
                .append(String.format("* Контракт на %s позиций*\n", distributedOrder.getCount()))
                .append(String.format("* Контракт %s*\n", DecimalFormatter.formatIsk(total) +
                        " " + DecimalFormatter.maybeToText(total)))
                .append("** ОТМЕНА**");
        var text = escapeMarkdownV2(builder.toString());
        return text.replace(".", "\\.");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {

    }
}
