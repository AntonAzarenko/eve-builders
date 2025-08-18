package com.azarenka.evebuilders.repository.database.market;

import com.azarenka.evebuilders.domain.db.PurchaseRequest;
import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.enums.MarketRequestStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface PurchaseRequestRepository
    extends JpaRepository<PurchaseRequest, String>, JpaSpecificationExecutor<PurchaseRequest> {

    Page<PurchaseRequest> findByRequesterUsername(String requesterUsername, Pageable pageable);

    Page<PurchaseRequest> findByStatus(MarketRequestStatus status, Pageable pageable);

    Page<PurchaseRequest> findByStatusIn(Collection<MarketRequestStatus> statuses, Pageable pageable);

    Page<PurchaseRequest> findByItemType_MaterialTypeAndStatusIn(MaterialType materialType,
                                                                 Collection<MarketRequestStatus> statuses,
                                                                 Pageable pageable);
}

