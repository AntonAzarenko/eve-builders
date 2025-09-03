package com.azarenka.evebuilders.service.api.market;

import com.azarenka.evebuilders.domain.dto.market.DealRowDTO;
import com.azarenka.evebuilders.domain.enums.MarketOrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ITradeOrderService {

    String convertReservationToOrder(String reservationId, String actorUsername);

    Page<DealRowDTO> listMyOrders(String username, MarketOrderStatus[] statuses, Pageable pageable);

    void cancel(String orderId, String actorUsername, boolean force);

    BigDecimal calculateTotal(String orderId);
}
