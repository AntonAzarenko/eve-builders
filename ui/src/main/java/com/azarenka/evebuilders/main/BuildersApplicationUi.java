package com.azarenka.evebuilders.main;

import com.azarenka.evebuilders.component.Header;
import com.azarenka.evebuilders.component.exception.IndustryErrorHandler;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.service.api.IEveAuthService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;

import java.util.Locale;
import java.util.Optional;

@Route("")
@CssImport(themeFor = "vaadin-grid", value = "./themes/builders/components/vaadin-grid-cell.css")
public class BuildersApplicationUi extends AppLayout {

    private IUserService userService;

    public BuildersApplicationUi(IEveAuthService eveAuthService, IUserService userService) {
        this.userService = userService;
        addToNavbar(new Header(eveAuthService, userService));
        super.setClassName("app-layout");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Optional<User> byUsername = userService.getByUsername(SecurityUtils.getUserName());
        byUsername.ifPresent(value -> VaadinSession.getCurrent().setLocale(new Locale(value.getLanguage())));
        UI current = UI.getCurrent();
        if (current != null) {
            LoadingIndicatorConfiguration loadingIndicatorConfiguration = current.getLoadingIndicatorConfiguration();
            loadingIndicatorConfiguration.setApplyDefaultTheme(false);
            current.getSession().setErrorHandler(new IndustryErrorHandler());
        }
    }
}
