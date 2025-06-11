package com.azarenka.evebuilders.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.*;
import jakarta.servlet.http.HttpServletResponse;

@Route("access-denied")
@PageTitle("access-denied")
public class AccessDeniedView extends VerticalLayout implements LocaleChangeObserver, HasErrorParameter<AccessDeniedException> {

    public AccessDeniedView() {
        add(new Button(VaadinIcon.BACKSPACE.create(), event -> UI.getCurrent().navigate("/orders/default")));
        add(new H1(getTranslation("errors.message.access_denied")));
        add(new Paragraph(getTranslation("errors.message.access_denied.message")));
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {

    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        return HttpServletResponse.SC_FORBIDDEN;
    }
}
