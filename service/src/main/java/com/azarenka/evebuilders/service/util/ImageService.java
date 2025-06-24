package com.azarenka.evebuilders.service.util;

import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.vaadin.flow.component.html.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ImageService {

    private static final String IMAGE_SIZE_64 = "64";
    private static final String IMAGE_SIZE_32 = "32";
    private static final Pattern PATTERN = Pattern.compile("x(\\d+)$");

    @Autowired
    private IEveMaterialDataService dataService;
    @Autowired
    private ImageCacheService imageCacheService;

    public Image createImage64(String typeName) {
        String normalizedName = normalizeName(typeName);
        int id = dataService.getTypeIdByName(normalizedName);
        return loadImage(id, IMAGE_SIZE_64);
    }

    public Image createImage32(String typeName) {
        String normalizedName = normalizeName(typeName);
        int id = dataService.getTypeIdByName(normalizedName);
        return loadImage(id, IMAGE_SIZE_32);
    }

    public Image createImage64(int id) {
        return loadImage(id, IMAGE_SIZE_64);
    }

    public Image createImage32(int id) {
        return loadImage(id, IMAGE_SIZE_32);
    }

    public Image createImage(int id, String size) {
        return loadImage(id, size);
    }

    private Image loadImage(int id, String cdnSize) {
        try {
            byte[] imageBytes = imageCacheService.getImageBytes(id, cdnSize);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String dataUrl = "data:image/png;base64," + base64;
            Image image = new Image(dataUrl, "type image");
            image.setWidth(cdnSize + "px");
            image.setHeight(cdnSize + "px");
            image.getElement().setAttribute("loading", "lazy");
            return image;
        } catch (Exception e) {
            return new Image("https://via.placeholder.com/" + cdnSize, "fallback");
        }
    }

    private String normalizeName(String moduleName) {
        Matcher matcher = PATTERN.matcher(moduleName);
        if (matcher.find()) {
            int index = moduleName.indexOf("x");
            return moduleName.substring(0, index - 1).trim();
        }
        return moduleName.trim();
    }
}
