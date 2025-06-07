package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDistributedOrderRepository extends JpaRepository<DistributedOrder, String> {

    Optional<DistributedOrder> findByOrderNumberAndUserName(String number, String userName);

    List<DistributedOrder> findAllByUserName(String userName);

    List<DistributedOrder> findAll(Specification<DistributedOrder> spec);

    List<DistributedOrder> findAllByOrderNumber(String orderNumber);
}
