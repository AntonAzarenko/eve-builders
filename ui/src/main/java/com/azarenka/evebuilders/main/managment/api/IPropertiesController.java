package com.azarenka.evebuilders.main.managment.api;

import com.azarenka.evebuilders.domain.db.ApplicationProperties;
import com.azarenka.evebuilders.domain.db.Destination;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.db.Receiver;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsDto;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsHistoryDto;
import com.azarenka.evebuilders.domain.dto.UserDto;

import java.util.List;

public interface IPropertiesController {

    List<Receiver> getReceivers();

    List<Destination> getDestinations();

    void addNewProperty(String value, ApplicationProperties property);

    void updateDestination(String destinationId, String value);

    void removeDestination(String destinationId);

    OrderPresetDefaultsDto getOrderPresetDefaultsForCurrentUser();

    OrderPresetDefaultsDto saveOrderPresetDefaultsForCurrentUser(OrderPresetDefaultsDto dto);

    List<OrderPresetDefaultsHistoryDto> getOrderPresetDefaultsHistoryForCurrentUser();

    List<ManagedCorporation> getAllManagedCorporations();

    List<UserDto> getAllReceiverUsers();
}
