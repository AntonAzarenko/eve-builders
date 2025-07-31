package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.dto.CalculationItemInformation;
import com.azarenka.evebuilders.domain.dto.ItemDto;
import com.azarenka.evebuilders.domain.dto.ProductionNode;

import java.util.List;
import java.util.Map;

public interface ICalculationItemInformationService {

    List<CalculationItemInformation> collectInformation(List<ProductionNode> nodes, Map<String, Integer> materialsCountMap);
}
