package com.azarenka.evebuilders.main.managment.api;

import com.azarenka.evebuilders.domain.db.ApplicationProperties;
import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.Receiver;

import java.util.List;

public interface IPropertiesController {

    List<Receiver> getReceivers();

    List<Destination> getDestinations();

    void addNewProperty(String value, ApplicationProperties property);
}
