package com.azarenka.evebuilders.repository.database.acl;

import com.azarenka.evebuilders.domain.acl.UserRole;
import com.azarenka.evebuilders.domain.acl.UserRoleId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByIdUserId(String userId);

    List<UserRole> findByIdUserIdOrderByIdRoleIdAsc(String userId);

    Optional<UserRole> findByIdUserIdAndIdRoleId(String userId, Long roleId);

    boolean existsByIdUserIdAndIdRoleId(String userId, Long roleId);

    void deleteByIdUserIdAndIdRoleId(String userId, Long roleId);

    void deleteByIdRoleId(Long roleId);

    void deleteByIdUserId(String userId);
}
