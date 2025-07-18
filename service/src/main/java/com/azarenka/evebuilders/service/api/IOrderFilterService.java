package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.OrderFilter;

public interface IOrderFilterService {

    void saveFilter(OrderFilter orderFilter);

    OrderFilter getOrderFilter();
}
