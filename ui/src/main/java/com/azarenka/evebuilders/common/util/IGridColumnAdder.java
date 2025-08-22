package com.azarenka.evebuilders.common.util;

import static com.azarenka.evebuilders.domain.OrderStatusEnum.DISTRIBUTED;
import static com.azarenka.evebuilders.domain.OrderStatusEnum.NEW;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.service.util.IOrderStatusToStringConverter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

public interface IGridColumnAdder<T> extends IOrderStatusToStringConverter {

    default Grid.Column<T> addAmountColumn(ValueProvider<T, ?> provider, String width, Grid<T> grid) {
        Grid.Column<T> column = grid.addColumn(provider);
        column.setWidth(width);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    default Column<T> addIntegerColumn(ValueProvider<T, Integer> provider, String width,
                                       Grid<T> grid) {
        Grid.Column<T> column = grid.addColumn(provider);
        column.setWidth(width);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    default Grid.Column<T> addDoubleColumn(ValueProvider<T, Double> provider, String width,
                                           Grid<T> grid) {
        Grid.Column<T> column = grid.addColumn(provider);
        column.setWidth(width);
        column.setTextAlign(ColumnTextAlign.END);
        return column;
    }

    default Grid.Column<T> addColumn(ValueProvider<T, String> provider, String width, Grid<T> grid) {
        Grid.Column<T> column = grid.addColumn(provider);
        column.setWidth(width);
        return column;
    }

    default Grid.Column<T> addComponentColumn(ValueProvider<T, Component> provider,
                                              String width, Grid<T> grid) {
        Grid.Column<T> column = grid.addComponentColumn(provider);
        column.setWidth(width);
        return column;
    }

    default Grid.Column<T> addBadgeColumn(ValueProvider<T, Component> provider,
                                          String width, Grid<T> grid, Function<T, String> function) {
        Grid.Column<T> column = grid.addColumn(new ComponentRenderer(provider));
        column.setWidth(width);
        column.setComparator(Comparator.comparing(
            function,
            java.text.Collator.getInstance(Locale.getDefault())
        ));
        return column;
    }

    default Span badge(OrderStatusEnum status) {
        var s = new Span(convertOrderStatus(status));
        s.getElement().getThemeList().add("badge");
        switch (status) {
            case NEW -> s.getElement().getThemeList().add("badge error");
            case IN_PROGRESS -> s.getElement().getThemeList().add("badge success");
            case DISTRIBUTED -> s.getElement().getThemeList().add("badge primary");
            case COMPLETED -> s.getElement().getThemeList().add("badge");
            case ARCHIVED -> s.getElement().getThemeList().add("badge contrast");
        }
        return s;
    }

    default Span badge(RequestOrderStatusEnum status) {
        var s = new Span(convertRequestStatus(status));
        s.getElement().getThemeList().add("badge");
        switch (status) {
            case CREATED -> s.getElement().getThemeList().add("badge error");
            case IN_PROGRESS -> s.getElement().getThemeList().add("badge success");
            case SUBMITTED -> s.getElement().getThemeList().add("badge primary");
            case COMPLETED -> s.getElement().getThemeList().add("badge");
            case ARCHIVED -> s.getElement().getThemeList().add("badge contrast");
        }
        return s;
    }
}
