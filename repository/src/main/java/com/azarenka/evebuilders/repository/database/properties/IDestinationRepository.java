package com.azarenka.evebuilders.repository.database.properties;

import com.azarenka.evebuilders.domain.db.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDestinationRepository extends JpaRepository<Destination, String> {

    @Override
    List<Destination> findAll();
}
