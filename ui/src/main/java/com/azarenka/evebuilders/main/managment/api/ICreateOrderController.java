package com.azarenka.evebuilders.main.managment.api;

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
import com.azarenka.evebuilders.service.api.IFitLoaderService;

import java.util.List;

public interface ICreateOrderController {

    List<Fit> gitAllFits();

    Fit getFitById(String id);

    List<InvGroup> getInvGroupsById(Integer id);

    List<InvType> getTypesByGroupIds(List<Integer> groupIds);

    List<InvType> getTypesByGroupId(Integer groupId);


    //TODO adjust logic to return Object with error messages instead of boolean
    boolean uploadFit(String text);

    Order createOrder(Order order);

    List<Destination> getAllDestination();

    List<Receiver> getAllReceivers();

    List<ManagedCorporation> getAllManagedCorporations();

    List<UserDto> getAllReceiverUsers();

    OrderPresetDefaultsDto getOrderPresetDefaultsForCurrentUser();

    void addNewDestination(String value);

    void addNewReceiver(String s);

    List<Order> getOriginalOrders();

    void removeOrder(String orderNumber);

    IFitLoaderService getFitLoaderService();

    void updateRequestStatusOrder(RequestOrder requestOrder, RequestOrderStatusEnum status);

    RequestOrder getRequestOrderById(String requestId);

    void sentMessage(Order order);
}
