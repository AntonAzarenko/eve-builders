package com.azarenka.evebuilders.main.constructions.api;

import com.azarenka.evebuilders.domain.mysql.DistributedOrder;
import com.azarenka.evebuilders.domain.mysql.Fit;
import com.azarenka.evebuilders.service.util.ImageService;

import java.util.List;

public interface ICorporationConstructionController {

    List<DistributedOrder> getOrderList();

    Fit getFitById(String id);

    ImageService getImageProviderService();

    void saveOrder(DistributedOrder distributedOrder, Integer value);
}
