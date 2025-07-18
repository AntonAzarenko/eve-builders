package com.azarenka.evebuilders.repository.database.properties;

import com.azarenka.evebuilders.domain.db.OrderFilter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IOrderFilterRepository extends JpaRepository<OrderFilter, String> {

    Optional<OrderFilter> findByUserId(String userId);
}
