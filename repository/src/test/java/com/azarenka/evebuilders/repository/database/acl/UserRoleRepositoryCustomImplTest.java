package com.azarenka.evebuilders.repository.database.acl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Array;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleRepositoryCustomImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Mock
    private Array sqlArray;

    private UserRoleRepositoryCustomImpl repository;

    @BeforeEach
    void setUp() {
        repository = new UserRoleRepositoryCustomImpl();
        repository.entityManager = entityManager;
    }

    @Test
    void syncUserRolesBuildsParameterizedSqlAndNormalizesInput() throws Exception {
        LinkedHashSet<String> input = new LinkedHashSet<>();
        input.add("  ceo  ");
        input.add("builder");
        input.add(" ");
        input.add(null);
        input.add("builder");

        when(entityManager.createNativeQuery(org.mockito.ArgumentMatchers.anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Object[]{sqlArray, 1L, 2L});
        when(sqlArray.getArray()).thenReturn(new String[]{"UNKNOWN"});

        UserRoleSyncResult result = repository.syncUserRoles("user-1", input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("WITH input_roles(code) AS"));
        assertTrue(sql.contains("SELECT CAST(:roleCode_CEO AS varchar) AS code UNION ALL SELECT CAST(:roleCode_BUILDER AS varchar) AS code"));
        assertTrue(sql.contains("NOT EXISTS (SELECT 1 FROM missing_roles)"));
        assertTrue(sql.contains("ON CONFLICT (user_id, role_id) DO NOTHING"));
        verify(query).setParameter("userId", "user-1");
        verify(query).setParameter("roleCode_CEO", "CEO");
        verify(query).setParameter("roleCode_BUILDER", "BUILDER");
        verify(entityManager).flush();

        assertEquals(Set.of("UNKNOWN"), result.missingRoles());
        assertEquals(1L, result.insertedCount());
        assertEquals(2L, result.deletedCount());
    }

    @Test
    void syncUserRolesUsesEmptyInputSourceWhenNoRolesProvided() {
        when(entityManager.createNativeQuery(org.mockito.ArgumentMatchers.anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Object[]{new String[0], 0L, 3L});

        UserRoleSyncResult result = repository.syncUserRoles("user-2", Set.of());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("SELECT CAST(NULL AS varchar) WHERE false"));
        verify(query).setParameter("userId", "user-2");
        verify(entityManager).flush();

        assertEquals(Set.of(), result.missingRoles());
        assertEquals(0L, result.insertedCount());
        assertEquals(3L, result.deletedCount());
    }
}
