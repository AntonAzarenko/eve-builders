package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IShipOrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT count(i) FROM Order i WHERE i.createdDate = :today")
    int findTodayOrdersCount(@Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE Order s SET s.orderStatus = :orderStatus WHERE s.id = :id")
    void update(@Param("orderStatus") OrderStatusEnum orderStatus, @Param("id") String id);

    Optional<Order> findByOrderNumber(String orderNumber);

}
