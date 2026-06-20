package com.azarenka.evebuilders.main.profile;

import com.azarenka.evebuilders.component.StatCard;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.auth.auth.ui.CurrentUserProfileResponse;
import com.azarenka.evebuilders.main.menu.MenuProfilePage;
import com.azarenka.evebuilders.service.api.IProfileService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.List;
import java.util.Locale;

import org.springframework.security.access.prepost.PreAuthorize;

@Route(value = "main", layout = MenuProfilePage.class)
@PreAuthorize("isAuthenticated()")
@PageTitle("Profile")
public class ProfileView extends View implements LocaleChangeObserver {

    private final IProfileService profileService;
    private CurrentUserProfileResponse profile;

    private final H1 title = new H1();
    private final Paragraph subtitle = new Paragraph();
    private final FlexLayout statsLayout = new FlexLayout();
    private final VerticalLayout detailsCard = new VerticalLayout();
    private final VerticalLayout preferencesCard = new VerticalLayout();
    private final ComboBox<String> languageField = new ComboBox<>();
    private final ComboBox<String> themeField = new ComboBox<>();
    private final Button saveLanguageButton = new Button();
    private final Button saveThemeButton = new Button();

    public ProfileView(IProfileService profileService) {
        this.profileService = profileService;
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        add(buildHeader(), buildStatsSection(), buildDetailsSection());
        loadProfile();
    }

    private VerticalLayout buildHeader() {
        title.getStyle().set("margin-bottom", "0");
        subtitle.getStyle().set("margin-top", "0");
        var header = new VerticalLayout(title, subtitle);
        header.setPadding(false);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }

    private HorizontalLayout buildStatsSection() {
        statsLayout.setWidthFull();
        statsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        statsLayout.getStyle().set("gap", "12px");
        var statsContainer = new HorizontalLayout(statsLayout);
        statsContainer.setWidthFull();
        return statsContainer;
    }

    private FlexLayout buildDetailsSection() {
        configureCard(detailsCard);
        configureCard(preferencesCard);
        configurePreferencesForm();

        var detailsColumn = new VerticalLayout(detailsCard);
        detailsColumn.setPadding(false);
        detailsColumn.setSpacing(true);
        detailsColumn.setWidthFull();
        detailsColumn.getStyle().set("flex", "1 1 320px");

        var preferencesColumn = new VerticalLayout(preferencesCard);
        preferencesColumn.setPadding(false);
        preferencesColumn.setSpacing(true);
        preferencesColumn.setWidthFull();
        preferencesColumn.getStyle().set("flex", "1 1 320px");

        var container = new FlexLayout();
        container.add(detailsColumn, preferencesColumn);
        container.setWidthFull();
        container.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        container.getStyle().set("gap", "12px");
        return container;
    }

    private void configureCard(VerticalLayout card) {
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        card.getStyle().set("background", "var(--lumo-base-color)");
        card.setWidthFull();
    }

    private void configurePreferencesForm() {
        languageField.setWidthFull();
        themeField.setWidthFull();
        saveLanguageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveThemeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveLanguageButton.addClickListener(event -> applyLanguage());
        saveThemeButton.addClickListener(event -> applyTheme());
    }

    private void loadProfile() {
        profile = profileService.getCurrentProfile();
        render();
    }

    private void render() {
        statsLayout.removeAll();
        detailsCard.removeAll();
        preferencesCard.removeAll();

        title.setText(getTranslation("label.profile.header"));
        subtitle.setText(getTranslation("label.profile.subtitle"));

        statsLayout.add(
            new StatCard(getTranslation("label.profile.stats.orders"), String.valueOf(profile.distributedOrders()),
                rankSuffix(profile.ordersRank())),
            new StatCard(getTranslation("label.profile.stats.completed_orders"),
                String.valueOf(profile.completedOrders()), ""),
            new StatCard(getTranslation("label.profile.stats.ships"), String.valueOf(profile.builtShips()),
                rankSuffix(profile.shipsRank())),
            new StatCard(getTranslation("label.profile.stats.fleets"),
                String.valueOf(profile.fleetParticipations()), rankSuffix(profile.fleetRank()))
        );

        detailsCard.add(
            new H2(getTranslation("label.profile.details")),
            new Span(getTranslation("label.profile.username") + ": " + profile.characterName()),
            new Span(getTranslation("label.profile.character_id") + ": " + profile.eveCharacterId()),
            new Span(getTranslation("label.profile.corporation") + ": " + safe(profile.corporationName())),
            new Span(getTranslation("label.profile.alliance") + ": " + safe(profile.allianceName())),
            new Span(getTranslation("label.profile.roles") + ": " + String.join(", ", profile.roles())),
            new Span(getTranslation("label.profile.permissions") + ": " + profile.permissions().size())
        );

        languageField.setItems(List.of("en", "ru"));
        languageField.setItemLabelGenerator(value -> "en".equals(value)
            ? getTranslation("label.profile.language.en")
            : getTranslation("label.profile.language.ru"));
        languageField.setValue(defaultIfBlank(profile.language(), "en"));

        themeField.setItems(List.of(Lumo.LIGHT, Lumo.DARK));
        themeField.setItemLabelGenerator(value -> Lumo.DARK.equals(value)
            ? getTranslation("label.profile.theme.dark")
            : getTranslation("label.profile.theme.light"));
        themeField.setValue(defaultIfBlank(profile.theme(), Lumo.LIGHT));

        preferencesCard.add(
            new H2(getTranslation("label.profile.preferences")),
            languageField,
            saveLanguageButton,
            themeField,
            saveThemeButton
        );

        saveLanguageButton.setText(getTranslation("button.profile.save_language"));
        saveThemeButton.setText(getTranslation("button.profile.save_theme"));
    }

    private void applyLanguage() {
        String language = languageField.getValue();
        if (language == null || language.isBlank()) {
            return;
        }
        profileService.updateLanguage(language);
        UI.getCurrent().setLocale(new Locale(language));
        UI.getCurrent().refreshCurrentRoute(true);
    }

    private void applyTheme() {
        String theme = themeField.getValue();
        if (theme == null || theme.isBlank()) {
            return;
        }
        profileService.updateTheme(theme);
        UI.getCurrent().getElement().setAttribute("theme", theme);
        UI.getCurrent().refreshCurrentRoute(true);
    }

    private String rankSuffix(Integer rank) {
        return rank == null ? "" : "#" + rank;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        render();
    }
}
