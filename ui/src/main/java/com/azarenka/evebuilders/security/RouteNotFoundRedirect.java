package com.azarenka.evebuilders.security;

import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouteNotFoundError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;

@Tag("div")
public class RouteNotFoundRedirect extends RouteNotFoundError {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteNotFoundRedirect.class);

    public RouteNotFoundRedirect() {
        UI.getCurrent().getPage().setLocation("/orders/main");
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        var attemptedRoute = "https://industry.scan-stakan.com/" + event.getLocation().getPath();
        var userName = SecurityUtils.getUserName();
        LOGGER.warn("Access denied for user '{}' on route: {}", userName, attemptedRoute);
        return HttpServletResponse.SC_FOUND;
    }
}
