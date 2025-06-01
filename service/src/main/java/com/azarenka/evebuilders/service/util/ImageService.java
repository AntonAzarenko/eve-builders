package com.azarenka.evebuilders.service.util;

import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.vaadin.flow.component.html.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
//TODO: move all constants to properties file
public class ImageService {

    private static final String IMAGES_PATH = "themes/ui/texture/types/";
    private static final String IMAGE_SIZE_64 = "_64.png";
    private static final String IMAGE_SIZE_32 = "_32.png";
    private static final Pattern PATTERN = Pattern.compile("x(\\d+)$");
    private static final String IMAGE_URL_TEMPLATE = "https://images.evetech.net/types/%s/icon?size=%s";

    private final WebClient webClient = WebClient.builder().build();

    @Autowired
    private IEveMaterialDataService dataService;

    public Image createImage64(String typeName) {
        if (checkModuleIsMultipleCount(typeName)) {
            typeName = typeName.substring(0, typeName.indexOf("x") - 1);
        }
        InvType invTypeByGroupName = dataService.getInvTypeByTypeName(typeName);
        return createImage64(invTypeByGroupName, "64");
    }

    public Image createImage32(String typeName) {
        if (checkModuleIsMultipleCount(typeName)) {
            typeName = typeName.substring(0, typeName.indexOf("x") - 1);
        }
        InvType invTypeByGroupName = dataService.getInvTypeByTypeName(typeName);
        return createImage32(invTypeByGroupName, "32");
    }

    public Image createImage64(InvType invType, String size) {
        return createimage(invType, size);
    }

    public Image createImage32(InvType invType, String size) {
        return createimage(invType, size);
    }

    boolean checkModuleIsMultipleCount(String moduleName) {
        Matcher matcher = PATTERN.matcher(moduleName);
        return matcher.find();
    }

    private Image createimage(InvType invType, String cdnSize) {
        try {
            byte[] imageBytes = webClient.get()
                    .uri(String.format(IMAGE_URL_TEMPLATE, invType.getTypeID(), cdnSize))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String dataUrl = "data:image/png;base64," + base64;

            Image image = new Image(dataUrl, "type image");
            image.setWidth(cdnSize + "px");
            image.setHeight(cdnSize + "px");
            image.getElement().setAttribute("loading", "lazy");
            return image;

        } catch (Exception e) {
            // fallback
            return new Image("https://via.placeholder.com/" + cdnSize, "fallback");
        }
    }
}
