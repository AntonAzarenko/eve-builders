package com.azarenka.evebuilders.repository.database.properties;

import com.azarenka.evebuilders.domain.db.OrderPresetDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IOrderPresetDefaultsRepository extends JpaRepository<OrderPresetDefaults, String> {
    Optional<OrderPresetDefaults> findByOwnerUsername(String ownerUsername);
}
