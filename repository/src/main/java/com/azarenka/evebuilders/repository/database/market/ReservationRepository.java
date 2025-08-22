package com.azarenka.evebuilders.repository.database.market;

import com.azarenka.evebuilders.domain.db.Reservation;
import com.azarenka.evebuilders.domain.enums.MarketReservationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {

    List<Reservation> findByStatusAndReservedUntilBefore(MarketReservationStatus status, LocalDate date);

    List<Reservation> findByOffer_Id(String offerId);

    List<Reservation> findByRequest_Id(String requestId);
}
