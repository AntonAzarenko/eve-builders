package com.azarenka.evebuilders.repository.litesql;

import com.azarenka.evebuilders.domain.sqllite.BlueprintInfo;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.domain.sqllite.MaterialClassificationInfo;
import com.azarenka.evebuilders.domain.sqllite.MaterialInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvTypesRepository extends JpaRepository<InvType, String> {

    List<InvType> findByTypeIDIn(List<Integer> ids);

    List<InvType> findByGroupId(Integer id);

    List<InvType> findByGroupIdIn(List<Integer> ids);

    @Query("SELECT i.typeID FROM InvType i WHERE i.groupId = 18")
    List<Integer> findMineralTypeIds();

    List<InvType> findByTypeName(String name);

    @Query("SELECT t.typeID FROM InvType t WHERE t.typeName = :typeName")
    Integer findTypeIdByName(@Param("typeName") String typeName);


    @Query(value = """
        SELECT bp.typeID AS typeID, bp.typeName AS typeName
        FROM industryActivityProducts p
        JOIN invTypes bp ON p.typeID = bp.typeID
        WHERE p.productTypeID = (
            SELECT typeID FROM invTypes WHERE typeName = :typeName
        )
        AND p.activityID = 1
        """, nativeQuery = true)
    List<BlueprintInfo> findBlueprintForProduct(@Param("typeName") String typeName);

    @Query(value = """
    SELECT g.groupName, c.categoryName
    FROM invTypes t
    JOIN invGroups g ON t.groupID = g.groupID
    JOIN invCategories c ON g.categoryID = c.categoryID
    WHERE t.typeName = :typeName
    """, nativeQuery = true)
    MaterialClassificationInfo findGroupAndCategoryByTypeName(@Param("typeName") String typeName);

    @Query(value = """
        SELECT
            m.materialTypeID,
            t2.typeName AS materialName,
            m.quantity,
            bp.typeID AS blueprintTypeID,
            bp.typeName AS blueprintName
        FROM invTypes target
        JOIN industryActivityProducts prod ON prod.productTypeID = target.typeID AND prod.activityID = 1
        JOIN invTypes bp ON prod.typeID = bp.typeID
        JOIN industryActivityMaterials m ON bp.typeID = m.typeID AND m.activityID = 1
        JOIN invTypes t2 ON m.materialTypeID = t2.typeID
        WHERE target.typeName = :typeName
        """, nativeQuery = true)
    List<MaterialInfo> findManufacturingMaterials(@Param("typeName") String typeName);

    @Query(value = """
       SELECT
         m.materialTypeID,
         t2.typeName AS materialName,
         m.quantity,
         bp.typeID AS blueprintTypeID,
         bp.typeName AS blueprintName,
         prod.quantity AS outputQuantity
       FROM invTypes target
       JOIN industryActivityProducts prod ON prod.productTypeID = target.typeID AND prod.activityID = 11
       JOIN invTypes bp ON prod.typeID = bp.typeID
       JOIN industryActivityMaterials m ON bp.typeID = m.typeID AND m.activityID = 11
       JOIN invTypes t2 ON m.materialTypeID = t2.typeID
       WHERE target.typeName = :typeName
        """, nativeQuery = true)
    List<MaterialInfo> findReactionMaterials(@Param("typeName") String typeName);

}
