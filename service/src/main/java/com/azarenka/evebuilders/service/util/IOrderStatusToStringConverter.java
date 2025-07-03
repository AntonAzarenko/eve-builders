package com.azarenka.evebuilders.service.util;

import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;

import org.apache.commons.lang3.StringUtils;

public interface IOrderStatusToStringConverter {

    default String convertStatus(RequestOrderStatusEnum statusEnum) {
        switch (statusEnum) {
            case CREATED -> {
                return "Создано";
            }
            case SUBMITTED -> {
                return "На рассмотрении";
            }
            case APPROVED -> {
                return "Принят";
            }
            case COMPLETED -> {
                return "Завершен";
            }
            case IN_PROGRESS -> {
                return "В работе";
            }
            case SUSPENDED -> {
                return "Приостановлен";
            }
            case REJECT -> {
                return "Отклонен";
            }
            default -> {
                return StringUtils.EMPTY;
            }
        }
    }
}
