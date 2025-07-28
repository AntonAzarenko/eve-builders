package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.domain.dto.CalculationItemInformation;
import com.azarenka.evebuilders.domain.dto.ItemDto;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.domain.sqllite.MaterialInfo;
import com.azarenka.evebuilders.main.constructions.api.IBuildConstructionController;
import com.azarenka.evebuilders.service.api.ICalculationItemInformationService;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.azarenka.evebuilders.service.api.IProductionTreeService;
import com.azarenka.evebuilders.service.impl.EveMaterialsDataService;
import com.azarenka.evebuilders.service.impl.inventory.AssetService;
import com.azarenka.evebuilders.service.util.ImageService;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BuilderConstructionController implements IBuildConstructionController {

    @Autowired
    private ImageService imageService;
    @Autowired
    private EveMaterialsDataService eveMaterialsDataService;
    @Autowired
    private IProductionTreeService productionTreeService;
    @Autowired
    private IEveMaterialDataService dataService;
    @Autowired
    private ICalculationItemInformationService calculationItemInformationService;

    @Override
    public Image getImageByInvTypeName(String name) {
        return imageService.createImage32(name);
    }

    @Override
    public List<MaterialInfo> getMaterialsByTypeName(String name) {
        return eveMaterialsDataService.getMaterialsForType(name);
    }

    @Override
    public ProductionNode getProductionNode(String moduleName, int i) {
        return productionTreeService.buildProductionTreeCached(moduleName, i);
    }

    @Override
    public List<InvGroup> getInvGroupsById(Integer id) {
        return dataService.getInvGroupsById(id);
    }

    @Override
    public List<InvType> getTypesByGroupIds(List<Integer> groupIds) {
        return dataService.getTypesByGroupIds(groupIds);
    }

    public ImageService getImageService() {
        return imageService;
    }

    @Override
    public List<CalculationItemInformation> collectInformation(List<ProductionNode> nodes,
                                                        Map<String, Integer> materialsCountMap) {
        return calculationItemInformationService.collectInformation(nodes, materialsCountMap);
    }

    @Override
    public Image createIcon(String name) {
        var icon = getImageByInvTypeName(name);
        icon.setWidth("25px");
        icon.setHeight("25px");
        return icon;
    }
}
