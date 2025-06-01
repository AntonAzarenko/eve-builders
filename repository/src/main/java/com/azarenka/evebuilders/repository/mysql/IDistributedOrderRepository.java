package com.azarenka.evebuilders.repository.mysql;

import com.azarenka.evebuilders.domain.mysql.DistributedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDistributedOrderRepository extends JpaRepository<DistributedOrder, String> {

    Optional<DistributedOrder> findByOrderNumberAndUserName(String number, String userName);

    List<DistributedOrder> findAllByUserName(String userName);
}
