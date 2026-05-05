package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsDto;
import com.azarenka.evebuilders.domain.dto.OrderPresetDefaultsHistoryDto;

import java.util.List;

public interface IOrderPresetDefaultsService {

    OrderPresetDefaultsDto getDefaultsForCurrentUser();

    OrderPresetDefaultsDto saveDefaultsForCurrentUser(OrderPresetDefaultsDto dto);

    List<OrderPresetDefaultsHistoryDto> getHistoryForCurrentUser();
}
