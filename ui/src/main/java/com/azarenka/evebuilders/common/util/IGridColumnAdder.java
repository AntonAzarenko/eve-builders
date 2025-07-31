package com.azarenka.evebuilders.common.util;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.function.ValueProvider;

import java.math.BigDecimal;

public interface IGridColumnAdder<T> {

    default  Grid.Column<T> addAmountColumn(ValueProvider<T, ?> provider, String width, Grid<T> grid) {
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
}
