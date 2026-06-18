# Migration Plan: Roles -> Permissions

## Goal

Migrate the access-control model from role-only checks to:

- `User -> Roles -> Permissions`
- `User -> Direct Permissions` as an optional override layer
- `SUPER_ADMIN` bypasses all permission checks

This document is a planning artifact only. No runtime behavior is changed here.

## Current State

The project is still role-centric:

- `User` stores `roles` and also has a `permissions` field, but permissions are not wired into access control.
- JWT and `me` responses expose roles, not permissions.
- UI is a JWT-only client. Vaadin-style route security is not part of the target architecture.
- `SUPER_ADMIN` is treated as a special role in scattered checks, not as a centralized bypass policy.

## Files That Depend On Old Roles

### Domain

- `domain/src/main/java/com/azarenka/evebuilders/domain/db/Role.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/Permission.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/User.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/dto/UserDto.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/dto/EveUserPrincipal.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/auth/auth/ui/MeResponse.java`

### Security / Auth

- `ui/src/main/java/com/azarenka/evebuilders/config/SecurityConfig.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/auth/eve/ui/DbUserDetailsService.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/auth/eve/ui/JwtService.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/auth/eve/ui/JwtAuthFilter.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/auth/eve/SecurityUtils.java`
- `ui/src/main/java/com/azarenka/evebuilders/rest/api/casino/ui/AuthController.java`
- `controller/src/main/java/com/azarenka/eve/evebuilders/controller/auth/AuthController.java`

### Backend Services

- `service/src/main/java/com/azarenka/evebuilders/service/impl/auth/eve/EveOAuth2UserService.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/UserService.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/staff/StaffService.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/MailService.java`

### UI Access Control

- `ui/src/main/java/com/azarenka/evebuilders/common/util/BuilderPermission.java`

### UI Screens With Role Checks

No Vaadin pages are included in the target access-control model.
Authorization is expected to be enforced through JWT + backend/API checks only.

### Database / Migrations

- `db/src/main/resources/db/changelog/add_roles.yaml`
- `db/src/main/resources/db/changelog/changeset-1.5.yaml`
- `db/src/main/resources/db/changelog/changeset-1.7.yaml`
- `db/src/main/resources/db/changelog/changeset-1.yaml`
- `db/src/main/resources/db/changelog/db.changelog-master.yaml`

## What Needs To Change

### 1. Data Model

- Define how permissions are represented in persistence.
- Decide whether role-permission and user-permission are separate relations.
- Keep `SUPER_ADMIN` as a bypass condition, not a normal permission.

### 2. Security Representation

- Replace role-only authorities with permission-aware authorities.
- Decide whether JWT should carry roles, permissions, or both.
- Ensure `me` returns enough data for the frontend to render access state.

### 3. Backend Authorization

- Replace direct role checks in services with permission checks where appropriate.
- Keep business rules that are still role-based only if they are truly domain roles.
- Introduce a single policy path for `SUPER_ADMIN` bypass.

### 4. UI Authorization

- Replace any remaining role-based visibility in the JWT frontend with permission-based visibility.
- Remove any UI logic that depends on Vaadin route annotations or Vaadin component security.
- Update any role-management UI that remains in the JWT frontend.

### 5. Database Migration

- Add tables and relations for permissions if missing.
- Add mapping tables for role-permission and user-permission.
- Backfill existing users so current access remains stable.

### 6. Tests

- Add coverage for permission resolution.
- Add coverage for `SUPER_ADMIN` bypass.
- Add coverage for JWT `me` flow.
- Add coverage for permission rendering in the JWT frontend.

## Suggested Migration Order

1. Freeze current access behavior with tests.
2. Introduce persistence for permissions and relations.
3. Build a permission resolution service.
4. Update JWT and `me` contract to carry the new access model.
5. Migrate backend checks from roles to permissions.
6. Backfill existing users and validate `SUPER_ADMIN` bypass.
7. Remove obsolete role-only checks where no longer needed.

## Target DB Model

### Tables

- `roles`
- `permissions`
- `user_roles`
- `role_permissions`
- `user_permissions`

### Existing Data Sources

- `builders.user_info.roles` remains the legacy source for backfilling `user_roles` during the migration.
- Existing users are not dropped or recreated.
- Legacy role values like `ROLE_ADMIN` and `ROLE_BUILDER` are translated into normalized role codes.

## Suggested Seed Data

### Roles

- `SUPER_ADMIN`
- `CEO`
- `MANAGER`
- `MINER`
- `BUILDER`

### Permissions

- `DASHBOARD_VIEW`
- `CONTRACTS_VIEW`
- `CONTRACTS_CREATE`
- `CONTRACTS_EDIT`
- `CONTRACTS_ACCEPT`
- `CONTRACTS_CANCEL`
- `CONTRACTS_DISCARD`
- `CORPORATION_VIEW`
- `CORPORATION_CONTRACT_VIEW`
- `CORPORATION_CONTRACT_EDIT`

## Role To Permission Mapping

### Bypass

- `SUPER_ADMIN` bypasses permission checks and does not need explicit permission rows.

### Canonical Roles

- `CEO`: `CORPORATION_VIEW`, `CORPORATION_CONTRACT_VIEW`, `CORPORATION_CONTRACT_EDIT`
- `MANAGER`: `CONTRACTS_VIEW`, `CONTRACTS_CREATE`, `CONTRACTS_EDIT`, `CONTRACTS_CANCEL`
- `MINER`: `DASHBOARD_VIEW`
- `BUILDER`: `DASHBOARD_VIEW`, `CONTRACTS_ACCEPT`, `CONTRACTS_DISCARD`

### Compatibility Roles

- Older compatibility roles and permissions can remain in the database for existing users, but they are not part of the new default seed catalog.

## Migration Notes

- Keep `user_info.roles` unchanged for now.
- Populate `user_roles` from `user_info.roles`.
- Add `permissions` catalog rows first, then wire `role_permissions`.
- `user_permissions` stays optional and empty unless direct grants are added later.

## Notes

- Do not remove old role logic until the permission path is fully validated.
- Keep the migration incremental to avoid locking out existing users.
- The `controller` module appears to be a legacy duplicate and should be reviewed separately before any cleanup.
- Vaadin-specific routing, menu visibility, and `@RolesAllowed` page guards are out of scope for the target architecture if they are not used by the JWT UI anymore.
