package com.azarenka.evebuilders.main.constructions.api;

import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.sqllite.MaterialInfo;
import com.vaadin.flow.component.html.Image;

import java.util.List;

public interface IBuildConstructionController {

    Image getImageByInvTypeName(String name);

    List<MaterialInfo> getMaterialsByTypeName(String name);

    ProductionNode getProductionNode(String moduleName, int i);
}
