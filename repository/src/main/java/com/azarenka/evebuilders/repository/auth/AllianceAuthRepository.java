package com.azarenka.evebuilders.repository.auth;

import com.azarenka.evebuilders.domain.authdb.UserFlightsInfo;

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
        List<Number> rows = em.createNativeQuery("""
                select ug.group_id
                from auth_user_groups ug
                join auth_user au on ug.user_id = au.id
                where au.username = :username
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
                    select au.username,
                           ee.character_id,
                           count(*) as appearances_count,
                           case when t3t3u.user_id is not null then 1 else 0 end as ts_connected
                    from auth_user au
                    join afat_fat af on au.id = af.character_id
                    join afat_fatlink afl on af.fatlink_id = afl.id
                        join eveonline_evecharacter ee on au.username = ee.character_name
                    left join teamspeak3_teamspeak3user t3t3u on au.id = t3t3u.user_id
                    WHERE afl.created >= :fromTs
                       AND afl.created <  :toTs
                        group by au.username, t3t3u.user_id
                
                """)
            .setParameter("fromTs", fromTs)
            .setParameter("toTs", toTs)
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
}
