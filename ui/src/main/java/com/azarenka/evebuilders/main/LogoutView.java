package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.security.AuthService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("/logout")
@PageTitle("Log out")
@PermitAll
public class LogoutView extends View {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutView.class);

    public LogoutView(HttpServletRequest request) {
        LOGGER.info("User {} is log off", SecurityUtils.getUserName());
        SecurityContextHolder.clearContext();
        request.getSession(false).invalidate();
    }
}
