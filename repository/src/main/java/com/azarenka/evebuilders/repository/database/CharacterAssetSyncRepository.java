package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.CharacterAssetSync;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterAssetSyncRepository extends JpaRepository<CharacterAssetSync, String> {

    Optional<CharacterAssetSync> findByUserName(String userName);

}
