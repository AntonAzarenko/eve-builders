package com.azarenka.evebuilders.main.constructions.api;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.service.api.IFitLoaderService;

import java.util.List;

public interface ICorporationConstructionController {

    List<DistributedOrder> getOrderList(OrderFilter filter);

    Fit getFitById(String id);

    void saveOrder(DistributedOrder distributedOrder, Integer value);

    IFitLoaderService getFitLoaderService();

    void discardOrder(DistributedOrder order);

    boolean sendOrderForApproval(DistributedOrder orderNumber);

    void saveFilter(OrderFilter filter);

    OrderFilter getFilter();

    String getDestination(String orderNumber);

    String getReceiver(String orderNumber);
}
