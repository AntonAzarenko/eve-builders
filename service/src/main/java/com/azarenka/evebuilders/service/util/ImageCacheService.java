package com.azarenka.evebuilders.service.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImageCacheService {

    private static final String IMAGE_URL_TEMPLATE = "https://images.evetech.net/types/%s/icon?size=%s";
    private final WebClient webClient = WebClient.builder().build();

    private final Map<String, SoftReference<byte[]>> imageCache = new ConcurrentHashMap<>();

    public byte[] getImageBytes(long typeId, String size) {
        String key = typeId + "_" + size;
        SoftReference<byte[]> ref = imageCache.get(key);
        byte[] bytes = ref != null ? ref.get() : null;

        if (bytes == null) {
            bytes = downloadImageBytes(typeId, size);
            imageCache.put(key, new SoftReference<>(bytes));
        }
        return bytes;
    }

    private byte[] downloadImageBytes(long typeId, String size) {
        return webClient.get()
                .uri(String.format(IMAGE_URL_TEMPLATE, typeId, size))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
