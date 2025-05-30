package com.azarenka.evebuilders.repository.litesql;

import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvGroupRepository extends JpaRepository<InvGroup, Integer> {

    @Query("SELECT i FROM InvGroup i WHERE i.categoryID = :id")
    List<InvGroup> findAllByCategoryID(@Param("id") Integer id);
}
