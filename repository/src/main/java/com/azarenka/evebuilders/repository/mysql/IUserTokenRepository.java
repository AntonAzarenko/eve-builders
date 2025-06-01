package com.azarenka.evebuilders.repository.mysql;

import com.azarenka.evebuilders.domain.mysql.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserTokenRepository extends JpaRepository<UserToken, String> {
}
