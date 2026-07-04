package com.azarenka.evebuilders.component;

import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.service.api.IEveAuthService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@PreserveOnRefresh
public class Header extends HorizontalLayout implements LocaleChangeObserver, RouterLayout {

    private final ComboBox<String> localeComboboxField = new ComboBox<>();
    private final Image logo = new Image("/themes/builders/img/favicon.png", "alt");
    private Span title;
    private Image avatar;
    private Button addCharacterButton;
    private Button aboutButton;
    private Button logoutButton;
    private Div userNameDiv;
    private Image largeAvatar;
    private final IEveAuthService eveAuthService;
    private final IUserService userService;
    private ThemeToggleIcon themeToggleIcon;

    public Header(IEveAuthService eveAuthService, IUserService userService) {
        this.eveAuthService = eveAuthService;
        this.userService = userService;
        super.setWidthFull();
        setHeight("45px");
        setMinHeight("45px");
        getStyle().set("padding", "0 10px 0 10px");
        initContent();
    }

    private void initContent() {
        initLogo();
        initLocaleComboboxField();
        initAvatar();
        initLogoutButton();
        initAddCharacterButton();
        initLargeAvatar();
        initUserName();
        initAboutLayout();
        createContextMenu();
        var layout = new HorizontalLayout();
        initTheme();
        layout.add(themeToggleIcon, aboutButton, avatar);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        setDefaultVerticalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(layout);
    }

    private void initTheme() {
        String theme = userService.getThemeName();
        if (!theme.isEmpty()) {
            setThemeToCookie(theme);
            themeToggleIcon = new ThemeToggleIcon(theme);
        } else {
            themeToggleIcon = new ThemeToggleIcon("light");
        }
    }

    private void initAboutLayout() {
        aboutButton = new Button(VaadinIcon.INFO.create());
        aboutButton.addClickListener(e -> new AppDevelopmentInformationWindow(eveAuthService.getAppVersion()).open());
        aboutButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        aboutButton.addClassName("circle-button");
    }

    private void initAddCharacterButton() {
        addCharacterButton = new Button(getTranslation("button.app.add_character"));
        addCharacterButton.addClickListener(event ->
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/eveonline"));
        addCharacterButton.setMaxWidth("176px");
        addCharacterButton.setMinWidth("176px");
    }

    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setOpenOnClick(true);
        contextMenu.getElement().getStyle().set("padding", "0px").set("margin", "0px");
        contextMenu.getElement().getStyle().set("align-items", "center");
        contextMenu.setTarget(avatar);
        addMenuItem(contextMenu, largeAvatar);
        addMenuItem(contextMenu, userNameDiv);
        addMenuItem(contextMenu, localeComboboxField);
        addMenuItem(contextMenu, addCharacterButton);
        addMenuItem(contextMenu, logoutButton);
        return contextMenu;
    }

    private void addMenuItem(ContextMenu contextMenu, Component component) {
        MenuItem menuItem = contextMenu.addItem(component);
        menuItem.getElement().getStyle().set("padding", "0px").set("margin", "0px");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        title.setText(getTranslation("app.logo"));
        logoutButton.setText(getTranslation("button.app.logout"));
        addCharacterButton.setText(getTranslation("button.app.add_character"));
    }

    private void initLargeAvatar() {
        largeAvatar = eveAuthService.getCharacterPortrait128();
        largeAvatar.setWidth("176px");
        largeAvatar.setHeight("176px");
    }

    private void initLogo() {
        title = new Span(getTranslation("app.logo"));
        title.getStyle().set("font-size", "25px");
        title.getStyle().set("font-weight", "bold");
        title.getStyle().set("font-family", "math");
        logo.setWidth("40px");
        logo.setHeight("40px");
        add(logo, title);
    }

    private void initAvatar() {
        avatar = eveAuthService.getCharacterPortrait();
        avatar.setWidth("50px");
        avatar.setHeight("50px");
    }

    private void initLocaleComboboxField() {
        localeComboboxField.setItems(List.of("Ru", "En"));
        localeComboboxField.addValueChangeListener(event -> {
            getUI().ifPresent(ui -> ui.setLocale(new Locale(event.getValue().toLowerCase())));
            getUI().ifPresent(ui -> ui.navigate(UI.getCurrent().getInternals().getActiveViewLocation().getPath()));
            userService.updateLanguage(event.getValue().toLowerCase());
            UI.getCurrent().refreshCurrentRoute(true);
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
        Optional<User> byUsername = userService.getByUsername(SecurityUtils.getUserName());
        byUsername.ifPresent(value -> localeComboboxField.setValue(value.getLanguage()));
        localeComboboxField.setWidth("176px");
    }

    private void initLogoutButton() {
        logoutButton = new Button(getTranslation("button.app.logout"), VaadinIcon.OUT.create(),
            event -> UI.getCurrent().getPage().setLocation("/logout"));
        logoutButton.setWidth("176px");
    }

    private void initUserName() {
        Span span = new Span(SecurityUtils.getUserName());
        userNameDiv = new Div(span);
        userNameDiv.getStyle().set("text-align", "center");
    }

    private void setThemeToCookie(String themeName) {
        UI.getCurrent().getElement().setAttribute("theme", themeName);
    }

    public class ThemeToggleIcon extends HorizontalLayout {

        private boolean darkMode = false;
        private final Icon sunIcon = VaadinIcon.SUN_O.create();
        private final Icon moonIcon = VaadinIcon.MOON.create();

        public ThemeToggleIcon(String themeName) {
            Button toggle = new Button();
            if (Lumo.LIGHT.equals(themeName)) {
                toggle.setIcon(sunIcon);
                sunIcon.setColor("orange");
            } else {
                darkMode = true;
                moonIcon.setColor("gold");
                toggle.setIcon(moonIcon);
            }
            toggle.addClickListener(e -> {
                darkMode = !darkMode;
                if (darkMode) {
                    setThemeToCookie(Lumo.DARK);
                    toggle.setIcon(moonIcon);
                    moonIcon.setColor("gold");
                } else {
                    setThemeToCookie(Lumo.LIGHT);
                    toggle.setIcon(sunIcon);
                    sunIcon.setColor("orange");
                }
                userService.updateTheme(darkMode ? Lumo.DARK : Lumo.LIGHT);
            });
            toggle.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer");

            add(toggle);
        }
    }
}
