# Order Properties Architecture (Decisions Applied)

## Scope
This document describes approved codebase and domain-model changes to support configurable default order parameters from `PropertiesView`.

No implementation is included.

## Decisions From Discovery
1. Preset defaults scope: **per user**.
2. `rightsholder` string: **must be part of defaults**.
3. Audit trail: **required** in a separate history table.

## Current State
- `ParametersOrderView` sets default values in UI code (`applyDefaultValues`).
- Corporation default is hardcoded by name (`Scan Stakan`) and resolved from managed corporations list.
- `PropertiesView` currently manages only destination dictionary.
- There is no domain entity dedicated to user-scoped preset order defaults.

## Target State
- Preset defaults are persisted in DB and managed by admins on `PropertiesView`.
- Defaults are resolved **for the current user**.
- `ParametersOrderView` loads defaults from service/controller, not hardcoded constants.
- Defaults remain resilient if referenced corporation/user becomes unavailable.
- Each change to defaults is written to audit history.

## Domain Model Changes
### New entity: `OrderPresetDefaults`
Table (proposed): `builders.order_preset_defaults`

Fields:
- `id` (string/uuid, PK)
- `owner_username` (string, not null) - user scope key
- `order_type` (string enum value of `OrderType`, not null)
- `receiver_type` (string enum value of `ReceiverTargetType`, not null)
- `receiver_ref_id` (string, nullable only for transitional fallback)
- `receiver_name` (string snapshot for display)
- `priority` (string enum value of `PriorityOption`, not null)
- `blue_print` (boolean, not null)
- `order_rights` (string enum value of `OrderRights`, not null)
- `rightsholder` (string, not null)
- `created_by` (string)
- `created_date` (date)
- `updated_by` (string)
- `updated_date` (date)

Constraints:
- Unique: `owner_username` (one active preset per user).
- Enum-backed fields non-null.
- `receiver_ref_id` required when `receiver_type` is set after migration cutover.

### New entity: `OrderPresetDefaultsHistory`
Table (proposed): `builders.order_preset_defaults_history`

Fields:
- `id` (string/uuid, PK)
- `preset_id` (FK to `order_preset_defaults.id`)
- `owner_username`
- Full snapshot of preset fields (`order_type`, `receiver_type`, `receiver_ref_id`, `receiver_name`, `priority`, `blue_print`, `order_rights`, `rightsholder`)
- `changed_by`
- `changed_date`
- `change_reason` (optional)

Purpose:
- Immutable audit log of every update.

## Repository Layer
Add in `repository` module:
- `OrderPresetDefaultsRepository`
  - `Optional<OrderPresetDefaults> findByOwnerUsername(String ownerUsername)`
  - `OrderPresetDefaults save(...)`
- `OrderPresetDefaultsHistoryRepository`
  - `OrderPresetDefaultsHistory save(...)`
  - optional reader queries for admin history UI.

## Service Layer
Add in `service` module:
- `IOrderPresetDefaultsService`
- `OrderPresetDefaultsService`

Responsibilities:
- Resolve effective defaults for current user.
- If user has no row, return legacy fallback defaults.
- Validate enum compatibility and receiver consistency.
- Persist presets upsert-style by `owner_username`.
- Write history record for each create/update.

Validation:
- `receiverType` mandatory.
- `receiverRefId`/`receiverName` must match chosen source:
  - corporation exists for `CORPORATION`
  - user exists for `USER`
- `rightsholder` non-empty and compatible with `order_rights` policy (if policy is later added).

Fallback policy:
- No user record -> use legacy defaults.
- Broken receiver reference -> return preset with `receiverMissing=true` marker for UI; saving blocked until corrected.

## UI/API Layer Changes
### `IPropertiesController` / `PropertiesController`
Add methods:
- `OrderPresetDefaultsDto getOrderPresetDefaultsForCurrentUser()`
- `void saveOrderPresetDefaultsForCurrentUser(OrderPresetDefaultsDto dto)`
- `List<ManagedCorporation> getAllManagedCorporations()`
- `List<UserDto> getAllReceiverUsers()`

Keep destination methods intact.

### `PropertiesView`
- Add new "Preset Order Defaults" section with form controls.
- Load/save current user defaults through controller methods.
- Show warning when stored receiver is unresolved.
- Keep dictionary section independent.

### `ParametersOrderView`
- Replace hardcoded `applyDefaultValues` source with service-provided defaults for current user.
- Keep null-safe UI behavior for missing receiver references.

## DTO Contract
`OrderPresetDefaultsDto`:
- `OrderType orderType`
- `ReceiverTargetType receiverType`
- `String receiverRefId`
- `String receiverName`
- `PriorityOption priority`
- `BlueprintOption blueprint`
- `OrderRights orderRights`
- `String rightsholder`
- `boolean receiverMissing`

## Migration Strategy
1. Add both tables and indexes/constraints.
2. Add service with fallback-first behavior (no runtime break).
3. Populate initial per-user presets lazily on first save (or optional backfill script).
4. Switch `ParametersOrderView` to service-backed defaults.
5. Add `PropertiesView` preset editor.

## Backward Compatibility
- Existing order creation continues working with fallback defaults.
- No change to `Order` table required in this scope.
- Existing destination management remains unchanged.

## Error Handling and Observability
- Log when fallback is used (no per-user preset).
- Log unresolved receiver references.
- Log save/update operations with owner username.
- Return validation errors to UI with actionable messages.

## Test Strategy
### Unit tests
- Service: per-user fetch, save, validation, fallback, audit write.
- Controller: current-user routing + dto pass-through.
- View: receiver-type switching and unresolved receiver state.

### Integration tests
- Persistence round-trip for both entities.
- Unique constraint on `owner_username`.
- History record creation on update.

### Regression tests
- Order creation opens with effective per-user defaults.
- Existing request-based order creation path unaffected.

## Risks
- Stale receiver references when corp/user removed.
- Data drift if enum names are changed.
- Higher write volume due to audit table (acceptable for admin-level feature).