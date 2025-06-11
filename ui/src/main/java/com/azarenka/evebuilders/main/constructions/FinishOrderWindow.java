package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.common.util.ISaveListener;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.constructions.api.ICorporationConstructionController;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

public class FinishOrderWindow extends CommonDialogComponent implements LocaleChangeObserver {

    private IntegerField countReadyShips;
    private final Binder<Integer> binder = new Binder<>();
    private final String headerTitle = getTranslation("window.header.finish.order");
    private final String headerLabel = getTranslation("label.finish_order.count");

    private final DistributedOrder order;
    private final ICorporationConstructionController controller;
    private final ISaveListener listener;

    public FinishOrderWindow(DistributedOrder order, ICorporationConstructionController controller,
                             ISaveListener listener) {
        this.order = order;
        this.listener = listener;
        this.controller = controller;
        setWidth("300px");
        setHeaderTitle(headerTitle);
        add(initContent());
        getFooter().add(iniButtonsLayout(), createCloseButton());
        VaadinUtils.addComponentId(this, "finish-order-window");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        countReadyShips.setLabel(getTranslation("label.finish_order.count"));
    }

    private VerticalLayout initContent() {
        countReadyShips = new IntegerField();
        countReadyShips.setLabel(headerLabel);
        binder.forField(countReadyShips)
                .withValidator(value -> value > 0,
                        String.format(getTranslation("errors.message.value_between"), 1,
                                (order.getCount() - order.getCountReady())))
                .withValidator(value -> (order.getCount() - order.getCountReady()) >= value,
                        String.format(getTranslation("errors.message.value_between"), 1,
                                (order.getCount() - order.getCountReady())))
                .bind(value -> value, (bean, fieldValue) -> fieldValue.toString());
        VerticalLayout layout = VaadinUtils.initCommonVerticalLayout();
        layout.setWidthFull();
        layout.add(countReadyShips);
        return layout;
    }

    private Button iniButtonsLayout() {
        Button button = new Button(VaadinIcon.PACKAGE.create(), event -> {
            if (binder.validate().isOk()) {
                controller.saveOrder(order, countReadyShips.getValue());
                this.close();
            }
        });
        button.addClickListener(listener);
        return button;
    }
}
