package com.azarenka.evebuilders.db.changelog;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AclCatalogMigrationTest {

    @Test
    void masterIncludesOnlyBootstrapSchemaAndSeedChangelogs() throws IOException {
        String master = readResource("db/changelog/db.changelog-master.yaml");

        assertTrue(master.contains("changeset-0-baseline-builders-schema.yaml"));
        assertTrue(master.contains("changeset-1.yaml"));
        assertFalse(master.contains("add_roles.yaml"));
        assertFalse(master.contains("changeset-1.5.yaml"));
        assertFalse(master.contains("changeset-1.7.yaml"));
        assertFalse(master.contains("changeset-2026-06-14-acl.yaml"));
        assertFalse(master.contains("changeset-2026-06-17-acl-catalog.yaml"));
        assertFalse(master.contains("changeset-2026-06-17-acl-cleanup.yaml"));
        assertFalse(master.contains("changeset-2026-06-18-acl-admin.yaml"));
        assertFalse(master.contains("changeset-2026-07-04-fit-text-fit.yaml"));
    }

    @Test
    void seedChangelogContainsCurrentRolesPermissionsAndMappings() throws IOException {
        String seed = readResource("db/changelog/changeset-1.yaml");

        assertTrue(seed.contains("('ADMIN', 'Administrator', 'Administrative role', true)"));
        assertTrue(seed.contains("('BUILDER', 'Builder', 'Construction and production role', true)"));
        assertTrue(seed.contains("('CEO', 'CEO', 'Executive role', true)"));
        assertTrue(seed.contains("('COORDINATOR', 'Coordinator', 'Coordination role', false)"));
        assertTrue(seed.contains("('MANAGER', 'Manager', 'Management role', true)"));
        assertTrue(seed.contains("('MINER', 'Miner', 'Mining and extraction role', true)"));
        assertTrue(seed.contains("('SUPER_ADMIN', 'Super Administrator', 'Full access bypass role', true)"));
        assertTrue(seed.contains("('VIEWER', 'Viewer', 'Read-only role', false)"));

        assertTrue(seed.contains("('ADMIN_VIEW', 'View admin shell', 'Access the admin shell and admin navigation', 'ADMIN')"));
        assertTrue(seed.contains("('ROLES_PERMISSIONS_EDIT', 'Edit role permissions', 'Assign permissions to roles', 'ROLES')"));
        assertTrue(seed.contains("('CORPORATION_CONTRACT_VIEW', 'View corporation contracts', 'Read corporation contract data', 'CORPORATION_CONTRACTS')"));
        assertTrue(seed.contains("('CORPORATION_CONTRACT_EDIT', 'Edit corporation contracts', 'Edit corporation contract data', 'CORPORATION_CONTRACTS')"));
        assertFalse(seed.contains("USERS_CREATE"));
        assertFalse(seed.contains("USERS_DELETE"));
        assertFalse(seed.contains("CORPORATION_EDIT"));
        assertFalse(seed.contains("CONTRACTS_DELETE"));
        assertFalse(seed.contains("MINING_OPS_"));
        assertFalse(seed.contains("BUILDS_"));
        assertFalse(seed.contains("WALLET_"));
        assertFalse(seed.contains("MEMBERS_"));

        assertTrue(seed.contains("('ADMIN', 'ADMIN_VIEW')"));
        assertTrue(seed.contains("('ADMIN', 'ROLES_PERMISSIONS_EDIT')"));
        assertTrue(seed.contains("('BUILDER', 'CONTRACTS_ACCEPT')"));
        assertTrue(seed.contains("('CEO', 'ROLES_PERMISSIONS_EDIT')"));
        assertTrue(seed.contains("('COORDINATOR', 'CORPORATION_CONTRACT_EDIT')"));
        assertTrue(seed.contains("('MANAGER', 'CONTRACTS_CANCEL')"));
        assertTrue(seed.contains("('MINER', 'DASHBOARD_VIEW')"));
        assertTrue(seed.contains("('VIEWER', 'CORPORATION_CONTRACT_VIEW')"));
        assertTrue(seed.contains("('SUPER_ADMIN', 'CONTRACTS_VIEW')"));
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
