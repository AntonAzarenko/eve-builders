package com.azarenka.evebuilders.security;

import com.azarenka.evebuilders.main.orders.corporation.OrdersView;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AccessDeniedException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;

@Route("access-denied")
@PageTitle("access-denied")
public class AccessDeniedView extends VerticalLayout implements HasErrorParameter<AccessDeniedException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessDeniedView.class);

    public AccessDeniedView() {
        add(new Button("Go to main menu",VaadinIcon.BACKSPACE.create(),event -> UI.getCurrent().navigate(OrdersView.class)));
        add(new H1(getTranslation("errors.message.access_denied")));
        add(new Paragraph(getTranslation("errors.message.access_denied.message")));
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        var attemptedRoute = "https://industry.scan-stakan.com/" + event.getLocation().getPath();
        var userName = SecurityUtils.getUserName();
        LOGGER.warn("Access denied for user '{}' on route: {}", userName, attemptedRoute);

        if(event.getLocation().getPath().isEmpty()) {
            event.rerouteTo(OrdersView.class);
            return HttpServletResponse.SC_OK;
        }
        return HttpServletResponse.SC_FORBIDDEN;
    }
}
