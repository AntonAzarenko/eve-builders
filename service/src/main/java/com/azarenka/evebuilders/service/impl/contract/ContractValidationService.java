package com.azarenka.evebuilders.service.impl.contract;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.dto.Contract;
import com.azarenka.evebuilders.domain.dto.ContractItem;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IInvTypeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ContractValidationService {

    @Autowired
    private IInvTypeService invTypesService;
    @Autowired
    private IFitLoaderService fitLoaderService;

    public void validateContract(Contract contract, List<ContractItem> items, Order order,
                                 DistributedOrder distributedOrder, ContractValidationReport report) {
        String fitId = order.getFitId();
        var fitInfo = Objects.nonNull(fitId)
            ? parseFit(fitLoaderService.getFitById(order.getFitId()).getTextFit())
            : new FitInfo(order.getShipName(), Map.of());
        var shipType = invTypesService.getInvTypeByModuleName(fitInfo.shipTypeName());
        int shipCount = items.stream()
            .filter(i -> i.getTypeId() == shipType.getTypeID() && i.isIncluded())
            .mapToInt(ContractItem::getQuantity)
            .sum();
        if (shipCount == 0) {
            report.setErrorMessage("В контракте нет кораблей: " + fitInfo.shipTypeName());
            report.setValid(false);
        }
        double expectedPrice = order.getPrice().multiply(new BigDecimal(shipCount)).doubleValue();
        if (contract.getPrice() != expectedPrice) {
            report.setErrorMessage("Неправильно указана цена" + contract.getPrice());
            report.setValid(false);
        }
        var contractItemMap = items.stream()
            .filter(ContractItem::isIncluded)
            .collect(Collectors.toMap(
                ContractItem::getTypeId,
                ContractItem::getQuantity,
                Integer::sum
            ));
        fitInfo.requiredItems().forEach((itemName, qtyPerShip) -> {
            try {
                var type = invTypesService.getInvTypeByModuleName(itemName);
                int required = qtyPerShip * shipCount;
                int inContract = contractItemMap.getOrDefault(type.getTypeID(), 0);
                if (inContract < required) {
                    report.setErrorMessage(
                        String.format("Недостаточно %s: нужно %d, есть %d", itemName, required, inContract));
                    report.setValid(false);
                }
            } catch (Exception e) {
                report.setErrorMessage("Не удалось найти предмет: " + itemName);
                report.setValid(false);
            }
        });
        report.setCountItems(shipCount);
    }

    public FitInfo parseFit(String textFit) {
        String[] lines = textFit.split("\n");
        var shipName = lines[0].substring(1, lines[0].indexOf(",")).trim();
        var requiredItems = new HashMap<String, Integer>();
        var cargoPattern = Pattern.compile("^(.+?) x(\\d+)$");
        for (int i = 1; i < lines.length; i++) {
            var line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("[")) {
                continue;
            }
            var matcher = cargoPattern.matcher(line);
            if (matcher.matches()) {
                String item = matcher.group(1).trim();
                int qty = Integer.parseInt(matcher.group(2));
                requiredItems.merge(item, qty, Integer::sum);
            } else {
                requiredItems.merge(line, 1, Integer::sum);
            }
        }
        return new FitInfo(shipName, requiredItems);
    }

    record FitInfo(String shipTypeName, Map<String, Integer> requiredItems) {
    }
}
