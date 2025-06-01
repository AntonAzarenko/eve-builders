package com.azarenka.evebuilders.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;

import java.util.Locale;

@Theme("builders")
public class VaadinAppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addLink("shortcut icon", "themes/builders/img/favicon.png");
        settings.addFavIcon(Inline.Position.APPEND, "shortcut icon", "themes/builders/img/favicon.png", "16px");
        AppShellConfigurator.super.configurePage(settings);
        VaadinSession.getCurrent().setLocale(new Locale("ru"));
    }
}
