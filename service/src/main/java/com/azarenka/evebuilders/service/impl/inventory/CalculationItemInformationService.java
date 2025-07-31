package com.azarenka.evebuilders.service.impl.inventory;

import com.azarenka.evebuilders.domain.dto.CalculationItemInformation;
import com.azarenka.evebuilders.domain.dto.ItemDto;
import com.azarenka.evebuilders.domain.dto.MarketPriceInfo;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.service.api.ICalculationItemInformationService;
import com.azarenka.evebuilders.service.api.IInvTypeService;
import com.azarenka.evebuilders.service.impl.intergarion.MarketPriceIntegrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CalculationItemInformationService implements ICalculationItemInformationService {

    @Autowired
    private AssetService assetService;
    @Autowired
    private MarketPriceIntegrationService marketPriceIntegrationService;
    @Autowired
    private IInvTypeService invTypesService;

    @Override
    public List<CalculationItemInformation> collectInformation(List<ProductionNode> nodes,
                                                               Map<String, Integer> materialsCountMap) {
        List<String> materialNames = new ArrayList<>(materialsCountMap.keySet());
        List<ItemDto> materials = assetService.getMaterials(materialNames);
        Set<Integer> typeIds = invTypesService.getInvTypesByTypeNames(materialNames).stream()
            .map(InvType::getTypeID)
            .collect(Collectors.toSet());
        Map<Integer, MarketPriceInfo> priceInfoMap =
            marketPriceIntegrationService.getMarketPricesFor(new ArrayList<>(typeIds))
                .stream()
                .collect(Collectors.toMap(MarketPriceInfo::getTypeId, Function.identity()));
        Map<String, List<ItemDto>> materialGrouped = materials.stream()
            .collect(Collectors.groupingBy(item -> item.getInvType().getTypeName()));
        return materialNames.stream()
            .map(typeName -> {
                List<ItemDto> group = materialGrouped.getOrDefault(typeName, Collections.emptyList());
                int totalHasQuantity = group.stream()
                    .map(item -> item.getAsset() != null ? item.getAsset().getQuantity() : 0)
                    .reduce(0, Integer::sum);
                Integer requiredQuantity = materialsCountMap.getOrDefault(typeName, 0);
                CalculationItemInformation info = new CalculationItemInformation();
                info.setTypeName(typeName);
                info.setHasQuantity(totalHasQuantity);
                info.setRequiredQuantity(requiredQuantity);
                group.stream().findFirst()
                    .ifPresent(item -> {
                        Integer typeId = item.getInvType().getTypeID();
                        info.setTypeID(typeId);
                        MarketPriceInfo priceInfo = priceInfoMap.get(typeId);
                        if (priceInfo != null) {
                            info.setJitaBuyPrice(priceInfo.getBuyPrice());
                            info.setJitaSellPrice(priceInfo.getSellPrice());
                            BigDecimal splitPrice = priceInfo.getBuyPrice()
                                .add(priceInfo.getSellPrice())
                                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
                            info.setJitaSplitPrice(splitPrice);
                        }
                    });
                Optional<ProductionNode> maybeNode = nodes.stream()
                    .filter(n -> typeName.equalsIgnoreCase(n.getTypeName()))
                    .findFirst();
                if (maybeNode.isPresent()) {
                    ProductionNode node = maybeNode.get();
                    info.setProductPerBatch(node.getEffectivePerBatch(typeName));
                    info.setProducedQuantity(node.getOutputQuantity());
                    info.setExcessQuantity(node.getOutputQuantity() - requiredQuantity);
                } else {
                    info.setProductPerBatch(0);
                    info.setProducedQuantity(0);
                    info.setExcessQuantity(-requiredQuantity); // нехватка
                }
                return info;
            })
            .sorted(Comparator.comparing(CalculationItemInformation::getTypeName))
            .toList();
    }
}
