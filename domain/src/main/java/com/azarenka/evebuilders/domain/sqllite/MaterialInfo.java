package com.azarenka.evebuilders.domain.sqllite;

public interface MaterialInfo {

    Integer getMaterialTypeID();

    String getMaterialName();

    Integer getQuantity();

    Integer getBlueprintTypeID();

    String getBlueprintName();

    Integer getOutputQuantity();
}
