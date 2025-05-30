package com.azarenka.evebuilders.repository.mysql.properties;

import com.azarenka.evebuilders.domain.mysql.Destination;
import com.azarenka.evebuilders.domain.mysql.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReceiverRepository extends JpaRepository<Receiver, String> {

    @Override
    List<Receiver> findAll();
}
