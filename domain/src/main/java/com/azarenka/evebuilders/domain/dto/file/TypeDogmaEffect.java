package com.azarenka.evebuilders.domain.dto.file;

public class TypeDogmaEffect {

    public int typeID;
    public int effectID;
    public int isDefault;

    public TypeDogmaEffect() {
    }

    public TypeDogmaEffect(int typeID, int effectID, int isDefault) {
        this.typeID = typeID;
        this.effectID = effectID;
        this.isDefault = isDefault;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public int getEffectID() {
        return effectID;
    }

    public void setEffectID(int effectID) {
        this.effectID = effectID;
    }

    public int isDefault() {
        return isDefault;
    }

    public void setDefault(int aDefault) {
        isDefault = aDefault;
    }
}
