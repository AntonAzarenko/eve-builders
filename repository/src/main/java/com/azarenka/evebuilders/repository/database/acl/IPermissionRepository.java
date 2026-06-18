package com.azarenka.evebuilders.repository.database.acl;

import com.azarenka.evebuilders.domain.db.Permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    List<Permission> findAllByOrderByGroupNameAscCodeAsc();
}
