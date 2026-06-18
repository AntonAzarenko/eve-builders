package com.azarenka.evebuilders.repository.database.acl;

import com.azarenka.evebuilders.domain.acl.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    List<Role> findAllByOrderByCodeAsc();
}
