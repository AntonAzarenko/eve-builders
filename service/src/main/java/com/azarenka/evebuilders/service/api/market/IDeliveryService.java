package com.azarenka.evebuilders.service.api.market;

import com.azarenka.evebuilders.domain.enums.MarketDeliveryStatus;

import java.time.LocalDate;

public interface IDeliveryService {

    String createDelivery(String orderId, long qty, String proofRef, String actorUsername, LocalDate deliveredOn);

    void confirmDelivery(String deliveryId, String actorUsername);

    void rejectDelivery(String deliveryId, String actorUsername, String reason);

    void setStatus(String deliveryId, MarketDeliveryStatus status, String actorUsername);
}
