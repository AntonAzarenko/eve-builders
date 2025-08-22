package com.azarenka.evebuilders.service.util;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.internal.LocaleUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public interface IOrderStatusToStringConverter extends LocaleChangeObserver {

    default String convertRequestStatus(RequestOrderStatusEnum statusEnum) {
        switch (statusEnum) {
            case CREATED -> {
                return getTranslation("label.created");
            }
            case SUBMITTED -> {
                return getTranslation("label.submitted");
            }
            case APPROVED -> {
                return getTranslation("label.approved");
            }
            case COMPLETED -> {
                return getTranslation("label.completed");
            }
            case IN_PROGRESS -> {
                return getTranslation("label.in_progress");
            }
            case SUSPENDED -> {
                return getTranslation("label.suspended");
            }
            case REJECTED -> {
                return getTranslation("label.rejected");
            }
            case ARCHIVED -> {
                return getTranslation("label.archived");
            }
            default -> {
                return StringUtils.EMPTY;
            }
        }
    }

    default String convertOrderStatus(OrderStatusEnum statusEnum) {
        switch (statusEnum) {
            case NEW -> {
                return getTranslation("label.new");
            }
            case IN_PROGRESS -> {
                return getTranslation("label.in_progress");
            }
            case DISTRIBUTED -> {
                return getTranslation("label.distributed");
            }
            case COMPLETED -> {
                return getTranslation("label.completed");
            }
            case DISCARDED -> {
                return getTranslation("label.discarded");
            }
            case WAITING_FOR_APPROVAL -> {
                return getTranslation("label.waiting_for_approval");
            }
            case EXPIRED -> {
                return getTranslation("label.expired");
            }
            case STOPPED -> {
                return getTranslation("label.stopped");
            }
            case ARCHIVED -> {
                return getTranslation("label.archived");
            }
            default -> {
                return StringUtils.EMPTY;
            }
        }
    }

    default void localeChange(LocaleChangeEvent event) {

    }

    default String getTranslation(String key, Object... params) {
        final Optional<I18NProvider> i18NProvider = LocaleUtil
            .getI18NProvider();
        return i18NProvider
            .map(i18n -> i18n.getTranslation(key,
                LocaleUtil.getLocale(() -> i18NProvider), params))
            .orElseGet(() -> "!{" + key + "}!");
    }
}
