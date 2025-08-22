package com.azarenka.evebuilders.repository.database.market;

import com.azarenka.evebuilders.domain.db.TradeOrder;
import com.azarenka.evebuilders.domain.enums.MarketOrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TradeOrderRepository extends JpaRepository<TradeOrder, String>, JpaSpecificationExecutor<TradeOrder> {

    Page<TradeOrder> findByStatusIn(Collection<MarketOrderStatus> statuses, Pageable pageable);

    Page<TradeOrder> findByBuyerUsernameOrSellerUsername(String buyerUsername, String sellerUsername,
                                                         Pageable pageable);
}

