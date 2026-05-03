# Corporation Management Stage 1: Research

## Scope of this document
- Stage 1 only: research, DB table design/migration plan, and UI page plans.
- Out of scope in Stage 1:
  - any refactoring of contract validation flow;
  - removal/replacement of `app.eve.corporation.id`;
  - behavior changes in existing contract validation.
- Additional execution note:
  - unit tests were requested and implemented after stage planning as a follow-up execution step.

## Current relevant codebase structure
- Multi-module Maven project:
  - `domain`: entities/DTOs/enums.
  - `repository`: Spring Data repositories.
  - `service`: business services and EVE integrations.
  - `ui`: Vaadin views/controllers/security/app config.
  - `db`: Liquibase changelogs.

## Existing EVE corporation / contract / JSON-related classes
- Corporation API JSON model:
  - `domain/src/main/java/com/azarenka/evebuilders/domain/db/Corporation.java`
  - Used by `service/src/main/java/com/azarenka/evebuilders/service/impl/intergarion/EveCorporationIntegrationService.java`.
  - Exposed via `service/src/main/java/com/azarenka/evebuilders/service/api/IEveCorporationService.java`.
- Contract API JSON models:
  - `domain/src/main/java/com/azarenka/evebuilders/domain/dto/Contract.java`
  - `domain/src/main/java/com/azarenka/evebuilders/domain/dto/ContractItem.java`
  - Used by `service/src/main/java/com/azarenka/evebuilders/service/impl/intergarion/EveContractsIntegrationService.java`.

## Existing object(s) that may represent corporation data
- `domain.db.Corporation` already represents ESI corporation payload (JSON fields like `alliance_id`, `tax_rate`, `war_eligible`).
- There is no JPA entity/repository for user-managed corporations yet.
- `service/api/ICorporationService.java` and `service/impl/corporation/CorporationService.java` exist but are empty placeholders.

## Can existing JSON DTO be reused as persistence/domain object?
- Direct reuse of `domain.db.Corporation` as persistence entity is **not recommended**.

Why:
- It is integration-shaped DTO (ESI payload), not app persistence model.
- No `@Entity`, no app-level primary key, no owner relation, no audit fields.
- Field set is ESI-rich but does not match Stage 1 UI needs (user-owned list/add).
- Mixing transport and persistence risks tight coupling to ESI schema changes.

Clean split proposal:
- Keep `domain.db.Corporation` for ESI integration only.
- Add separate persistence entity for user-managed corporations:
  - proposed class: `domain/src/main/java/com/azarenka/evebuilders/domain/db/ManagedCorporation.java`
  - dedicated repository/service and UI DTOs if needed.

## Current contract validation dependency (must remain unchanged in Stage 1)
- `service/impl/contract/ContractService.java` contains:
  - `@Value("${app.eve.corporation.id}") private Long corporationId;`
  - TODO comment explicitly says hardcoded property should later be moved to DB.
- Property exists in:
  - `ui/src/main/resources/application-local.yml`
  - `ui/src/main/resources/application-prod.yml`
  - path: `app.eve.corporation.id`.
- Contract integration currently uses that property for corporation contracts/items requests.

Stage 1 decision:
- Do not modify `ContractService`, `IContractService`, contract jobs, or existing property behavior.

## Existing database migration style and placement
- Liquibase master:
  - `db/src/main/resources/db/changelog/db.changelog-master.yaml`
  - includes: `changeset-1.yaml`, `add_roles.yaml`, `changeset-1.5.yaml`, `changeset-1.7.yaml`.
- Existing style:
  - YAML-based changelogs.
  - mix of Liquibase structured changes and raw SQL blocks.
  - schema is `builders`.
- Stage 1 migration placement:
  - new file under `db/src/main/resources/db/changelog/`.
  - proposed name: `changeset-1.8-corporation-management.yaml`.
  - add `include` entry to `db.changelog-master.yaml`.

## Existing persistence patterns

### Entity style
- JPA entities in `domain.db` with `@Entity`, `@Table(name=..., schema="builders")`.
- IDs are mostly string UUIDs in app-managed entities (`Order`, `RequestOrder`, `DistributedOrder`, `Fit`).
- Audit-like columns are explicit fields, not shared base class:
  - common names: `created_by`, `created_date`, `updated_by`, `updated_date`.
- Ownership pattern is mostly username string field (for example `Fit.createdBy`, `DistributedOrder.userName`), not strict FK to `user_info`.

### Repository style
- Spring Data `JpaRepository<Entity, String>` interfaces in `repository.database`.
- Custom query methods and occasional `@Query` / `@Modifying` where needed.
- Filtering often via `Specification` for list views.

