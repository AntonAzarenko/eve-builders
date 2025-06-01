package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.component.SearchComponent;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.main.managment.api.ICreateOrderController;
import com.azarenka.evebuilders.main.managment.page.AddOrderPage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "default", layout = AddOrderPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
@PermitAll
public class CreateOrderView extends View implements LocaleChangeObserver {

    private final ICreateOrderController controller;
    private SplitLayout splitLayout;
    private SearchComponent searchField;

    public CreateOrderView(ICreateOrderController controller) {
        this.controller = controller;
        super.setPadding(true);
        initSplitLayout();
        add(initHeaderLayout(), splitLayout);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {

    }

    private HorizontalLayout initHeaderLayout() {
        searchField = new SearchComponent(getTranslation("management.search.placeholder"),
                event -> searchByText(searchField.getValue()),
                event -> clearSearch()
        );
        searchField.setWidth("40%");
        var headerTitle = new HorizontalLayout(searchField, new Button(VaadinIcon.REFRESH.create(), event -> refresh()));
        headerTitle.setWidthFull();
        headerTitle.setVerticalComponentAlignment(Alignment.STRETCH);
        headerTitle.setAlignItems(Alignment.CENTER);
        add(headerTitle);
        return headerTitle;
    }

    private void initSplitLayout() {
        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.addToPrimary(new ParametersOrderView(controller));
        splitLayout.setSplitterPosition(50);
        splitLayout.addToSecondary(new ExistingOrdersView(controller));
    }

    private void refresh() {
        //todo implement later
    }

    private void searchByText(String value) {
      //todo implement later
    }

    private void clearSearch() {
        searchField.clearText();
        searchByText("");
    }
}
