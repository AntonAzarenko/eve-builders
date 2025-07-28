package com.azarenka.evebuilders.service.impl.inventory;

import com.azarenka.evebuilders.domain.dto.CalculationItemInformation;
import com.azarenka.evebuilders.domain.dto.ItemDto;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.service.api.ICalculationItemInformationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CalculationItemInformationService implements ICalculationItemInformationService {

    @Autowired
    private AssetService assetService;

    @Override
    public List<CalculationItemInformation> collectInformation(List<ProductionNode> nodes,
                                                               Map<String, Integer> materialsCountMap) {
        List<String> materialsIds = new ArrayList<>(materialsCountMap.keySet());
        List<ItemDto> materials = assetService.getMaterials(materialsIds);
        return materials.stream()
            .map(item -> {
                String typeName = item.getInvType().getTypeName();
                Integer typeId = item.getInvType().getTypeID();

                CalculationItemInformation info = new CalculationItemInformation();
                info.setTypeID(typeId);
                info.setTypeName(typeName);
                info.setHasQuantity(item.getAsset() != null ? item.getAsset().getQuantity() : 0);
                Integer required = materialsCountMap.getOrDefault(typeName, 0);
                info.setRequiredQuantity(required);
                Optional<ProductionNode> maybeNode = nodes.stream()
                    .filter(n -> typeName.equalsIgnoreCase(n.getTypeName()))
                    .findFirst();
                if (maybeNode.isPresent()) {
                    ProductionNode node = maybeNode.get();
                    info.setProductPerBatch(node.getEffectivePerBatch(typeName));
                    info.setProducedQuantity(node.getOutputQuantity());
                    double excess = node.getOutputQuantity() - required;
                    info.setExcessQuantity(excess);
                } else {
                    info.setProductPerBatch(0);
                    info.setProducedQuantity(0);
                    info.setExcessQuantity(-required); // нехватка
                }
                return info;
            })
            .toList();
    }
}
