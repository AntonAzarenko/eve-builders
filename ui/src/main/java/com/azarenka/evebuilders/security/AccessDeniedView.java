package com.azarenka.evebuilders.security;

import com.azarenka.evebuilders.main.MainWidget;
import com.azarenka.evebuilders.main.orders.corporation.OrdersView;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AccessDeniedException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletResponse;

public class AccessDeniedView extends VerticalLayout implements HasErrorParameter<AccessDeniedException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessDeniedView.class);

    private final AccessAnnotationChecker accessChecker;

    public AccessDeniedView(@Autowired AccessAnnotationChecker accessChecker) {
        this.accessChecker = accessChecker;
        add(new RouterLink("На главную", OrdersView.class));
        add(new H1(getTranslation("errors.message.access_denied")));
        add(new Paragraph(getTranslation("errors.message.access_denied.message")));
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        var attemptedRoute = "https://industry.scan-stakan.com/" + event.getLocation().getPath();
        var userName = SecurityUtils.getUserName();
        LOGGER.warn("Access denied for user '{}' on route: {}", userName, attemptedRoute);

        Class<? extends Component> target = pickFirstAllowed(
            OrdersView.class,
            MainWidget.class
        );
        if (target != null) {
            event.rerouteTo(target);
            return HttpServletResponse.SC_OK;
        }

        return HttpServletResponse.SC_FORBIDDEN;
    }

    @SafeVarargs
    private Class<? extends Component> pickFirstAllowed(Class<? extends Component>... candidates) {
        for (var c : candidates) {
            if (c != null && accessChecker.hasAccess(c)) {
                return c;
            }
        }
        return null;
    }
}
