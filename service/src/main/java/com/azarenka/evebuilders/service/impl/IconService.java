package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.sqllite.EveIcon;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;

import java.io.InputStream;

@Deprecated
public class IconService {

    public static Image createImageFromEveIcon(EveIcon eveIcon) {

        String iconPath = eveIcon.getIconFile().startsWith("res:/")
                ? eveIcon.getIconFile().replace("res:/", "")
                : eveIcon.getIconFile();
        StreamResource resource = new StreamResource(
                eveIcon.getIconId() + ".png",
                () -> getResourceAsStream(iconPath)
        );
        Image image = new Image(resource, eveIcon.getDescription());
        image.setAlt(eveIcon.getDescription() != null ? eveIcon.getDescription() : "Icon");

        return image;
    }

    public static Image createImageWithSize30(EveIcon eveIcon) {
        Image imageFromEveIcon = createImageFromEveIcon(eveIcon);
        imageFromEveIcon.setWidth("30px");
        imageFromEveIcon.setHeight("30px");
        return imageFromEveIcon;
    }

    private static InputStream getResourceAsStream(String resourcePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
    }
}
