package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.common.util.INumberFormater;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.dto.CalculationItemInformation;
import com.azarenka.evebuilders.domain.dto.LocationInfo;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.constructions.api.IBuildConstructionController;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.Objects;

public class UserMaterialsInfoWindow extends CommonDialogComponent implements INumberFormater {

    private final IBuildConstructionController controller;

    public UserMaterialsInfoWindow(List<CalculationItemInformation> fullInfo, IBuildConstructionController controller) {
        super("user-materials-info-window", false);
        this.controller = controller;
        super.setWidth("800px");
        super.setHeight("500px");
        super.setDraggable(true);
        setHeaderTitle("Info for " + fullInfo.get(0).getTypeName());
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setSizeFull();
        VerticalLayout rootBlock = VaadinUtils.initCommonVerticalLayout();
        rootBlock.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        rootBlock.setPadding(false);
        rootBlock.setSpacing(false);
        rootBlock.setSizeFull();
        Span header = new Span(fullInfo.get(0).getTypeName());
        header.getStyle().set("font-weight", "bold");
        rootBlock.add(new HorizontalLayout(createIcon(fullInfo.get(0).getTypeName()), header));

        for (CalculationItemInformation info : fullInfo) {
            LocationInfo locationInfoById = controller.getLocationInfoById(info.getItemDto().getAsset().getLocationId(),
                info.getItemDto().getUserName());
            String count = "";
            if (Objects.nonNull(locationInfoById)) {
                count = formatNumber(info.getItemDto().getAsset().getQuantity());
            }
            Span line = new Span(info.getItemDto()
                .getUserName() + " Количество: " + count);
            VerticalLayout f =new VerticalLayout();
            f.setSpacing(false);
            f.setPadding(false);
            HorizontalLayout horizontalLayout =
                new HorizontalLayout(new Span("↳ "), controller.createAvatarIcon(info.getItemDto().getUserName()),
                    line);
            HorizontalLayout layout1 =
                new HorizontalLayout(new Span("↳ Местонахождение: "), new Span(locationInfoById.getName()));
            layout1.getStyle().set("margin-left", "10px");
            layout1.getStyle().set("padding", "10px");
            horizontalLayout.getStyle().set("margin-left", "10px");
            horizontalLayout.getStyle().set("padding", "10px");
            f.add(horizontalLayout);
            f.add(layout1);
            rootBlock.add(f);
        }
        layout.add(rootBlock);
        add(layout);
    }

    private Image createIcon(String moduleName) {
        var icon = controller.getImageByInvTypeName(moduleName);
        icon.setWidth("25px");
        icon.setHeight("25px");
        return icon;
    }
}
