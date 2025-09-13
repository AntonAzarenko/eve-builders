package com.azarenka.evebuilders.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional(readOnly = true, transactionManager = "mariaTransactionManager")
public class AllianceAuthRepository {

    @PersistenceContext(unitName = "mariadb")
    private EntityManager em;

    public List<Integer> findGroupIdsByUsername(String username) {
        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery("""
                select ug.group_id
                from auth_user_groups ug
                join auth_user au on ug.user_id = au.id
                where au.username = :username
                """)
            .setParameter("username", username)
            .getResultList();

        // MariaDB часто возвращает числовые колонки как BigInteger/Long
        return rows.stream().map(Number::intValue).toList();
    }
}
