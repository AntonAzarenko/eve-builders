package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.main.managment.api.ICreateOrderController;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.ITelegramIntegrationService;
import com.azarenka.evebuilders.service.util.ImageService;
import com.vaadin.flow.component.html.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Controller
public class CreateOrderViewController implements ICreateOrderController {

    @Autowired
    private IEveMaterialDataService dataService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IFitLoaderService fitLoaderService;
    @Autowired
    private ITelegramIntegrationService telegramIntegrationService;

    @Override
    public List<Fit> gitAllFits() {
        return fitLoaderService.getAll();
    }

    @Override
    public Fit getFitById(String id) {
        return fitLoaderService.getFitById(id);
    }

    @Override
    public List<InvGroup> getInvGroupsById(Integer id) {
        return dataService.getInvGroupsById(id);
    }

    @Override
    public List<InvType> getTypesByGroupIds(List<Integer> groupIds) {
        return dataService.getTypesByGroupIds(groupIds);
    }

    @Override
    public List<InvType> getTypesByGroupId(Integer groupId) {
        return dataService.getTypesByGroupId(groupId);
    }

    @Override
    public Image getBigImageByParameters(InvType invType, String size) {
        return imageService.createImage64(invType, size);
    }

    @Override
    public boolean uploadFit(String text) {
        return fitLoaderService.upload(text);
    }

    @Override
    public ImageService getImageProviderService() {
        return imageService;
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        Order byOrderNumber = orderService.getByOrderNumber(order.getOrderNumber());
        if (Objects.nonNull(byOrderNumber)) {
            return orderService.updateOrder(order);
        } else {
            return orderService.saveOrder(order);
        }
    }

    @Override
    public List<Destination> getAllDestination() {
        return orderService.getAllDestination();
    }

    @Override
    public List<Receiver> getAllReceivers() {
        return orderService.getAllReceivers();
    }

    @Override
    public void sendMessage(String s) {
        telegramIntegrationService.sendMessage(s);
    }

    @Override
    public void addNewDestination(String value) {
        orderService.addNewDestination(value);
    }

    @Override
    public void addNewReceiver(String value) {
        orderService.addNewReceiver(value);
    }

    @Override
    public List<Order> getOriginalOrders() {
        return orderService.getOriginalOrderList();
    }
}
