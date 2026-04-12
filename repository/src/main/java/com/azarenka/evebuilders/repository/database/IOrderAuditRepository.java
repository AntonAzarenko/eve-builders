package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.OrderAudit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrderAuditRepository  extends JpaRepository<OrderAudit, String> {
    List<OrderAudit> findByOrderNumberOrderByCreatedDateDesc(String orderNumber);
}
