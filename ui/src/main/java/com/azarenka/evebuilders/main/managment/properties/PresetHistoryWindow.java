package com.azarenka.evebuilders.main.managment.properties;

import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsHistoryDto;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class PresetHistoryWindow extends CommonDialogComponent {

    public PresetHistoryWindow(List<OrderPresetDefaultsHistoryDto> history) {
        super("preset-history-window", false);
        setHeaderTitle(getTranslation("properties.preset.history.title"));
        setWidth("900px");
        setMaxWidth("95vw");
        setHeight("70vh");
        setMaxHeight("70vh");
        add(buildHistoryContent(history));
        Button closeButton = new Button(getTranslation("button.app.close"), event -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        getFooter().add(closeButton);
    }

    private VerticalLayout buildHistoryContent(List<OrderPresetDefaultsHistoryDto> history) {
        VerticalLayout historyList = VaadinUtils.initCommonVerticalLayout();
        historyList.setPadding(false);
        historyList.setSpacing(true);
        historyList.setWidthFull();
        historyList.getStyle().set("overflow", "auto");
        historyList.getStyle().set("max-height", "60vh");

        if (history.isEmpty()) {
            historyList.add(new Span(getTranslation("properties.preset.history.empty")));
            return historyList;
        }

        history.forEach(value -> {
            VerticalLayout card = VaadinUtils.initCommonVerticalLayout();
            card.setPadding(true);
            card.setSpacing(false);
            card.setWidthFull();
            card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
            card.getStyle().set("border-radius", "8px");
            card.getStyle().set("gap", "6px");

            Span header = new Span(String.format("%s | %s", value.getChangedDate(), value.getChangedBy()));
            header.getStyle().set("font-weight", "600");
            Span line1 = new Span(String.format("Type: %s | Priority: %s | Blueprint: %s",
                value.getOrderType(), value.getPriority(), value.getBlueprint()));
            Span line2 = new Span(String.format("Receiver: %s | %s (%s)",
                value.getReceiverType(), value.getReceiverName(), value.getReceiverRefId()));
            Span line3 = new Span(String.format("Rights: %s | Rightsholder: %s",
                value.getOrderRights(), value.getRightsholder()));

            card.add(header, line1, line2, line3);
            historyList.add(card);
        });
        return historyList;
    }
}
