package com.azarenka.evebuilders.main.commonview;

import com.azarenka.evebuilders.domain.db.Fit;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

public class FitComponent extends VerticalLayout implements LocaleChangeObserver {

    private final Fit fit;
    private final Span highLevelHeader = new Span(getTranslation("view.fit.high_slot"));
    private final TextArea fitTextArea = new TextArea();
    private String editedText;

    public FitComponent(Fit fit) {
        this.fit = fit;
        super.setMaxHeight("800px");
        super.setWidth("500px");
        initContent();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        highLevelHeader.setText(getTranslation("view.fit.high_slot"));
    }

    public TextArea getFitTextArea() {
        return fitTextArea;
    }

    public String getEditedText() {
        return editedText;
    }

    public void setEditedText(String editedText) {
        this.editedText = editedText;
    }

    private void initContent() {
        fitTextArea.setSizeFull();
        fitTextArea.setValue(fit.getTextFit());
        editedText = fit.getTextFit();
        add(fitTextArea);
        setSpacing(false);
    }
}