### Service style
- Interface in `service.api`, implementation in `service.impl...`.
- `@Service` classes, field `@Autowired`.
- Current user is obtained through `SecurityUtils.getUserName()`.
- IDs commonly generated via `UUID.randomUUID().toString()`.

### Ownership/user relation style
- User table entity: `domain.db.User` (`builders.user_info`) with string `uid`, username, roles.
- Business records frequently store `createdBy`/`userName` as string usernames.
- Existing user-scoped retrieval is done by filtering with current username (for example distributed orders and fits).

## Existing UI patterns for normal user pages
- Vaadin route views with menu layout parent and role annotations.
- Example user-scoped screen:
  - `ui/.../main/orders/myorders/CorporationConstructionsView.java` (`@Route("corporation", layout=MenuOrdersPage.class)`).
  - Uses controller interface + controller implementation pattern.
  - Uses `Grid`, `ListDataProvider`, `SearchComponent`, toolbar buttons, optional filter popup.
- Navigation for user order area:
  - `ui/.../main/menu/MenuOrdersPage.java` with tabs and `@RoutePrefix("orders")`.

## Existing UI patterns for admin pages
- Admin screens usually under manager/request/admin packages.
- Examples:
  - `ui/.../main/request/admin/RequestsView.java` (`@RolesAllowed({"ROLE_ADMIN","ROLE_SUPER_ADMIN"})`).
  - `ui/.../main/managment/orders/OrdersManagmentView.java` under `MenuManagerPage`.
- Typical UI: grid + action toolbar + search + role-restricted route.

## Existing security / role / access-control model
- Security config: `ui/.../config/SecurityConfig.java` extends `VaadinWebSecurity`.
- Route access mainly by `@RolesAllowed`, plus `@PermitAll` / `@AnonymousAllowed` for selected routes.
- Roles enum: `domain.db.Role` with values such as `ROLE_BUILDER`, `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`, etc.
- Role checks also used in UI utility: `ui/.../common/util/BuilderPermission.java`.

## What should be added in Stage 1
- Persistence layer for managed corporations (separate from ESI DTO):
  - `ManagedCorporation` entity (new).
  - `IManagedCorporationRepository` (new).
  - fill `ICorporationService` and `CorporationService` with add/list methods.
- DB migration:
  - create table in `builders` schema with owner + corporation fields + audit fields.
  - include migration in `db.changelog-master.yaml`.
- User UI page (normal user):
  - add route under orders area for adding corporation and listing own corporations.
  - use existing view/controller/grid/search conventions.
- Admin UI page:
  - add route under manager area for read-only list of all corporations.
  - use existing manager menu/view conventions.

## Proposed Stage 1 table/entity design (based on discovered patterns)
- Proposed table: `builders.managed_corporation`.
- Proposed entity: `ManagedCorporation`.
- Proposed fields:
  - `id` `VARCHAR(36)` primary key (UUID string, consistent with app entities).
  - `eve_corporation_id` `BIGINT` not null (EVE corporation identifier; use `Long` in Java).
  - `corporation_name` `VARCHAR(255)` not null (display/listing needs).
  - `corporation_ticker` `VARCHAR(20)` nullable (optional from ESI; useful in UI).
  - `owner_username` `VARCHAR(255)` not null (ownership pattern consistent with current app).
  - `created_by` `VARCHAR(255)` not null.
  - `created_date` `DATE` not null.
  - `updated_by` `VARCHAR(255)` nullable.
  - `updated_date` `DATE` nullable.
- Proposed constraints:
  - unique (`owner_username`, `eve_corporation_id`) to prevent per-user duplicates.
  - index on `owner_username` for user list query.
  - index on `eve_corporation_id` for admin/search/duplicate checks.

Rationale:
- Uses existing ID and audit conventions from `Order`, `RequestOrder`, `Fit`.
- Keeps ownership user-scoped without introducing new FK pattern that is not common in current business entities.
- Avoids storing the full ESI payload in this first stage.

## What explicitly should NOT be changed in Stage 1
- `service/impl/contract/ContractService.java` behavior and flow.
- `@Value("${app.eve.corporation.id}")` usage and property definitions.
- existing contract validation/report/job logic.
- any test implementation.

## Decisions accepted
- Ownership field is `owner_username` (not `owner_uid` FK) for consistency with existing app ownership style.
- Admin corporation overview is placed in manager area (`MenuManagerPage` layout).
- `corporation_name` and `corporation_ticker` are user-input fields.
- `eve_corporation_id` must be validated through ESI API (`IEveCorporationService`) to confirm corporation existence.

## Risks
- Risk: existing DB naming has historical inconsistencies; new migration must keep strict, explicit naming.
- Risk: some views use broad `@PermitAll`; route-level annotations must be checked carefully when adding pages.
- Risk: Liquibase + Hibernate `ddl-auto:update` are both enabled in configs; migration-first discipline should be followed.
