package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.RequestOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRequestOrderRepository extends JpaRepository<RequestOrder, String> {
}
