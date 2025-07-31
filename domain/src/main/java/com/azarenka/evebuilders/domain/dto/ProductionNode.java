package com.azarenka.evebuilders.domain.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductionNode {

    private String typeName;
    private int quantity;
    private String blueprintName;
    private MaterialType materialType;
    private int producedQuantity;
    private int excessQuantity;
    private int finalQuantity;
    private int efficiency;
    private int outputQuantity;
    private List<ProductionNode> children = new ArrayList<>();
    private final Map<String, Integer> recipePerBatchBase = new HashMap<>();
    private final Map<String, Integer> recipePerBatchEff = new HashMap<>();


    private ProductionNode parent;

    private boolean stageHeader;
    private int stage;

    public void putRecipePerBatch(String childTypeName, int qtyPerBatch) {
        recipePerBatchBase.put(childTypeName, qtyPerBatch);
    }

    /** Возвращает чистое значение из рецепта (на 1 батч родителя) */
    public int getRecipeQuantityBase(String childTypeName) {
        return recipePerBatchBase.getOrDefault(childTypeName, 0);
    }

    public void putRecipePerBatchEff(String childTypeName, int qtyPerBatch) {
        recipePerBatchEff.put(childTypeName, qtyPerBatch);
    }

    public int getRecipePerBatchEff(String childTypeName) {
        return recipePerBatchEff.getOrDefault(childTypeName, 0);
    }

    public int getEffectivePerBatch(String childTypeName) {
        int v = recipePerBatchEff.getOrDefault(childTypeName, 0);
        return v > 0 ? v : recipePerBatchBase.getOrDefault(childTypeName, 0);
    }

    /** Когда нужно “пересчитать заново” — чистим эффективную мапу */
    public void clearRecipePerBatchEff() {
        recipePerBatchEff.clear();
    }

    public int getOutputQuantity() {
        return outputQuantity;
    }

    public void setOutputQuantity(int outputQuantity) {
        this.outputQuantity = outputQuantity;
    }

    public int getFinalQuantity() {
        return finalQuantity;
    }

    public void setFinalQuantity(int finalQuantity) {
        this.finalQuantity = finalQuantity;
    }

    public boolean isStageHeader() {
        return stageHeader;
    }

    public void setStageHeader(boolean stageHeader) {
        this.stageHeader = stageHeader;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public ProductionNode getParent() {
        return parent;
    }

    public int getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(int efficiency) {
        this.efficiency = efficiency;
    }

    public void setParent(ProductionNode parent) {
        this.parent = parent;
    }

    public int getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(int producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public int getExcessQuantity() {
        return excessQuantity;
    }

    public void setExcessQuantity(int excessQuantity) {
        this.excessQuantity = excessQuantity;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public List<ProductionNode> getChildren() {
        return children;
    }

    public void setChildren(List<ProductionNode> children) {
        this.children = children;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }
}
