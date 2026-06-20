package com.azarenka.evebuilders.repository.database.acl;

import com.azarenka.evebuilders.domain.acl.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    @org.springframework.data.jpa.repository.Query("select r.id from Role r where upper(r.code) = upper(:code)")
    Optional<Long> findIdByCode(@Param("code") String code);

    List<Role> findAllByOrderByCodeAsc();
}
