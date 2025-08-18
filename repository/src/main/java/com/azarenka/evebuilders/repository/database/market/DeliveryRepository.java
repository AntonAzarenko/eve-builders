package com.azarenka.evebuilders.repository.database.market;

import com.azarenka.evebuilders.domain.db.Delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, String> {

    List<Delivery> findByOrder_Id(String orderId);
}
