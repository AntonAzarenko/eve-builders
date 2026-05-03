package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IManagedCorporationRepository extends JpaRepository<ManagedCorporation, String> {

    List<ManagedCorporation> findAllByOwnerUsernameOrderByCreatedDateDesc(String ownerUsername);

    List<ManagedCorporation> findAllByOrderByCreatedDateDesc();

    boolean existsByOwnerUsernameAndEveCorporationId(String ownerUsername, Long eveCorporationId);
}
