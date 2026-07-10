package com.azarenka.evebuilders.main.managment.create;

import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsDto;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.main.managment.api.ICreateOrderController;
import com.azarenka.evebuilders.service.api.ICorporationService;
import com.azarenka.evebuilders.service.api.IEveMailService;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IOrderPresetDefaultsService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import com.azarenka.evebuilders.service.api.IUserService;

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
    private IOrderService orderService;
    @Autowired
    private IFitLoaderService fitLoaderService;
    @Autowired
    private IRequestOrderService requestOrderService;
    @Autowired
    private IEveMailService mailService;
    @Autowired
    private ICorporationService corporationService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IOrderPresetDefaultsService orderPresetDefaultsService;

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
    public boolean uploadFit(String text) {
        return fitLoaderService.upload(text);
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
    public List<ManagedCorporation> getAllManagedCorporations() {
        return corporationService.getAllCorporations();
    }

    @Override
    public List<UserDto> getAllReceiverUsers() {
        return userService.getUsersDto();
    }

    @Override
    public OrderPresetDefaultsDto getOrderPresetDefaultsForCurrentUser() {
        return orderPresetDefaultsService.getDefaultsForCurrentUser();
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
