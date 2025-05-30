package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.component.Header;
import com.azarenka.evebuilders.domain.mysql.User;
import com.azarenka.evebuilders.service.api.IEveAuthService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.Locale;
import java.util.Optional;

@Route("")
public class BuildersApplicationUi extends AppLayout {

    private IUserService userService;

    public BuildersApplicationUi(IEveAuthService eveAuthService, IUserService userService) {
        this.userService = userService;
        addToNavbar(new Header(eveAuthService, userService));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Optional<User> byUsername = userService.getByUsername(SecurityUtils.getUserName());
        byUsername.ifPresent(value -> VaadinSession.getCurrent().setLocale(new Locale(value.getLanguage())));
    }
}
