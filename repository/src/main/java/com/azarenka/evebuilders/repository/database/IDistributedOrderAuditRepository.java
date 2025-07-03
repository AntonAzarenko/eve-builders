package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.DistributedOrderAudit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDistributedOrderAuditRepository extends JpaRepository<DistributedOrderAudit, String> {
}
