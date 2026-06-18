package com.azarenka.evebuilders.repository.database.acl;

import com.azarenka.evebuilders.domain.acl.UserPermission;
import com.azarenka.evebuilders.domain.acl.UserPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
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

    void deleteByIdUserIdAndIdPermissionId(String userId, Long permissionId);

    void deleteByIdUserId(String userId);
}
