package com.azarenka.evebuilders.main.request.admin;

import com.azarenka.evebuilders.common.util.ISaveListener;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.constructions.api.ICorporationConstructionController;
import com.azarenka.evebuilders.main.request.api.IRequestsController;
import com.azarenka.evebuilders.validators.RequiredValidator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SubmitRequestOrderWindow extends CommonDialogComponent implements LocaleChangeObserver {

    private static final String REQUIRED_FIELD_VALUE = "error.message.required_value";

    private BigDecimalField cost;
    private final Binder<Integer> binder = new Binder<>();
    private final String headerTitle = getTranslation("window.header.submit.request");
    private final String headerLabel = getTranslation("label.submit.request.cost");

    private final RequestOrder order;
    private final IRequestsController controller;
    private final ISaveListener listener;

    public SubmitRequestOrderWindow(RequestOrder order, IRequestsController controller,
                                    ISaveListener listener) {
        this.order = order;
        this.listener = listener;
        this.controller = controller;
        setWidth("300px");
        setHeaderTitle(headerTitle);
        add(initContent());
        getFooter().add(iniButtonsLayout(), createCloseButton());
        VaadinUtils.addComponentId(this, "submit-request-window");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        cost.setLabel(getTranslation("label.submit.request.cost"));
    }

    private VerticalLayout initContent() {
        cost = new BigDecimalField();
        cost.setLabel(headerLabel);
        cost.setWidthFull();
        binder.forField(cost)
            .withValidator(new RequiredValidator(REQUIRED_FIELD_VALUE))
            .bind(value -> BigDecimal.valueOf(value), (bean, fieldValue) -> fieldValue.toString());
        VerticalLayout layout = VaadinUtils.initCommonVerticalLayout();
        layout.setWidthFull();
        layout.add(cost);
        return layout;
    }

    private Button iniButtonsLayout() {
        Button button = new Button(VaadinIcon.PACKAGE.create(), event -> {
            if (binder.validate().isOk()) {
                order.setPrice(cost.getValue());
                order.setRequestStatus(RequestOrderStatusEnum.SUBMITTED);
                controller.updateRequest(order);
                this.close();
            }
        });
        button.addClickListener(listener);
        return button;
    }
}
