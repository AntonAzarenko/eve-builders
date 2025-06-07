package com.azarenka.evebuilders.main.constructions.api;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.util.ImageService;

import java.util.List;

public interface ICorporationConstructionController {

    List<DistributedOrder> getOrderList(OrderFilter filter);

    Fit getFitById(String id);

    ImageService getImageProviderService();

    void saveOrder(DistributedOrder distributedOrder, Integer value);

    IFitLoaderService getFitLoaderService();
}
