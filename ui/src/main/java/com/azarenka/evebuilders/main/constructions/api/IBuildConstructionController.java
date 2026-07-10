package com.azarenka.evebuilders.main.constructions.api;

import com.azarenka.evebuilders.domain.dto.CalculationItemInformation;
import com.azarenka.evebuilders.domain.dto.LocationInfo;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.domain.sqllite.MaterialInfo;

import java.util.List;
import java.util.Map;

public interface IBuildConstructionController {


    List<MaterialInfo> getMaterialsByTypeName(String name);

    ProductionNode getProductionNode(String moduleName, int i);

    List<InvGroup> getInvGroupsById(Integer id);

    List<InvType> getTypesByGroupIds(List<Integer> groupIds);


    List<CalculationItemInformation> collectInformation(List<ProductionNode> nodes,
                                                        Map<String, Integer> materialsCountMap);

    LocationInfo getLocationInfoById(Long id, String userName);
}
