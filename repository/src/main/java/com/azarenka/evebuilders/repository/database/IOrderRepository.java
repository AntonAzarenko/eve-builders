package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT count(i) FROM Order i WHERE i.createdDate = :today")
    int findTodayOrdersCount(@Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE Order s SET s.orderStatus = :orderStatus WHERE s.id = :id")
    void update(@Param("orderStatus") OrderStatusEnum orderStatus, @Param("id") String id);

    Optional<Order> findByOrderNumber(String orderNumber);

    void deleteByOrderNumber(String orderNumber);

    List<Order> findAll(Specification<Order> spec);

}
