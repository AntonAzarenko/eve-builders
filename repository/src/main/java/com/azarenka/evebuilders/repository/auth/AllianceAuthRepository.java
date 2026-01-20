package com.azarenka.evebuilders.repository.auth;

import com.azarenka.evebuilders.domain.auth.authdb.UserFlightsInfo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional(readOnly = true, transactionManager = "mariaTransactionManager")
public class AllianceAuthRepository {

    @PersistenceContext(unitName = "mariadb")
    private EntityManager em;

    public List<Integer> findGroupIdsByUsername(String username) {
        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery(
            """
                select ug.group_id
                from eveonline_evecharacter ec
                join authentication_userprofile aup on aup.main_character_id = ec.id
                join auth_user_groups ug on ug.user_id = aup.user_id
                where ec.character_name = :username
            """)
            .setParameter("username", username)
            .getResultList();

        return rows.stream().map(Number::intValue).toList();
    }

    public List<UserFlightsInfo> findUserFlights() {
        List<Object[]> rows = em.createNativeQuery("""
                    select au.username,
                           ee.character_id,
                           count(*) as appearances_count,
                           case when t3t3u.user_id is not null then 1 else 0 end as ts_connected
                    
                    from auth_user au
                    join afat_fat af on au.id = af.character_id
                    join afat_fatlink afl on af.fatlink_id = afl.id
                    left join teamspeak3_teamspeak3user t3t3u on au.id = t3t3u.user_id
                        group by au.username, t3t3u.user_id
                """)
            .getResultList();

        return rows.stream()
            .map(r -> new UserFlightsInfo(
                (String) r[0],
                ((Number) r[1]).longValue(),
                ((Number) r[2]).longValue(),
                ((Number) r[3]).intValue() == 1
            ))
            .collect(Collectors.toList());
    }

    public List<UserFlightsInfo> findUserFlights(LocalDate from, LocalDate to) {
        var fromTs = java.sql.Timestamp.valueOf(from.atStartOfDay());
        var toTs = java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
                   SELECT
                            u.username,
                            COALESCE(ee.character_id, 0)                 AS character_id,
                            COUNT(*)                                      AS appearances_count,
                            CASE WHEN t3.user_id IS NOT NULL THEN 1 ELSE 0 END AS ts_connected
                          FROM (
                            /* УЧАСТНИКИ: user_id = af.character_id, связка по af.fatlink_id -> afl.id */
                            SELECT
                              af.fatlink_id          AS link_id,
                              af.character_id        AS user_id
                            FROM afat_fat af
                          
                            UNION ALL
                          
                            /* АВТОРЫ: user_id = afl.creator_id */
                            SELECT
                              afl.id                 AS link_id,
                              afl.creator_id         AS user_id
                            FROM afat_fatlink afl
                          ) AS inv
                          JOIN afat_fatlink afl   ON afl.id = inv.link_id
                          JOIN auth_user     u    ON u.id  = inv.user_id
                          LEFT JOIN eveonline_evecharacter ee
                                                 ON ee.character_name = u.username
                          LEFT JOIN teamspeak3_teamspeak3user t3
                                                 ON t3.user_id = u.id
                          WHERE afl.created >= :fromTs
                            AND afl.created <  :toTs
                          GROUP BY
                            u.username,
                            COALESCE(ee.character_id, 0),
                            t3.user_id;
                                                                                      
                """)
            .setParameter("fromTs", fromTs)
            .setParameter("toTs", toTs)
            .getResultList();

        return rows.stream()
            .map(r -> new UserFlightsInfo(
                (String) r[0],
                r[1] == null ? null : ((Number) r[1]).longValue(),
                ((Number) r[2]).longValue(),
                ((Number) r[3]).intValue() == 1
            ))
            .collect(Collectors.toList());
    }
}
