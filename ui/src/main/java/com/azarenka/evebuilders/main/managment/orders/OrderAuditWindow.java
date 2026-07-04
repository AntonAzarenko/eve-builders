package com.azarenka.evebuilders.main.managment.orders;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.db.OrderAudit;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.util.List;

public class OrderAuditWindow extends CommonDialogComponent implements LocaleChangeObserver {

    private Grid<OrderAudit> grid;
    private final ListDataProvider<OrderAudit> dataProvider;

    public OrderAuditWindow(List<OrderAudit> audits) {
        super("order-audit-window", true);
        dataProvider = DataProvider.ofCollection(audits);
        setHeaderTitle(getTranslation("window.header.order_audit"));
        setWidth("1200px");
        setHeight("600px");
        add(initContent());
        getFooter().add(initFooter());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        setHeaderTitle(getTranslation("window.header.order_audit"));
        grid.getColumns().get(0).setHeader(getTranslation("table.column.id"));
        grid.getColumns().get(1).setHeader(getTranslation("table.column.order_number"));
        grid.getColumns().get(2).setHeader(getTranslation("table.column.status"));
        grid.getColumns().get(3).setHeader(getTranslation("table.column.reason"));
        grid.getColumns().get(4).setHeader(getTranslation("table.column.created_by"));
        grid.getColumns().get(5).setHeader(getTranslation("table.column.created_date"));
        grid.getColumns().get(6).setHeader(getTranslation("table.column.updated_by"));
        grid.getColumns().get(7).setHeader(getTranslation("table.column.updated_date"));
    }

    private VerticalLayout initContent() {
        grid = VaadinUtils.initGrid(dataProvider, "order-audit-grid");
        addColumns();
        grid.getColumns().forEach(column -> {
            column.setSortable(true);
            column.setResizable(true);
        });

        VerticalLayout layout = VaadinUtils.initCommonVerticalLayout();
        layout.add(grid);
        return layout;
    }

    private HorizontalLayout initFooter() {
        Button closeButton = new Button(getTranslation("button.app.close"), event -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout footer = new HorizontalLayout(closeButton);
        footer.setWidthFull();
        return footer;
    }

    private void addColumns() {
        grid.addColumn(OrderAudit::getId);
        grid.addColumn(OrderAudit::getOrderNumber);
        grid.addColumn(value -> value.getStatus() == null ? "" : value.getStatus().name());
        grid.addColumn(OrderAudit::getReason);
        grid.addColumn(OrderAudit::getCreatedBy);
        grid.addColumn(value -> value.getCreatedDate() == null ? "" : value.getCreatedDate().toString());
        grid.addColumn(OrderAudit::getUpdatedBy);
        grid.addColumn(value -> value.getUpdatedDate() == null ? "" : value.getUpdatedDate().toString());
    }
}