# Corporation Management Stage 1: Implementation Slices

## Slice 1: Codebase discovery
### Goal
- Capture codebase-specific facts about EVE corporation/contract JSON models, persistence conventions, migration style, UI patterns, and security/roles.

### Existing files/classes to inspect
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/Corporation.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/dto/Contract.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/intergarion/EveCorporationIntegrationService.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/contract/ContractService.java`
- `db/src/main/resources/db/changelog/db.changelog-master.yaml`
- `db/src/main/resources/db/changelog/changeset-1.yaml`
- `db/src/main/resources/db/changelog/changeset-1.5.yaml`
- `db/src/main/resources/db/changelog/changeset-1.7.yaml`
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/Order.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/RequestOrder.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/Fit.java`
- `repository/src/main/java/com/azarenka/evebuilders/repository/database/IOrderRepository.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/order/DistributedOrderService.java`
- `ui/src/main/java/com/azarenka/evebuilders/main/orders/myorders/CorporationConstructionsView.java`
- `ui/src/main/java/com/azarenka/evebuilders/main/request/admin/RequestsView.java`
- `ui/src/main/java/com/azarenka/evebuilders/config/SecurityConfig.java`

### Files/classes to create
- `docs/corporation-management-stage-1-research.md`

### Files/classes to update
- none (code)

### Database changes
- none

### Acceptance criteria
- Research document contains exact current class/package names and behavior notes.
- Contract validation hardcoded corporation property is documented as out of scope.

### Notes/risks
- Avoid assumptions; unresolved points must be listed as open questions.

## Slice 2: Corporation data model and database table
### Goal
- Define and document the persistence model for user-managed corporations and prepare Liquibase migration plan.

### Existing files/classes to inspect
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/Corporation.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/intergarion/EveCorporationIntegrationService.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/Fit.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/Order.java`
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/User.java`
- `db/src/main/resources/db/changelog/db.changelog-master.yaml`

### Files/classes to create
- `domain/src/main/java/com/azarenka/evebuilders/domain/db/ManagedCorporation.java` (proposed)
- `db/src/main/resources/db/changelog/changeset-1.8-corporation-management.yaml` (proposed)

### Files/classes to update
- `db/src/main/resources/db/changelog/db.changelog-master.yaml` (add include)

### Database changes
- Create table `builders.managed_corporation` with:
  - `id VARCHAR(36)` PK
  - `eve_corporation_id BIGINT` NOT NULL
  - `corporation_name VARCHAR(255)` NOT NULL
  - `corporation_ticker VARCHAR(20)` NULL
  - `owner_username VARCHAR(255)` NOT NULL
  - `created_by VARCHAR(255)` NOT NULL
  - `created_date DATE` NOT NULL
  - `updated_by VARCHAR(255)` NULL
  - `updated_date DATE` NULL
- Add unique constraint on (`owner_username`, `eve_corporation_id`).
- Add indexes on `owner_username` and `eve_corporation_id`.

### Acceptance criteria
- Migration naming and placement follow current Liquibase convention.
- Field types and audit/ownership fields are justified from existing entity patterns.
- Document explicitly states JSON `domain.db.Corporation` is integration DTO, not persistence entity.

### Notes/risks
- Ownership is fixed as `owner_username` (per research decision).

## Slice 3: Corporation service/repository layer
### Goal
- Plan service/repository for add/list operations with user-scoped and admin-scoped retrieval.

### Existing files/classes to inspect
- `service/src/main/java/com/azarenka/evebuilders/service/api/ICorporationService.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/corporation/CorporationService.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/auth/eve/SecurityUtils.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/UserService.java`
- `repository/src/main/java/com/azarenka/evebuilders/repository/database/IUserRepository.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/order/DistributedOrderService.java`

### Files/classes to create
- `repository/src/main/java/com/azarenka/evebuilders/repository/database/IManagedCorporationRepository.java` (proposed)
- service-layer request/response DTOs if needed:
  - `domain/src/main/java/com/azarenka/evebuilders/domain/dto/ManagedCorporationDto.java` (optional, proposed)

### Files/classes to update
- `service/src/main/java/com/azarenka/evebuilders/service/api/ICorporationService.java`
- `service/src/main/java/com/azarenka/evebuilders/service/impl/corporation/CorporationService.java`

### Database changes
- none beyond Slice 2 migration.

