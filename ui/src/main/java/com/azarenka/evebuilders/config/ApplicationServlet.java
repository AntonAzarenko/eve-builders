package com.azarenka.evebuilders.config;

import com.vaadin.flow.server.VaadinServlet;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/*", name = "slot", asyncSupported = true, loadOnStartup = 1,
        initParams = { @WebInitParam(name = "com.vaadin.flow.i18n.provider", value = "com.vaadin.example.ui.TranslationProvider") })
public class ApplicationServlet extends VaadinServlet {
}
