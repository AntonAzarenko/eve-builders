package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.domain.db.*;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.main.managment.api.ICreateOrderController;
import com.azarenka.evebuilders.service.api.*;
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
    private IRequestOrderService requestOrderService;
    @Autowired
    private IEveMailService mailService;

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

    @Override
    public void removeOrder(String orderNumber) {
        orderService.removeOrder(orderNumber);
    }

    public IFitLoaderService getFitLoaderService() {
        return fitLoaderService;
    }

    public void updateRequestStatusOrder(RequestOrder requestOrder, RequestOrderStatusEnum status) {
        requestOrder.setRequestStatus(status);
        requestOrderService.update(requestOrder);
    }

    @Override
    public RequestOrder getRequestOrderById(String requestId) {
        return requestOrderService.getRequestById(requestId);
    }

    @Override
    public void sentMessage(Order order) {
        mailService.sendMailToCoordinator(order);
    }
}
