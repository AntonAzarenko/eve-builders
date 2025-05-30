package com.azarenka.evebuilders.domain.dto.file;

public class TypeDogmaAttribute {

    public int typeID;
    public int attributeID;
    private String valueInt;
    public Double valueFloat;

    public String getValueInt() {
        return valueInt;
    }

    public void setValueInt(String valueInt) {
        this.valueInt = valueInt;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public int getAttributeID() {
        return attributeID;
    }

    public void setAttributeID(int attributeID) {
        this.attributeID = attributeID;
    }

    public Double getValueFloat() {
        return valueFloat;
    }

    public void setValueFloat(Double valueFloat) {
        this.valueFloat = valueFloat;
    }
}
