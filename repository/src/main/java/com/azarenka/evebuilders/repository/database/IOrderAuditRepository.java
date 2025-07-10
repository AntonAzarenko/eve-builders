package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.OrderAudit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IOrderAuditRepository  extends JpaRepository<OrderAudit, String> {
}
