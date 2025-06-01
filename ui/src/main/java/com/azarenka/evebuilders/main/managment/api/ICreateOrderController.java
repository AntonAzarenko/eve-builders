package com.azarenka.evebuilders.main.managment.api;

import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.service.util.ImageService;
import com.vaadin.flow.component.html.Image;

import java.util.List;

public interface ICreateOrderController {

    List<Fit> gitAllFits();

    Fit getFitById(String id);

    List<InvGroup> getInvGroupsById(Integer id);

    List<InvType> getTypesByGroupIds(List<Integer> groupIds);

    List<InvType> getTypesByGroupId(Integer groupId);

    Image getBigImageByParameters(InvType invType, String size);

    //TODO adjust logic to return Object with error messages instead of boolean
    boolean uploadFit(String text);

    ImageService getImageProviderService();

    Order createOrder(Order order);

    List<Destination> getAllDestination();

    List<Receiver> getAllReceivers();

    void sendMessage(String s);

    void addNewDestination(String value);

    void addNewReceiver(String s);

    List<Order> getOriginalOrders();
}
