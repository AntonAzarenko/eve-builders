package com.azarenka.evebuilders.repository.database.properties;

import com.azarenka.evebuilders.domain.db.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReceiverRepository extends JpaRepository<Receiver, String> {

    @Override
    List<Receiver> findAll();
}
