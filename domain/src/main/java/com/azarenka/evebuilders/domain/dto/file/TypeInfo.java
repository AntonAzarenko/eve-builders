package com.azarenka.evebuilders.domain.dto.file;

import java.util.ArrayList;
import java.util.List;

public class TypeInfo {

    private int typeID;
    private String typeName;
    private String groupName;
    private int categoryID;
    private int outputQuantity;
    private List<MaterialEntry> materials = new ArrayList<>();

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public int getOutputQuantity() {
        return outputQuantity;
    }

    public void setOutputQuantity(int outputQuantity) {
        this.outputQuantity = outputQuantity;
    }

    public List<MaterialEntry> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialEntry> materials) {
        this.materials = materials;
    }
}
