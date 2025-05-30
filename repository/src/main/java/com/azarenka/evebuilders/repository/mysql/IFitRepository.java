package com.azarenka.evebuilders.repository.mysql;

import com.azarenka.evebuilders.domain.mysql.Fit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFitRepository extends JpaRepository<Fit, String> {

    @Override
    Optional<Fit> findById(String s);

    @Override
    List<Fit> findAll();
}
