package com.azarenka.evebuilders.main.constructions.api;

import com.azarenka.evebuilders.domain.dto.CalculationItemInformation;
import com.azarenka.evebuilders.domain.dto.ItemDto;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.domain.sqllite.MaterialInfo;
import com.azarenka.evebuilders.service.util.ImageService;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;

import java.util.List;
import java.util.Map;

public interface IBuildConstructionController {

    Image getImageByInvTypeName(String name);

    List<MaterialInfo> getMaterialsByTypeName(String name);

    ProductionNode getProductionNode(String moduleName, int i);

    List<InvGroup> getInvGroupsById(Integer id);

    List<InvType> getTypesByGroupIds(List<Integer> groupIds);

    ImageService getImageService();

    List<CalculationItemInformation> collectInformation(List<ProductionNode> nodes,
                                                        Map<String, Integer> materialsCountMap);

    Image createIcon(String name);
}
