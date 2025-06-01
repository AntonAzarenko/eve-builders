package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.main.constructions.api.ICorporationConstructionController;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.util.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CorporationConstructionController implements ICorporationConstructionController {

    @Autowired
    private IFitLoaderService fitLoaderService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private IDistributedOrderService distributedOrderService;

    @Override
    public List<DistributedOrder> getOrderList() {
        return distributedOrderService.getAllByUserName();
    }

    @Override
    public Fit getFitById(String id) {
        return fitLoaderService.getFitById(id);
    }

    @Override
    public ImageService getImageProviderService() {
        return imageService;
    }

    @Override
    public void saveOrder(DistributedOrder distributedOrder, Integer value) {
        distributedOrderService.update(distributedOrder, value);
    }
}
