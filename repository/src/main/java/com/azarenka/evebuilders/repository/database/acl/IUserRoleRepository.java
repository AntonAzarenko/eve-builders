package com.azarenka.evebuilders.repository.database.acl;

import com.azarenka.evebuilders.domain.acl.UserRole;
import com.azarenka.evebuilders.domain.acl.UserRoleId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRoleRepository extends JpaRepository<UserRole, UserRoleId>, UserRoleRepositoryCustom {

    List<UserRole> findByIdUserId(String userId);

    List<UserRole> findByIdUserIdOrderByIdRoleIdAsc(String userId);

    Optional<UserRole> findByIdUserIdAndIdRoleId(String userId, Long roleId);

    boolean existsByIdUserIdAndIdRoleId(String userId, Long roleId);

    @Modifying
    @Query("delete from UserRole ur where ur.id.userId = :userId and ur.id.roleId = :roleId")
    void deleteByUserIdAndRoleId(@Param("userId") String userId, @Param("roleId") Long roleId);

    @Modifying
    @Query("delete from UserRole ur where ur.id.roleId = :roleId")
    void deleteAllByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("delete from UserRole ur where ur.id.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
