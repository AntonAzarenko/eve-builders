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

        // Получаем все ItemDto от пользователей
        List<ItemDto> materials = assetService.getMaterials(materialNames);

        // Получаем InvType, чтобы иметь гарантированное сопоставление по всем materialNames
        List<InvType> invTypes = invTypesService.getInvTypesByTypeNames(materialNames);

        // Получаем цены по всем типам
        Map<Integer, MarketPriceInfo> priceInfoMap =
            marketPriceIntegrationService.getMarketPricesFor(
                invTypes.stream().map(InvType::getTypeID).toList()
            ).stream().collect(Collectors.toMap(MarketPriceInfo::getTypeId, Function.identity()));

        // Для быстрого поиска typeId по имени (если отсутствует ItemDto, но есть invType)
        Map<String, InvType> typeNameToInvTypeMap = invTypes.stream()
            .collect(Collectors.toMap(InvType::getTypeName, Function.identity()));

        // Формируем список CalculationItemInformation по каждому ItemDto
        List<CalculationItemInformation> result = new ArrayList<>();

        for (String typeName : materialNames) {
            // Получаем все ItemDto для текущего typeName
            List<ItemDto> itemsOfThisType = materials.stream()
                .filter(item -> typeName.equals(item.getInvType().getTypeName()))
                .toList();

            if (!itemsOfThisType.isEmpty()) {
                for (ItemDto item : itemsOfThisType) {
                    CalculationItemInformation info = new CalculationItemInformation();
                    info.setTypeName(typeName);
                    info.setItemDto(item);
                    info.setTypeID(item.getInvType().getTypeID());
                    info.setHasQuantity(item.getAsset() != null ? item.getAsset().getQuantity() : 0);
                    info.setRequiredQuantity(materialsCountMap.getOrDefault(typeName, 0));

                    // Маркет
                    MarketPriceInfo priceInfo = priceInfoMap.get(item.getInvType().getTypeID());
                    if (priceInfo != null) {
                        info.setJitaBuyPrice(priceInfo.getBuyPrice());
                        info.setJitaSellPrice(priceInfo.getSellPrice());
                        info.setJitaSplitPrice(
                            priceInfo.getBuyPrice().add(priceInfo.getSellPrice())
                                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                    }

                    // ProductionNode
                    nodes.stream()
                        .filter(n -> typeName.equalsIgnoreCase(n.getTypeName()))
                        .findFirst()
                        .ifPresentOrElse(node -> {
                            info.setProductPerBatch(node.getEffectivePerBatch(typeName));
                            info.setProducedQuantity(node.getOutputQuantity());
                            info.setExcessQuantity(node.getOutputQuantity() - info.getRequiredQuantity());
                        }, () -> {
                            info.setProductPerBatch(0);
                            info.setProducedQuantity(0);
                            info.setExcessQuantity(-info.getRequiredQuantity());
                        });

                    result.add(info);
                }
            } else {
                // Нет ItemDto — создаём пустой info, но с ценой, если найдётся по invType
                InvType invType = typeNameToInvTypeMap.get(typeName);
                if (invType != null) {
                    CalculationItemInformation info = new CalculationItemInformation();
                    info.setTypeName(typeName);
                    info.setTypeID(invType.getTypeID());
                    info.setHasQuantity(0);
                    info.setRequiredQuantity(materialsCountMap.getOrDefault(typeName, 0));

                    MarketPriceInfo priceInfo = priceInfoMap.get(invType.getTypeID());
                    if (priceInfo != null) {
                        info.setJitaBuyPrice(priceInfo.getBuyPrice());
                        info.setJitaSellPrice(priceInfo.getSellPrice());
                        info.setJitaSplitPrice(
                            priceInfo.getBuyPrice().add(priceInfo.getSellPrice())
                                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                    }

                    nodes.stream()
                        .filter(n -> typeName.equalsIgnoreCase(n.getTypeName()))
                        .findFirst()
                        .ifPresentOrElse(node -> {
                            info.setProductPerBatch(node.getEffectivePerBatch(typeName));
                            info.setProducedQuantity(node.getOutputQuantity());
                            info.setExcessQuantity(node.getOutputQuantity() - info.getRequiredQuantity());
                        }, () -> {
                            info.setProductPerBatch(0);
                            info.setProducedQuantity(0);
                            info.setExcessQuantity(-info.getRequiredQuantity());
                        });

                    result.add(info);
                }
            }
        }

        return result.stream()
            .sorted(Comparator.comparing(CalculationItemInformation::getTypeName))
            .toList();
    }

}
