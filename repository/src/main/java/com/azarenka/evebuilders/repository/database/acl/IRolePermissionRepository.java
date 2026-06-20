package com.azarenka.evebuilders.repository.database.acl;

import com.azarenka.evebuilders.domain.acl.RolePermission;
import com.azarenka.evebuilders.domain.acl.RolePermissionId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    List<RolePermission> findByIdRoleId(Long roleId);

    List<RolePermission> findByIdRoleIdIn(Collection<Long> roleIds);

    Optional<RolePermission> findByIdRoleIdAndIdPermissionId(Long roleId, Long permissionId);

    boolean existsByIdRoleIdAndIdPermissionId(Long roleId, Long permissionId);

    boolean existsByIdPermissionId(Long permissionId);

    @Modifying
    @Query("delete from RolePermission rp where rp.id.roleId = :roleId and rp.id.permissionId = :permissionId")
    void deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Modifying
    @Query("delete from RolePermission rp where rp.id.roleId = :roleId")
    void deleteAllByRoleId(@Param("roleId") Long roleId);
}
