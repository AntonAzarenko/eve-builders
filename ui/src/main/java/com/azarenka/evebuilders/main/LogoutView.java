package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.service.impl.auth.EveOAuth2AuthorizedClientService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Route("/logout")
@PageTitle("Log out")
@PermitAll
public class LogoutView extends View {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutView.class);

    public LogoutView(EveOAuth2AuthorizedClientService authorizedClientService) {
        var username = SecurityUtils.getUserName();
        LOGGER.info("User [{}] is logged out", username);
        authorizedClientService.removeAuthorizedClient("eveonline", username);
        SecurityContextHolder.clearContext();
        VaadinSession.getCurrent().getSession().invalidate();
    }
}
