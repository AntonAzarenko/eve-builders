package com.azarenka.evebuilders.repository.database.acl;

import com.azarenka.evebuilders.domain.acl.UserPermission;
import com.azarenka.evebuilders.domain.acl.UserPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUserPermissionRepository extends JpaRepository<UserPermission, UserPermissionId> {

    List<UserPermission> findByIdUserId(String userId);

    List<UserPermission> findByIdUserIdOrderByIdPermissionIdAsc(String userId);

    List<UserPermission> findByIdUserIdIn(Collection<String> userIds);

    Optional<UserPermission> findByIdUserIdAndIdPermissionId(String userId, Long permissionId);

    boolean existsByIdUserIdAndIdPermissionId(String userId, Long permissionId);

    boolean existsByIdPermissionId(Long permissionId);

    @Modifying
    @Query("delete from UserPermission up where up.id.userId = :userId and up.id.permissionId = :permissionId")
    void deleteByUserIdAndPermissionId(@Param("userId") String userId, @Param("permissionId") Long permissionId);

    @Modifying
    @Query("delete from UserPermission up where up.id.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
