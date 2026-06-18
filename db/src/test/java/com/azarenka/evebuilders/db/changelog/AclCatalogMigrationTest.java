package com.azarenka.evebuilders.db.changelog;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AclCatalogMigrationTest {

    @Test
    void masterIncludesCanonicalAclCatalogAfterLegacyAclMigration() throws IOException {
        String master = readResource("db/changelog/db.changelog-master.yaml");

        int legacyIndex = master.indexOf("changeset-2026-06-14-acl.yaml");
        int canonicalIndex = master.indexOf("changeset-2026-06-17-acl-catalog.yaml");
        int cleanupIndex = master.indexOf("changeset-2026-06-17-acl-cleanup.yaml");

        assertTrue(legacyIndex >= 0, "legacy ACL migration must remain included");
        assertTrue(canonicalIndex > legacyIndex, "canonical ACL migration must be included after the legacy ACL migration");
        assertTrue(cleanupIndex > canonicalIndex, "cleanup ACL migration must be included after the canonical ACL migration");
    }

    @Test
    void canonicalAclCatalogSeedsRequestedRolesPermissionsAndMappings() throws IOException {
        String migration = readResource("db/changelog/changeset-2026-06-17-acl-catalog.yaml");

        assertTrue(migration.contains("('SUPER_ADMIN', 'Super Administrator', 'Full access bypass role', true)"));
        assertTrue(migration.contains("('CEO', 'CEO', 'Executive role', true)"));
        assertTrue(migration.contains("('MANAGER', 'Manager', 'Management role', true)"));
        assertTrue(migration.contains("('MINER', 'Miner', 'Mining and extraction role', true)"));
        assertTrue(migration.contains("('BUILDER', 'Builder', 'Construction and production role', true)"));

        assertTrue(migration.contains("('DASHBOARD_VIEW', 'View dashboard', 'Access dashboard overview', 'DASHBOARD')"));
        assertTrue(migration.contains("('CONTRACTS_VIEW', 'View contracts', 'Read contract data', 'CONTRACTS')"));
        assertTrue(migration.contains("('CONTRACTS_CREATE', 'Create contracts', 'Create contracts', 'CONTRACTS')"));
        assertTrue(migration.contains("('CONTRACTS_EDIT', 'Edit contracts', 'Edit contracts', 'CONTRACTS')"));
        assertTrue(migration.contains("('CONTRACTS_ACCEPT', 'Accept contracts', 'Accept incoming contracts', 'CONTRACTS')"));
        assertTrue(migration.contains("('CONTRACTS_CANCEL', 'Cancel contracts', 'Cancel owned contracts', 'CONTRACTS')"));
        assertTrue(migration.contains("('CONTRACTS_DISCARD', 'Discard contracts', 'Discard draft contracts', 'CONTRACTS')"));
        assertTrue(migration.contains("('CORPORATION_VIEW', 'View corporation', 'Read corporation data', 'CORPORATION')"));
        assertTrue(migration.contains("('CORPORATION_CONTRACT_VIEW', 'View corporation contracts', 'Read corporation contract data', 'CORPORATION_CONTRACTS')"));
        assertTrue(migration.contains("('CORPORATION_CONTRACT_EDIT', 'Edit corporation contracts', 'Edit corporation contract data', 'CORPORATION_CONTRACTS')"));

        assertTrue(migration.contains("('CEO', 'CORPORATION_VIEW')"));
        assertTrue(migration.contains("('CEO', 'CORPORATION_CONTRACT_VIEW')"));
        assertTrue(migration.contains("('CEO', 'CORPORATION_CONTRACT_EDIT')"));
        assertTrue(migration.contains("('MANAGER', 'CONTRACTS_VIEW')"));
        assertTrue(migration.contains("('MANAGER', 'CONTRACTS_CREATE')"));
        assertTrue(migration.contains("('MANAGER', 'CONTRACTS_EDIT')"));
        assertTrue(migration.contains("('MANAGER', 'CONTRACTS_CANCEL')"));
        assertTrue(migration.contains("('MINER', 'DASHBOARD_VIEW')"));
        assertTrue(migration.contains("('BUILDER', 'DASHBOARD_VIEW')"));
        assertTrue(migration.contains("('BUILDER', 'CONTRACTS_ACCEPT')"));
        assertTrue(migration.contains("('BUILDER', 'CONTRACTS_DISCARD')"));

        String rolePermSection = migration.substring(migration.indexOf("WITH role_perm"));
        assertFalse(rolePermSection.contains("('SUPER_ADMIN',"), "SUPER_ADMIN must not receive database permission rows");
        assertFalse(rolePermSection.contains("ROLE_ADMIN"));
        assertFalse(rolePermSection.contains("ROLE_VIEWER"));
        assertFalse(rolePermSection.contains("ROLE_COORDINATOR"));
        assertFalse(rolePermSection.contains("ROLE_WORKER"));
    }

    @Test
    void cleanupMigrationRemovesObsoletePermissionsSafely() throws IOException {
        String migration = readResource("db/changelog/changeset-2026-06-17-acl-cleanup.yaml");

        assertTrue(migration.contains("DELETE FROM builders.role_permissions rp"));
        assertTrue(migration.contains("DELETE FROM builders.user_permissions up"));
        assertTrue(migration.contains("DELETE FROM builders.permissions"));
        assertTrue(migration.contains("'USERS_VIEW'"));
        assertTrue(migration.contains("'ROLES_VIEW'"));
        assertTrue(migration.contains("'BUILDS_VIEW'"));
        assertTrue(migration.contains("'WALLET_VIEW'"));
        assertTrue(migration.contains("'CORPORATION_EDIT'"));
        assertTrue(migration.contains("'CONTRACTS_DELETE'"));
        assertTrue(migration.contains("'MINING_OPS_VIEW'"));
        assertTrue(migration.contains("'MINING_OPS_CREATE'"));
        assertTrue(migration.contains("'MINING_OPS_EDIT'"));
        assertTrue(migration.contains("'MINING_OPS_DELETE'"));
    }

    private String readResource(String path) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
