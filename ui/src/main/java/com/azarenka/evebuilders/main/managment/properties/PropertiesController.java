package com.azarenka.evebuilders.main.managment.properties;

import com.azarenka.evebuilders.domain.db.ApplicationProperties;
import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsDto;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsHistoryDto;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.main.managment.api.IPropertiesController;
import com.azarenka.evebuilders.service.api.ICorporationService;
import com.azarenka.evebuilders.service.api.IOrderPresetDefaultsService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.order.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PropertiesController implements IPropertiesController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private IOrderPresetDefaultsService orderPresetDefaultsService;
    @Autowired
    private ICorporationService corporationService;
    @Autowired
    private IUserService userService;

    @Override
    public List<Receiver> getReceivers() {
        return orderService.getAllReceivers();
    }

    @Override
    public List<Destination> getDestinations() {
        return orderService.getAllDestination();
    }

    @Override
    public void addNewProperty(String value, ApplicationProperties property) {
        //todo: reimplement. use OOP instead a lot of if statements
        if (property instanceof Destination) {
            orderService.addNewDestination(value);
        }
        if (property instanceof Receiver) {
            orderService.addNewReceiver(value);
        }
    }

    @Override
    public void updateDestination(String destinationId, String value) {
        orderService.updateDestination(destinationId, value);
    }

    @Override
    public void removeDestination(String destinationId) {
        orderService.removeDestination(destinationId);
    }

    @Override
    public OrderPresetDefaultsDto getOrderPresetDefaultsForCurrentUser() {
        return orderPresetDefaultsService.getDefaultsForCurrentUser();
    }

    @Override
    public OrderPresetDefaultsDto saveOrderPresetDefaultsForCurrentUser(OrderPresetDefaultsDto dto) {
        return orderPresetDefaultsService.saveDefaultsForCurrentUser(dto);
    }

    @Override
    public List<OrderPresetDefaultsHistoryDto> getOrderPresetDefaultsHistoryForCurrentUser() {
        return orderPresetDefaultsService.getHistoryForCurrentUser();
    }

    @Override
    public List<ManagedCorporation> getAllManagedCorporations() {
        return corporationService.getAllCorporations();
    }

    @Override
    public List<UserDto> getAllReceiverUsers() {
        return userService.getUsersDto();
    }
}