### Acceptance criteria
- Planned methods include:
  - add corporation for current user;
  - list corporations for current user;
  - list all corporations for admin.
- Duplicate handling is defined via (`owner_username`, `eve_corporation_id`) uniqueness.
- No coupling to `ContractService` contract validation flow.

### Notes/risks
- If username can change, ownership-by-username has migration implications.

## Slice 4: User corporation management UI
### Goal
- Add/plan user-facing page for adding and viewing only own corporations.

### Existing files/classes to inspect
- `ui/src/main/java/com/azarenka/evebuilders/main/orders/myorders/CorporationConstructionsView.java`
- `ui/src/main/java/com/azarenka/evebuilders/main/orders/myorders/CorporationConstructionController.java`
- `ui/src/main/java/com/azarenka/evebuilders/main/constructions/api/ICorporationConstructionController.java`
- `ui/src/main/java/com/azarenka/evebuilders/main/menu/MenuOrdersPage.java`
- `ui/src/main/java/com/azarenka/evebuilders/component/SearchComponent.java`

### Files/classes to create
- `ui/src/main/java/com/azarenka/evebuilders/main/orders/corporation/ManagedCorporationsView.java` (proposed)
- `ui/src/main/java/com/azarenka/evebuilders/main/orders/corporation/ManagedCorporationsController.java` (proposed)
- `ui/src/main/java/com/azarenka/evebuilders/main/orders/corporation/api/IManagedCorporationsController.java` (proposed)

### Files/classes to update
- `ui/src/main/java/com/azarenka/evebuilders/main/menu/MenuOrdersPage.java` (add tab to new user corporation page)

### Database changes
- none

### Acceptance criteria
- User can add corporation by Stage 1 field set (at minimum EVE corporation ID; optionally name/ticker auto-resolved).
- Grid/list shows only records owned by current user.
- Page follows existing `View` + Vaadin Grid + toolbar/search conventions.
- Corporation input is user-driven for `corporation_name` and `corporation_ticker`, while `eve_corporation_id` must be validated against ESI (existence check via `IEveCorporationService`).

### Notes/risks
- `corporation_name`/`corporation_ticker` are manual user input; API call is for corporation existence validation only.

## Slice 5: Admin corporation overview UI
### Goal
- Add/plan admin page for read-only overview of all managed corporations.

### Existing files/classes to inspect
- `ui/src/main/java/com/azarenka/evebuilders/main/managment/orders/OrdersManagmentView.java`
- `ui/src/main/java/com/azarenka/evebuilders/main/request/admin/RequestsView.java`
- `ui/src/main/java/com/azarenka/evebuilders/main/menu/MenuManagerPage.java`
- `ui/src/main/java/com/azarenka/evebuilders/config/SecurityConfig.java`

### Files/classes to create
- `ui/src/main/java/com/azarenka/evebuilders/main/managment/corporation/CorporationRegistryView.java` (proposed)
- `ui/src/main/java/com/azarenka/evebuilders/main/managment/corporation/CorporationRegistryController.java` (proposed)
- `ui/src/main/java/com/azarenka/evebuilders/main/managment/api/ICorporationRegistryController.java` (proposed)

### Files/classes to update
- `ui/src/main/java/com/azarenka/evebuilders/main/menu/MenuManagerPage.java` (add admin tab entry)

### Database changes
- none

### Acceptance criteria
- Route is restricted to `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`.
- Admin sees all corporations with columns aligned to actual persisted fields.
- Default behavior is view-only (no edit/delete in stage 1).
- Page is implemented in `manager` area (`MenuManagerPage`) as the canonical admin layout.

### Notes/risks
- If edit/delete UX is desired, defer to stage 2 unless explicitly required.

## Slice 6: Stage 1 cleanup and documentation
### Goal
- Finalize both docs with exact names/paths decided during implementation and unresolved decisions.

### Existing files/classes to inspect
- all files added/updated in Slices 1-5.

### Files/classes to create
- none

### Files/classes to update
- `docs/corporation-management-stage-1-research.md`
- `docs/corporation-management-stage-1-slices.md`

### Database changes
- none

### Acceptance criteria
- Docs contain final exact package/class/migration/table names used in Stage 1.
- Docs explicitly mark:
  - contract validation replacement/refactor is out of scope for Stage 1;
  - tests are out of scope for Stage 1.
- Stage 2 follow-up list is present.

### Notes/risks
- Keep documentation synchronized with actual code choices to avoid drift.
