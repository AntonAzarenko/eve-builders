package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.sqllite.MaterialInfo;
import com.azarenka.evebuilders.main.constructions.api.IBuildConstructionController;
import com.azarenka.evebuilders.service.impl.EveMaterialsDataService;
import com.azarenka.evebuilders.service.impl.ProductionTreeService;
import com.azarenka.evebuilders.service.util.ImageService;
import com.vaadin.flow.component.html.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BuilderConstructionController implements IBuildConstructionController {

    @Autowired
    private ImageService imageService;
    @Autowired
    private EveMaterialsDataService eveMaterialsDataService;
    @Autowired
    private ProductionTreeService productionTreeService;


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
        return productionTreeService.buildTree(moduleName, i);
    }
}
