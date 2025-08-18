package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.ProductionTreeCacheKey;
import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.dto.file.MaterialEntry;
import com.azarenka.evebuilders.domain.dto.file.TypeInfo;
import com.azarenka.evebuilders.repository.litesql.InvTypesRepository;
import com.azarenka.evebuilders.service.ProductionTreeCache;
import com.azarenka.evebuilders.service.api.IProductionTreeService;
import com.azarenka.evebuilders.service.util.StaticMaterialLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductionTreeService implements IProductionTreeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionTreeService.class);

    @Autowired
    private InvTypesRepository repository;
    @Autowired
    private EveMaterialsDataService eveMaterialsDataService;
    @Autowired
    private StaticMaterialLoader loader;
    @Autowired
    private ProductionTreeCache treeCache;

    public ProductionNode buildProductionTreeCached(String typeName, int quantity) {
        ProductionTreeCacheKey key = new ProductionTreeCacheKey(typeName, quantity);
        if (treeCache.contains(key)) {
            LOGGER.info("Production tree cache hit");
            return treeCache.get(key);
        }
        ProductionNode node = buildProductionTree(typeName, quantity);
        treeCache.put(key, node);
        return node;
    }

    public ProductionNode buildProductionTree(String typeName, int requiredQty) {
        return buildProductionTreeRecursive(typeName, requiredQty, 0);
    }

    private ProductionNode buildProductionTreeRecursive(String typeName, int requiredQty, int currentStage) {
        TypeInfo typeInfo = loader.getByTypeName(typeName);
        ProductionNode root = new ProductionNode();
        root.setTypeName(typeName);
        root.setQuantity(requiredQty);
        root.setStage(currentStage);
        if (typeInfo == null) {
            root.setProducedQuantity(requiredQty);
            root.setExcessQuantity(0);
            return root;
        }
        int outputPerBatch = typeInfo.getOutputQuantity() > 0 ? typeInfo.getOutputQuantity() : 1;
        root.setOutputQuantity(outputPerBatch);
        int batches = (int) Math.ceil((double) requiredQty / outputPerBatch);
        int produced = batches * outputPerBatch;
        root.setProducedQuantity(produced);
        root.setExcessQuantity(produced - requiredQty);
        root.setMaterialType(resolveMaterialType(typeInfo.getGroupName(), typeInfo.getCategoryID()));
        for (MaterialEntry material : typeInfo.getMaterials()) {
            int qtyPerBatch = material.getQuantity(); // <<<< то, что нам нужно сохранить как "чистый рецепт"
            root.putRecipePerBatch(material.getMaterialTypeName(), qtyPerBatch);
            ProductionNode child = buildProductionTreeRecursive(
                material.getMaterialTypeName(),
                material.getQuantity() * batches,
                currentStage + 1
            );
            child.setParent(root);
            root.getChildren().add(child);
        }
        return root;
    }

    public MaterialType resolveMaterialType(String groupName, Integer categoryId) {
        return switch (categoryId) {
            case 2 -> MaterialType.MINERAL;                     // minerals
            case 4 -> switch (groupName) {
                case "Money" -> MaterialType.UNKNOWN;
                case "Mineral" -> MaterialType.MINERAL;
                case "Drug" -> MaterialType.UNKNOWN;
                case "Gas Isotopes" -> MaterialType.GAS;
                case "Ice Product" -> MaterialType.ICE_PRODUCT;
                case "Moon Materials" -> MaterialType.MOON_MATERIAL;
                case "Intermediate Materials" -> MaterialType.INTERMEDIATE;
                case "Composite" -> MaterialType.COMPOSITE_REACTION;
                case "Biochemical Material" -> MaterialType.UNKNOWN;
                case "Salvaged Materials" -> MaterialType.UNKNOWN;
                case "Rogue Drone Components" -> MaterialType.COMPONENT;
                case "Ancient Salvage" -> MaterialType.UNKNOWN;
                case "Wormhole Minerals" -> MaterialType.UNKNOWN;
                case "Hybrid Polymers" -> MaterialType.COMPOSITE_REACTION;
                case "Fuel Block" -> MaterialType.FUEL;
                case "Named Components" -> MaterialType.COMPONENT;
                case "Abyssal Materials" -> MaterialType.COMPONENT;
                case "Molecular-Forged Materials" -> MaterialType.UNKNOWN;
                default -> MaterialType.UNKNOWN;
            };
            case 6 -> MaterialType.SHIP;                       // all ships
            case 7 -> MaterialType.MODULE;                     // all modules
            case 22 -> MaterialType.COMPONENT;                 // advanced components
            case 17 -> MaterialType.COMPONENT;
            case 65 -> MaterialType.PLANETARY;                 // PI commodities
            case 29 -> MaterialType.STRUCTURE;
            default -> MaterialType.UNKNOWN;
        };
    }
}
