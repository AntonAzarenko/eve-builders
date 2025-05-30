package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.component.View;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("/logout")
@PageTitle("Log out")
@PermitAll
public class LogoutView extends View {

    public LogoutView(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        request.getSession(false).invalidate();
    }
}
