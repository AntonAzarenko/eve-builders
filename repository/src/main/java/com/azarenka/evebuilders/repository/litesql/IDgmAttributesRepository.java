package com.azarenka.evebuilders.repository.litesql;

import com.azarenka.evebuilders.domain.sqllite.DgmTypeAttribute;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDgmAttributesRepository extends CrudRepository<DgmTypeAttribute, Integer> {

    @Query("SELECT i.attributeId FROM DgmTypeAttribute i WHERE i.typeId = :id")
    List<Integer> findAttributeIdByTypeId(@Param("id")Integer id);
}
