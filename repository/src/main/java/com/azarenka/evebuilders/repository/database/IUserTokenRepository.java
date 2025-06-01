package com.azarenka.evebuilders.repository.database;

import com.azarenka.evebuilders.domain.db.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserTokenRepository extends JpaRepository<UserToken, String> {
}
