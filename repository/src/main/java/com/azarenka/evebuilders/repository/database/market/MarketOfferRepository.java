package com.azarenka.evebuilders.repository.database.market;

import com.azarenka.evebuilders.domain.db.MarketOffer;
import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.enums.MarketOfferStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface MarketOfferRepository
    extends JpaRepository<MarketOffer, String>, JpaSpecificationExecutor<MarketOffer> {

    Page<MarketOffer> findByStatus(MarketOfferStatus status, Pageable pageable);

    Page<MarketOffer> findByStatusIn(Collection<MarketOfferStatus> statuses, Pageable pageable);

    Page<MarketOffer> findBySellerUsername(String sellerUsername, Pageable pageable);

    Page<MarketOffer> findByItemType_MaterialTypeAndStatusIn(MaterialType materialType,
                                                             Collection<MarketOfferStatus> statuses, Pageable pageable);
}
