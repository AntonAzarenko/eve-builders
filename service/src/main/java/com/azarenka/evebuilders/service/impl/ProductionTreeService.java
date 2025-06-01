package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.dto.file.MaterialEntry;
import com.azarenka.evebuilders.domain.dto.file.TypeInfo;
import com.azarenka.evebuilders.domain.sqllite.MaterialInfo;
import com.azarenka.evebuilders.repository.litesql.InvTypesRepository;
import com.azarenka.evebuilders.service.api.IProductionTreeService;
import com.azarenka.evebuilders.service.util.StaticMaterialLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductionTreeService implements IProductionTreeService {

    private final InvTypesRepository repository;

    @Autowired
    private EveMaterialsDataService eveMaterialsDataService;
    @Autowired
    private StaticMaterialLoader loader;

    public ProductionTreeService(InvTypesRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductionNode buildProductionTree(String typeName, int quantity) {
        MaterialType baseType = eveMaterialsDataService.getTypeByName(typeName);
        ProductionNode root = new ProductionNode();
        root.setTypeName(typeName);
        root.setQuantity(quantity);
        root.setMaterialType(baseType);
        List<MaterialInfo> materials = resolveMaterials(baseType, typeName);
        if (materials.isEmpty()) {
            return root;
        }
        for (MaterialInfo mat : materials) {
            int inputPerBatch = mat.getQuantity();
            int outputPerBatch = mat.getOutputQuantity() != null ? mat.getOutputQuantity() : 1;
            int batches = (int) Math.ceil((double) quantity / outputPerBatch);
            int totalQty = inputPerBatch * batches;
            ProductionNode child = buildProductionTree(mat.getMaterialName(), inputPerBatch);
            child.setQuantity(totalQty); // для UI: общее кол-во
            child.setBlueprintName(mat.getBlueprintName());
            child.setMaterialType(eveMaterialsDataService.getTypeByName(mat.getMaterialName()));
            root.getChildren().add(child);
        }
        return root;
    }

    public List<MaterialInfo> resolveMaterials(MaterialType baseType, String typeName) {
        return switch (baseType) {
            case MINERAL, MOON_MATERIAL, ICE_PRODUCT, GAS, PLANETARY, UNKNOWN -> List.of();
            case COMPONENT, STRUCTURE, SHIP, FUEL, MATERIAL, MODULE -> {
                List<MaterialInfo> mats = repository.findManufacturingMaterials(typeName);
                if (mats.isEmpty()) {
                    mats = repository.findReactionMaterials(typeName);
                }
                yield mats;
            }
            case COMPOSITE_REACTION, SIMPLE_REACTION -> repository.findReactionMaterials(typeName);
        };
    }

    public ProductionNode buildTree(String typeName, int requiredQty) {
        TypeInfo typeInfo = loader.getByTypeName(typeName);
        ProductionNode root = new ProductionNode();
        root.setTypeName(typeName);
        root.setQuantity(requiredQty);
        if (typeInfo == null) {
            root.setProducedQuantity(requiredQty);
            root.setExcessQuantity(0);
            return root;
        }
        int outputPerBatch = typeInfo.getOutputQuantity() > 0 ? typeInfo.getOutputQuantity() : 1;
        int batches = (int) Math.ceil((double) requiredQty / outputPerBatch);
        int produced = batches * outputPerBatch;
        root.setProducedQuantity(produced);
        root.setExcessQuantity(produced - requiredQty);
        for (MaterialEntry material : typeInfo.getMaterials()) {
            ProductionNode child = buildTree(material.getMaterialTypeName(), material.getQuantity() * batches);
            if (child != null) {
                child.setParent(root);
                root.getChildren().add(child);
            }
        }
        return root;
    }
}
