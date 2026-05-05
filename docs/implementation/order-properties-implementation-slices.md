# Order Properties Implementation Slices

## Slice 1: DB Schema for Per-User Presets
### Goal
Create persistent storage for user-scoped order preset defaults and audit history.

### Changes
- Add Liquibase changesets:
  - `builders.order_preset_defaults`
  - `builders.order_preset_defaults_history`
- Add unique index on `order_preset_defaults.owner_username`.
- Add indexes for history lookups: `preset_id`, `owner_username`, `changed_date`.

### Done Criteria
- Migration applies cleanly on empty and existing DB.
- Roll-forward tested locally.
- Constraints enforce one preset row per user.

## Slice 2: Domain + Repository Layer
### Goal
Introduce entities and repositories for presets and history.

### Changes
- Add domain entities in `domain` module:
  - `OrderPresetDefaults`
  - `OrderPresetDefaultsHistory`
- Add repositories in `repository` module:
  - `OrderPresetDefaultsRepository`
  - `OrderPresetDefaultsHistoryRepository`

### Done Criteria
- Repositories can save and read by `owner_username`.
- History entity supports immutable snapshot writes.

## Slice 3: Service Contract + Core Logic
### Goal
Add service that resolves effective defaults per current user with fallback and validation.

### Changes
- Add `IOrderPresetDefaultsService`.
- Implement `OrderPresetDefaultsService`:
  - `getDefaultsForCurrentUser()`
  - `saveDefaultsForCurrentUser(dto)`
- Add validation for enums, receiver reference, and `rightsholder`.
- Add fallback to legacy defaults when no row exists.
- Add audit write on create/update.

### Done Criteria
- Save performs upsert by `owner_username`.
- Every save writes one history record.
- Missing/broken receiver is flagged (`receiverMissing=true`) and save is blocked until valid.

## Slice 4: Controller API for Properties Page
### Goal
Expose presets to UI via `IPropertiesController`.

### Changes
- Extend `IPropertiesController` and `PropertiesController` with:
  - `getOrderPresetDefaultsForCurrentUser()`
  - `saveOrderPresetDefaultsForCurrentUser(...)`
  - receiver source loaders (corporations/users)
- Keep destination dictionary endpoints unchanged.

### Done Criteria
- Controller returns typed DTO for current user.
- Save endpoint validates and propagates domain errors.

## Slice 5: PropertiesView Preset Section UI
### Goal
Add editable "Preset Order Defaults" section to `PropertiesView`.

### Changes
- Add form controls for:
  - order type, receiver type/value, priority, blueprint, order rights, rightsholder
- Add `Save`/`Reset` actions.
- Add unresolved receiver warning state.
- Add i18n keys for labels/errors/buttons.

### Done Criteria
- Admin can load/edit/save per-user presets.
- Validation errors shown in UI.
- Existing destination section still works unchanged.

## Slice 6: ParametersOrderView Integration
### Goal
Use persisted per-user presets as defaults when creating a new order.

### Changes
- Replace hardcoded defaults in `applyDefaultValues()` with controller/service data.
- Preserve current behavior for edit/request flows.
- Preserve null-safe handling when preset receiver is unresolved.

### Done Criteria
- New order form opens with per-user saved defaults.
- If no saved presets, fallback defaults are applied.

## Slice 7: Auditing Visibility (Optional UI Read)
### Goal
Provide basic visibility into preset changes for admins.

### Changes
- Optional read endpoint + simple grid/modal in `PropertiesView` for history.
- Show changed date, changed by, key fields snapshot.

### Done Criteria
- Admin can inspect recent preset changes.

## Slice 8: Tests and Regression Net
### Goal
Stabilize feature with automated tests.

### Changes
- Unit tests:
  - service fallback, validation, save, audit
  - controller mapping
- Integration tests:
  - repository + constraints + audit writes
- UI/unit tests where feasible for form logic.
- Regression tests for order creation defaults path.

### Done Criteria
- All new tests green.
- Existing relevant test suites remain green.

## Slice Ordering Rationale
- DB and domain first to avoid API churn.
- Service before UI to keep view wiring thin.
- `ParametersOrderView` switch happens only after presets become writable/readable.
- Tests finalize each critical boundary.