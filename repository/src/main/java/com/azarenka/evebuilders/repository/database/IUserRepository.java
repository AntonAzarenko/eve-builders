package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, String> {

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "directPermissions", "directPermissions.permission"})
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "directPermissions", "directPermissions.permission"})
    Optional<User> findByUid(String uid);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "directPermissions", "directPermissions.permission"})
    Optional<User> findByCharacterId(String characterId);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "directPermissions", "directPermissions.permission"})
    List<User> findAllByOrderByUsernameAscUidAsc();

    @Query("""
       select distinct alt
       from User main
       join User alt on alt.mainId = main.uid
       where main.username = :mainUsername
         and alt.isMainCharacter = false
       """)
    List<User> findAltsByMainUsername(@Param("mainUsername") String mainUsername);

}
