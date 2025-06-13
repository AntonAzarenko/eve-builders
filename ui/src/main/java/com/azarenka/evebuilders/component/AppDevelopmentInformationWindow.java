package com.azarenka.evebuilders.component;

import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AppDevelopmentInformationWindow extends CommonDialogComponent {

    public AppDevelopmentInformationWindow(String version) {
        setHeaderTitle("EVE Online Industry Manager");
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        layout.add(new Paragraph("Alliance: Hold My Probs"));

        layout.add(new Paragraph("Version: " + version));
        layout.add(new Paragraph("Developer: AntonFromEpam"));
        Anchor githubLink = new Anchor("https://github.com/AntonAzarenko/eve-builders", "View on GitHub");
        githubLink.setTarget("_blank");
        layout.add(githubLink);

        Anchor releaseNotes = new Anchor("https://github.com/AntonAzarenko/eve-builders/blob/main/README.md", "Release Notes");
        releaseNotes.setTarget("_blank");
        layout.add(releaseNotes);

        Button donationButton = new Button(VaadinIcon.HEART.create());
        donationButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        donationButton.setText("Поддержать проект");
        layout.add(donationButton);
        donationButton.addClickListener(e -> createSupportDialog().open());

        getFooter().add(createCloseButton());
        add(layout);
    }

    public  Dialog createSupportDialog() {
        CommonDialogComponent dialog = new CommonDialogComponent();
        dialog.setHeaderTitle("Поддержать проект");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        layout.add(new Paragraph("Вы можете отправить внутриигровую валюту (ISK) на персонажа AntonFromEpam.\n\n" +
                "Откройте в игре меню Contacts → Поиск, Найдите персонажа и укажите сумму."));

        Button copyNameBtn = new Button("Скопировать имя для перевода");
        copyNameBtn.addClickListener(e -> UI.getCurrent().getPage()
                .executeJs("navigator.clipboard.writeText('AntonFromEpam')"));

        layout.add(copyNameBtn);
        dialog.add(layout);
        dialog.getFooter().add(dialog.createCloseButton());

        return dialog;
    }
}
