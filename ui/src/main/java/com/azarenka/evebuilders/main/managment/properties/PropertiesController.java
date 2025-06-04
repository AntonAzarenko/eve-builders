package com.azarenka.evebuilders.main.managment.properties;

import com.azarenka.evebuilders.domain.db.ApplicationProperties;
import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.main.managment.api.IPropertiesController;
import com.azarenka.evebuilders.service.impl.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PropertiesController implements IPropertiesController {

    @Autowired
    private OrderService orderService;

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
}
