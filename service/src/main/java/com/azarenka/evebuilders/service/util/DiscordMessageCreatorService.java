package com.azarenka.evebuilders.service.util;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;

import java.math.BigDecimal;

public final class DiscordMessageCreatorService {

    private static final String FORMAT = "- %s: %s%n";
    private static final int DISCORD_CONTENT_LIMIT = 2000;

    private DiscordMessageCreatorService() {
    }

    public static String createOrderMessage(Order order) {
        String message = String.format(FORMAT, "Заказ", order.getOrderNumber())
            + String.format(FORMAT, "Наименование", order.getShipName())
            + String.format(FORMAT, "Количество", order.getCount())
            + String.format(FORMAT, "Цена за единицу", DecimalFormatter.formatDecimalValueForMessage(order.getPrice()))
            + String.format(FORMAT, "Приоритет", order.getPriority())
            + String.format(FORMAT, "Оснастка", "https://industry.scan-stakan.com/")
            + String.format(FORMAT, "Срок сдачи до", order.getFinishBy());
        return trimToDiscordLimit(message);
    }

    public static String createOrderRemovedMessage(String orderNumber) {
        return trimToDiscordLimit(String.format("Заказ %s был удален", orderNumber));
    }

    public static String createTakeOrderMessage(ShipOrderDto orderDto, int count, String userName) {
        BigDecimal total = getOutcome(orderDto.getPrice(), count);
        String message = String.format("Заказ %s отдан в работу - %s%n", orderDto.getOrderNumber(), userName)
            + String.format(FORMAT, "Наименование", orderDto.getItemName())
            + String.format(FORMAT, "Количество", count)
            + String.format(FORMAT, "Остаток свободных позиций в заказе", orderDto.getCount() - orderDto.getInProgressCount())
            + String.format(FORMAT, "Цена за единицу",
            DecimalFormatter.formatDecimalValueForMessage(orderDto.getPrice()) + " " + DecimalFormatter.maybeToText(orderDto.getPrice()))
            + String.format(FORMAT, "Цена за все",
            DecimalFormatter.formatDecimalValueForMessage(total) + " " + DecimalFormatter.maybeToText(total))
            + String.format(FORMAT, "Срок сдачи до", orderDto.getFinishDate());
        return trimToDiscordLimit(message);
    }

    public static String createFinishOrderMessage(DistributedOrder distributedOrder, int readyCount, String userName) {
        BigDecimal total = getOutcome(distributedOrder.getPrice(), readyCount);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Отчет от - %s%n", userName))
            .append(String.format("Заказ: %s%n", distributedOrder.getOrderNumber()))
            .append(String.format(FORMAT, "Наименование", distributedOrder.getShipName()))
            .append(String.format("Контракт на %s позиций%n", readyCount))
            .append(String.format("Контракт %s%n",
                DecimalFormatter.formatDecimalValueForMessage(total) + " " + DecimalFormatter.maybeToText(total)));
        if (distributedOrder.getCountReady().equals(distributedOrder.getCount())) {
            builder.append(String.format("Заказ завершен полностью - %s шт", distributedOrder.getCountReady()));
        } else {
            builder.append(String.format("Остаток по заказу - %s шт", distributedOrder.getCount() - distributedOrder.getCountReady()));
        }
        return trimToDiscordLimit(builder.toString());
    }

    public static String createWaitingForApprovalMessage(DistributedOrder distributedOrder, String userName) {
        String message = String.format("Отчет от - %s%n", userName)
            + String.format("Заказ: %s%n", distributedOrder.getOrderNumber())
            + String.format(FORMAT, "Наименование", distributedOrder.getShipName())
            + String.format(FORMAT, "СТАТУС", "ОЖИДАЕТ ВАЛИДАЦИИ КОНТРАКТА");
        return trimToDiscordLimit(message);
    }

    public static String createDiscardOrderMessage(DistributedOrder distributedOrder, String userName) {
        BigDecimal total = getOutcome(distributedOrder.getPrice(), distributedOrder.getCount());
        String message = String.format("Отчет от - %s%n", userName)
            + String.format("Заказ: %s - Отменен%n", distributedOrder.getOrderNumber())
            + String.format(FORMAT, "Наименование", distributedOrder.getShipName())
            + String.format("Контракт на %s позиций%n", distributedOrder.getCount())
            + String.format("Контракт %s%n",
            DecimalFormatter.formatDecimalValueForMessage(total) + " " + DecimalFormatter.maybeToText(total))
            + "ОТМЕНА";
        return trimToDiscordLimit(message);
    }

    private static BigDecimal getOutcome(BigDecimal price, Integer count) {
        return price.multiply(new BigDecimal(count));
    }

    private static String trimToDiscordLimit(String value) {
        if (value == null || value.length() <= DISCORD_CONTENT_LIMIT) {
            return value;
        }
        return value.substring(0, DISCORD_CONTENT_LIMIT - 3) + "...";
    }
}

