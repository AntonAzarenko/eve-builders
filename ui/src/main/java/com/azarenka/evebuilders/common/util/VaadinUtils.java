package com.azarenka.evebuilders.common.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.List;
import java.util.stream.IntStream;

public class VaadinUtils {

    public static void addComponentId(Component component, String id) {
        if (component.getId().isEmpty()) {
            component.setId(id);
        }
        component.addClassName(id);
    }

    public static void addComponentIds(Component component, String... ids) {
        if (component.getId().isEmpty()) {
            IntStream.range(0, ids.length).forEach(i -> {
                component.addClassName(ids[i]);
                component.setId(ids[i]);
            });
        }
        IntStream.range(0, ids.length).forEach(i -> {
            component.addClassName(ids[i]);
        });
    }

    public static VerticalLayout initCommonVerticalLayout() {
        var verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(false);
        verticalLayout.setMargin(false);
        verticalLayout.setSpacing(false);
        verticalLayout.setSizeFull();
        return verticalLayout;
    }

    public static void setPaddingZero(Component... components) {
        IntStream.range(0, components.length).forEach(i -> components[i].getStyle().setPadding("0"));
    }

    public static void setPadding(String padding, Component... components) {
        IntStream.range(0, components.length).forEach(i -> components[i].getStyle().setPadding(padding));
    }

    public static <T> Grid<T> initGrid(List<T> collectionItems, String id) {
        Grid<T> grid = new Grid<>();
        grid.setItems(collectionItems);
        applyGridProperties(grid, id);
        return grid;
    }

    public static <T> Grid<T> initGrid(String id) {
        Grid<T> grid = new Grid<>();
        applyGridProperties(grid, id);
        return grid;
    }

    public static <T> Grid<T> initGrid(ListDataProvider<T> dataProvider, String id) {
        Grid<T> grid = new Grid<>();
        grid.setItems(dataProvider);
        applyGridProperties(grid, id);
        return grid;
    }

    private static <T> void applyGridProperties(Grid<T> grid, String id) {
        grid.addThemeVariants(
                GridVariant.LUMO_COLUMN_BORDERS,
                GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
        VaadinUtils.addComponentId(grid, id);
    }
}
