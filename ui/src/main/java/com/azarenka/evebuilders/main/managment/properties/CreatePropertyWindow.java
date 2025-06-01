package com.azarenka.evebuilders.main.managment.properties;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.util.function.Consumer;

public class CreatePropertyWindow extends CommonDialogComponent implements LocaleChangeObserver {

    private TextField textField;
    private final Binder<String> binder = new Binder<>();
    private final String label;
    private ComponentEventListener<ClickEvent<Button>> clickListener;
    private Button apply;

    public CreatePropertyWindow(String header, String label,
                                ComponentEventListener<ClickEvent<Button>> clickListener) {
        super.setWidth("300px");
        super.setHeaderTitle(header);
        this.label = label;
        this.clickListener = clickListener;
        add(initContent());
        getFooter().add(iniButtonsLayout(), createCloseButton());
        VaadinUtils.addComponentId(this, "create-property-window");
    }

    private VerticalLayout initContent() {
        textField = new TextField();
        textField.setLabel(label);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        binder.forField(textField)
                .withValidator(value -> !value.isBlank(), getTranslation("errors.message.field_empty"))
                .bind(value -> value, (bean, fieldValue) -> fieldValue.toString());
        VerticalLayout layout = VaadinUtils.initCommonVerticalLayout();
        layout.setWidthFull();
        layout.add(textField);
        return layout;
    }

    public void setClickListener(Consumer<String> consumer) {
        apply.addClickListener(event -> {
            if (binder.validate().isOk()) {
                consumer.accept(textField.getValue());
                super.close();
            }
        });
        apply.addClickListener(clickListener);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {

    }

    public TextField getTextField() {
        return textField;
    }

    private Button iniButtonsLayout() {
        apply = new Button(VaadinIcon.PACKAGE.create());
        apply.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        return apply;
    }
}
