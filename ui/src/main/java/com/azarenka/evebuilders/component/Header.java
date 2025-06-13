package com.azarenka.evebuilders.component;

import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.service.api.IEveAuthService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLayout;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@PreserveOnRefresh
public class Header extends HorizontalLayout implements LocaleChangeObserver, RouterLayout {

    private final ComboBox<String> localeComboboxField = new ComboBox<>();
    private final Image logo = new Image("/themes/builders/img/holdmyprobs_logo.jpg", "alt");
    private Span title;
    private Image avatar;
    private Button addCharacterButton;
    private Button aboutButton;
    private Button logoutButton;
    private Div userNameDiv;
    private Image largeAvatar;
    private final IEveAuthService eveAuthService;
    private final IUserService userService;

    public Header(IEveAuthService eveAuthService, IUserService userService) {
        this.eveAuthService = eveAuthService;
        this.userService = userService;
        super.setWidthFull();
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
        layout.add(aboutButton, avatar);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        setDefaultVerticalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(layout);
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
                UI.getCurrent().getPage().setLocation(eveAuthService.generateAuthUrl()));
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

    private MenuItem addMenuItem(ContextMenu contextMenu, Component component) {
        MenuItem menuItem = contextMenu.addItem(component);
        menuItem.getElement().getStyle().set("padding", "0px").set("margin", "0px");
        return menuItem;
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
        logo.setWidth("64px");
        logo.setHeight("64px");
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
}
