package com.azarenka.evebuilders.domain.dto;

import java.util.ArrayList;
import java.util.List;

public class ProductionNode {

    private String typeName;
    private int quantity;
    private String blueprintName;
    private MaterialType materialType;
    private int producedQuantity;
    private int excessQuantity;
    private List<ProductionNode> children = new ArrayList<>();

    private ProductionNode parent;

    private boolean stageHeader;
    private int stage;

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
