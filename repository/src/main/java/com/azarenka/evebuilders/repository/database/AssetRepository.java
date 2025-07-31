package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.AssetEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, String> {

    List<AssetEntity> findAllByUserNameAndTypeIdIn(String userName, List<Integer> typeIds);

    void deleteAllByUserName(String userName);

}
