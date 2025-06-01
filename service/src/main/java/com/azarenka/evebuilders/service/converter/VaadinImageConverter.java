package com.azarenka.evebuilders.service.converter;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;

public class VaadinImageConverter {

    public static Image createImageFromBytes(byte[] imageBytes) {
        StreamResource resource = new StreamResource("Character portrait",
                () -> new ByteArrayInputStream(imageBytes));
        resource.setContentType("image/png");
        Image image = new Image(resource, "");
        image.setHeight("32px");
        image.setWidth("32px");
        return image;
    }
}
