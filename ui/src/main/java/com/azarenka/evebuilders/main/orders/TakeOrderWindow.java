package com.azarenka.evebuilders.main.orders;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.commonview.FitView;
import com.azarenka.evebuilders.main.orders.api.IOrderViewController;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TakeOrderWindow extends CommonDialogComponent implements LocaleChangeObserver {

    private String HEADER_WINDOW = getTranslation("window.header.take_an_order");
    private final Set<ShipOrderDto> selectedItems;
    private final Map<ShipOrderDto, IntegerField> orderTexfieldMap = new HashMap<>();
    private final IOrderViewController controller;
    private Binder<Integer> binder = new Binder<>();
    private ComponentEventListener<ClickEvent<Button>> clickListener;

    private Button addButton;

    public TakeOrderWindow(Set<ShipOrderDto> selectedItems, IOrderViewController controller,
                           ComponentEventListener<ClickEvent<Button>> clickListener) {
        this.selectedItems = selectedItems;
        this.controller = controller;
        this.clickListener = clickListener;
        super.setHeaderTitle(HEADER_WINDOW);
        super.setWidth("500px");
        super.applyCommonProperties("take-order-modal-window", false);
        initContent();
        initButtonsLayout();
        getFooter().add(addButton, createCloseButton());
    }

    private void initButtonsLayout() {
        addButton = new Button(VaadinIcon.CART.create(), event -> {
            if (isValid()) {
                controller.saveOrders(orderTexfieldMap);
                this.close();
            }
        });
        addButton.addClickListener(clickListener);
    }

    private boolean isValid() {
        return binder.validate().isOk();
    }

    private void initContent() {
        add(initMainLayout());
    }

    private VerticalLayout initMainLayout() {
        var mainLayout = VaadinUtils.initCommonVerticalLayout();
        selectedItems.forEach(shipOrderDto -> {
            mainLayout.add(initOrderLayout(shipOrderDto));
            mainLayout.add(new Hr());
        });
        return mainLayout;
    }

    private VerticalLayout initOrderLayout(ShipOrderDto shipOrderDto) {
        IntegerField countTextField = new IntegerField();
        binder.forField(countTextField)
                .withValidator(integer ->
                                integer <= (shipOrderDto.getCount() - shipOrderDto.getInProgressCount()),
                        String.format(getTranslation("errors.message.count_orders_not_available"),
                                shipOrderDto.getCount() - shipOrderDto.getInProgressCount()))
                .withValidator(integer -> integer > 0, getTranslation("errors.message.filed_positive_zero_not_allowed"))
                .bind(e -> e, (s, r) -> r.intValue());
        orderTexfieldMap.put(shipOrderDto, countTextField);
        VerticalLayout layout = VaadinUtils.initCommonVerticalLayout();
        Button showFitButton = new Button(VaadinIcon.EYE.create(), event -> {
            Fit fitById = controller.getFitById(shipOrderDto.getFitId());
            if (Objects.nonNull(fitById)) {
                new FitView(fitById, controller.getFitLoaderService()).open();
            }
        });
        layout.add(
                initNextLayout(getTranslation("table.column.order_number"), shipOrderDto.getOrderNumber()),
                initNextLayout(getTranslation("table.column.nomination"), shipOrderDto.getItemName()),
                initNextLayout(getTranslation("table.column.price"), shipOrderDto.getPrice().toString()),
                initNextLayout(getTranslation("management.label.fit"), showFitButton),
                initNextLayout(getTranslation("management.label.order_free"),
                        String.valueOf(shipOrderDto.getCount() - shipOrderDto.getInProgressCount())),
                initNextLayout(getTranslation("table.column.count"), countTextField)
        );
        return layout;
    }

    private HorizontalLayout initNextLayout(String label, Object value) {
        HorizontalLayout layout;
        Span span = new Span(String.format("%s: ", label));
        span.setWidth("50%");
        if (value instanceof String) {
            layout = new HorizontalLayout(span, new Span((String) value));
        } else if (value instanceof IntegerField) {
            IntegerField textField = (IntegerField) value;
            textField.setWidth("48%");
            layout = new HorizontalLayout(span, textField);
        } else {
            Button button = (Button) value;
            layout = new HorizontalLayout(span, button);
        }
        layout.setWidthFull();
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        return layout;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {

    }
}
