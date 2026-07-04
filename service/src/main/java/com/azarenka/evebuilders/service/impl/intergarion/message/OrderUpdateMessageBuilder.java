package com.azarenka.evebuilders.service.impl.intergarion.message;

import com.azarenka.evebuilders.domain.db.Order;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.StringJoiner;

public final class OrderUpdateMessageBuilder {

    private OrderUpdateMessageBuilder() {
    }

    public static String build(Order previousOrder, Order updatedOrder) {
        if (updatedOrder == null) {
            return "Заказ был изменен администратором\nИзмененные поля:\n- Нет данных по заказу";
        }

        String orderNumber = valueOrDefault(updatedOrder.getOrderNumber(), "<неизвестно>");
        String who = valueOrDefault(updatedOrder.getUpdatedBy(), "администратором");
        StringJoiner changes = new StringJoiner("\n");

        if (previousOrder != null) {
            addChange(changes, "Категория", previousOrder.getCategory(), updatedOrder.getCategory());
            addChange(changes, "Группа", previousOrder.getGroupName(), updatedOrder.getGroupName());
            addChange(changes, "Наименование", previousOrder.getShipName(), updatedOrder.getShipName());
            addChange(changes, "Количество", previousOrder.getCount(), updatedOrder.getCount());
            addChange(changes, "Цена", previousOrder.getPrice(), updatedOrder.getPrice());
            addChange(changes, "Тип", previousOrder.getOrderType(), updatedOrder.getOrderType());
            addChange(changes, "Доставка", previousOrder.getDestination(), updatedOrder.getDestination());
            addChange(changes, "Тип приемщика", previousOrder.getReceiverType(), updatedOrder.getReceiverType());
            addChange(changes, "ID приемщика", previousOrder.getReceiverRefId(), updatedOrder.getReceiverRefId());
            addChange(changes, "Приемщик", previousOrder.getReceiverName(), updatedOrder.getReceiverName());
            addChange(changes, "Приоритет", previousOrder.getPriority(), updatedOrder.getPriority());
            addChange(changes, "Чертеж", previousOrder.isBluePrint(), updatedOrder.isBluePrint());
            addChange(changes, "Фит", previousOrder.getFitId(), updatedOrder.getFitId());
            addChange(changes, "Права", previousOrder.getOrderRights(), updatedOrder.getOrderRights());
            addChange(changes, "Правообладатель", previousOrder.getRightsholder(), updatedOrder.getRightsholder());
            addChange(changes, "Срок сдачи", previousOrder.getFinishBy(), updatedOrder.getFinishBy());
        }

        if (changes.length() == 0) {
            changes.add("- Изменений не обнаружено");
        }

        return String.format("Заказ %s был изменен %s\nИзмененные поля:\n%s", orderNumber, who, changes);
    }

    private static void addChange(StringJoiner changes, String field, Object oldValue, Object newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        changes.add(String.format("- %s: %s -> %s", field, formatValue(oldValue), formatValue(newValue)));
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "<пусто>";
        }
        if (value instanceof String stringValue && stringValue.isBlank()) {
            return "<пусто>";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        return value.toString();
    }

    private static String valueOrDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}