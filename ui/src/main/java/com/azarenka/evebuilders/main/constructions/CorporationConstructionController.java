package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.main.constructions.api.ICorporationConstructionController;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.ITelegramIntegrationService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.util.ImageService;
import com.azarenka.evebuilders.service.util.TelegramMessageCreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CorporationConstructionController implements ICorporationConstructionController {

    @Value("${app.telegram_thread_request_id}")
    private String threadRequestId;

    @Autowired
    private IFitLoaderService fitLoaderService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private IDistributedOrderService distributedOrderService;
    @Autowired
    private ITelegramIntegrationService telegramIntegrationService;

    @Override
    public List<DistributedOrder> getOrderList(OrderFilter filter) {
        return distributedOrderService.getAllByUserName(filter);
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
        telegramIntegrationService.sendMessage(
                TelegramMessageCreatorService.createFinishOrderMessage(
                        distributedOrder, value, SecurityUtils.getUserName()), threadRequestId);
    }

    public IFitLoaderService getFitLoaderService() {
        return fitLoaderService;
    }
}
