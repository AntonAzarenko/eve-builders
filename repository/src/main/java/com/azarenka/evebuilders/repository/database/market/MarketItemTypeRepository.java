package com.azarenka.evebuilders.repository.database.market;

import com.azarenka.evebuilders.domain.db.MarketItemType;
import com.azarenka.evebuilders.domain.dto.MaterialType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketItemTypeRepository extends JpaRepository<MarketItemType, String> {

    Optional<MarketItemType> findByTypeId(String typeId);

    List<MarketItemType> findByMaterialTypeAndActiveTrue(MaterialType materialType);

    List<MarketItemType> findByActiveTrue();
}
