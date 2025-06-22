package com.azarenka.evebuilders.service;

import com.azarenka.evebuilders.domain.ProductionTreeCacheKey;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductionTreeCache {

    private final Map<ProductionTreeCacheKey, ProductionNode> cache = new ConcurrentHashMap<>();

    public ProductionNode get(ProductionTreeCacheKey key) {
        return cache.get(key);
    }

    public void put(ProductionTreeCacheKey key, ProductionNode node) {
        cache.put(key, node);
    }

    public boolean contains(ProductionTreeCacheKey key) {
        return cache.containsKey(key);
    }

    public void clear() {
        cache.clear();
    }
}
