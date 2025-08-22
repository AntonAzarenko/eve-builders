package com.azarenka.evebuilders.service.api.market;

import com.azarenka.evebuilders.domain.enums.MarketReservationStatus;

import java.time.LocalDate;

public interface IReservationService {

    String reserveFromOffer(String offerId, String reserverUsername, long qty, LocalDate reservedUntil);

    String reserveAgainstRequest(String requestId, String reserverUsername, long qty, LocalDate reservedUntil);

    void cancel(String reservationId, String actorUsername);

    int expireOverdue(LocalDate today);

    void setStatus(String reservationId, MarketReservationStatus status, String actorUsername);

}
