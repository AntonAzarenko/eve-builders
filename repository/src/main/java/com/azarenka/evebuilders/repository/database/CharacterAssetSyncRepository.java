package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.CharacterAssetSync;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterAssetSyncRepository extends JpaRepository<CharacterAssetSync, String> {

    CharacterAssetSync findByUserName(String userName);

}
