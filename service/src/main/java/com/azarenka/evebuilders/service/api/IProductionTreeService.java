package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.dto.ProductionNode;

public interface IProductionTreeService {

    ProductionNode buildProductionTree(String typeName, int quantity);
}
