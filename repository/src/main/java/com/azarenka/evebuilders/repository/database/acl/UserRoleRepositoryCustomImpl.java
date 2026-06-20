package com.azarenka.evebuilders.repository.database.acl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.Array;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRoleRepositoryCustomImpl implements UserRoleRepositoryCustom {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public UserRoleSyncResult syncUserRoles(String userId, Set<String> roleCodes) {
        Set<String> normalizedRoleCodes = normalizeRoleCodes(roleCodes);
        entityManager.flush();

        String inputRolesSql = normalizedRoleCodes.isEmpty()
            ? "SELECT CAST(NULL AS varchar) WHERE false"
            : normalizedRoleCodes.stream()
                .map(code -> "SELECT CAST(:" + parameterName(code) + " AS varchar) AS code")
                .collect(Collectors.joining(" UNION ALL "));

        String sql = """
            WITH input_roles(code) AS (
              %s
            ),
            resolved_roles AS (
              SELECT r.id, r.code
              FROM builders.roles r
              JOIN input_roles i ON i.code = r.code
            ),
            missing_roles AS (
              SELECT i.code
              FROM input_roles i
              LEFT JOIN resolved_roles r ON r.code = i.code
              WHERE r.id IS NULL
            ),
            deleted AS (
              DELETE FROM builders.user_roles ur
              WHERE ur.user_id = :userId
                AND NOT EXISTS (SELECT 1 FROM missing_roles)
                AND NOT EXISTS (
                  SELECT 1
                  FROM resolved_roles rr
                  WHERE rr.id = ur.role_id
                )
              RETURNING ur.role_id
            ),
            inserted AS (
              INSERT INTO builders.user_roles (user_id, role_id)
              SELECT :userId, rr.id
              FROM resolved_roles rr
              WHERE NOT EXISTS (SELECT 1 FROM missing_roles)
              ON CONFLICT (user_id, role_id) DO NOTHING
              RETURNING role_id
            )
            SELECT
              COALESCE((SELECT array_agg(code) FROM missing_roles), ARRAY[]::varchar[]) AS missing_roles,
              (SELECT count(*) FROM inserted) AS inserted_count,
              (SELECT count(*) FROM deleted) AS deleted_count
            """.formatted(inputRolesSql);

        var query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        normalizedRoleCodes.forEach(code -> query.setParameter(parameterName(code), code));

        Object[] row = (Object[]) query.getSingleResult();
        Set<String> missingRoles = toStringSet(row[0]);
        long insertedCount = toLong(row[1]);
        long deletedCount = toLong(row[2]);
        return new UserRoleSyncResult(missingRoles, insertedCount, deletedCount);
    }

    private Set<String> normalizeRoleCodes(Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Set.of();
        }
        return roleCodes.stream()
            .map(this::normalizeCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private String parameterName(String roleCode) {
        return "roleCode_" + roleCode.replaceAll("[^A-Z0-9_]", "_");
    }

    private Set<String> toStringSet(Object value) {
        if (value == null) {
            return Set.of();
        }
        if (value instanceof Array sqlArray) {
            try {
                Object arrayValue = sqlArray.getArray();
                if (arrayValue instanceof Object[] objects) {
                    return toLinkedHashSet(objects);
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to read missing role codes", ex);
            }
        }
        if (value instanceof Object[] objects) {
            return toLinkedHashSet(objects);
        }
        if (value instanceof Set<?> set) {
            return set.stream()
                .map(Object::toString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return Set.of(value.toString());
    }

    private Set<String> toLinkedHashSet(Object[] values) {
        return java.util.Arrays.stream(values)
            .filter(java.util.Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }
}
