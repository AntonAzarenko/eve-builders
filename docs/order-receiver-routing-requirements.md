# Order Receiver Routing: Requirement Change Plan

## Goal
Allow an admin, at order creation time, to choose **who accepts the order**:
- corporation
- user

And then choose a concrete receiver entity of the selected type.

Also remove dependency on hardcoded `app.eve.corporation.id` in contract validation flow and resolve contract search target from the order itself.

---

## Current State (As-Is)

### UI
- `ParametersOrderView` has one field `receiverField` (`ComboBox<String>`) labeled `management.label.receiver`.
- Receiver values are loaded from `receivers` property table via `IOrderService.getAllReceivers()`.
- No receiver type (`corporation/user`) exists in create-order form.

### Domain / DB
- `Order` has only one receiver field: `receiver` (string).
- No structured receiver target info (type, user id, corporation id).

### Contract validation/search
- `ContractService` uses hardcoded configuration:
  - `@Value("${app.eve.corporation.id}") private Long corporationId;`
- Corporation contracts are always fetched by this static corporation id.
- Character contracts are fetched for distributed order owner, but source selection logic is not driven by order receiver type.

---

## Required To-Be Behavior

1. Admin can select receiver type in create order form:
- `Corporation`
- `User`

2. Admin can select concrete receiver entity based on selected type:
- if `Corporation`: select a corporation
- if `User`: select a user

3. Order stores enough structured data to resolve contract source at validation time without static property.

4. Contract search mode depends on order receiver type:
- corporation mode: use corporation contracts endpoint
- user mode: use character contracts endpoint

---

## Change Scope

## 1) Data Model & Persistence

### 1.1 Order model
Add structured receiver fields in `Order`:
- `receiverType` (enum/string: `CORPORATION` | `USER`)
- `receiverRefId` (string/long id of target entity in EVE terms)
- `receiverName` (display name snapshot; optional but recommended)

Keep existing `receiver` temporarily for backward compatibility and migration period.

### 1.2 DB migration
Add columns to `builders.orders`:
- `receiver_type`
- `receiver_ref_id`
- `receiver_name`

Migration/backfill strategy for legacy rows:
- set `receiver_type` to default `CORPORATION` (or `UNKNOWN`) for old data
- keep old `receiver` value in `receiver_name` where possible

---

## 2) UI (Create Order)

### 2.1 Replace single receiver field
In `ParametersOrderView` replace current `receiverField` layout with two combo boxes:
- `receiverTypeField` (Corporation/User)
- `receiverValueField` (concrete values according to selected type)

### 2.2 Data sources for second combo
- For corporation list: use managed corporations source (already exists in app, entity `ManagedCorporation`)
- For users list: use active users from `IUserService`/controller DTO

### 2.3 Binding and validation
Binder rules:
- `receiverTypeField`: required
- `receiverValueField`: required
- `receiverValueField` options must be reloaded when type changes
- on type change, clear previously selected value

### 2.4 i18n
Add translation keys for:
- receiver type label
- receiver type options
- second combo label (dynamic or generic)
- validation error messages for receiver selection

---

## 3) Controller / Service API for Create Order

Update `ICreateOrderController` and `CreateOrderViewController` to provide typed receiver datasets:
- list of corporation receiver options (id + name)
- list of user receiver options (characterId + username)

Do not reuse legacy free-text receiver property as the source of truth for this new flow.

---

## 4) Contract Service Logic

### 4.1 Remove static target source
Deprecate and remove usage of:
- `app.eve.corporation.id` in `ContractService`

### 4.2 Resolve target from order
For each distributed order, load original `Order` and derive contract source:
- if receiver type is `CORPORATION`: use `receiverRefId` as corporation id
- if receiver type is `USER`: use `receiverRefId` as character id

### 4.3 Search strategy
- Corporation receiver:
  - `EveContractsIntegrationService.getCorporationContracts(token, corporationId)`
  - contract items via corporation contract items endpoint
- User receiver:
  - `EveContractsIntegrationService.getCharacterContracts(token, characterId)`
  - define item resolution approach for character-contract path (see open questions)

---

## 5) Validation Logic Update

Current validation should be updated to consume new receiver fields.

Minimum required checks:
- receiver type exists
- receiver target id exists
- target id is valid for chosen type
- contract existence/approval checks use selected receiver target

---

## 6) Backward Compatibility

For existing orders without new receiver fields:
- fallback behavior must be explicitly defined:
  - Option A: treat as corporation and use legacy path
  - Option B: mark as non-validatable until updated

Recommendation: Option A for smooth rollout, then remove fallback after data cleanup.

---

## 7) Testing Plan (what must be covered)

### Unit tests
- receiver type/value binder validation
- mapping from UI selection to order fields
- contract source resolver for both receiver types
- fallback behavior for legacy orders

### Integration tests
- create order with corporation receiver, validate contract search path
- create order with user receiver, validate contract search path
- ensure no dependency on `app.eve.corporation.id`

### E2E/manual checks
- Create order -> Corporation -> specific corp -> saved and validated
- Create order -> User -> specific user -> saved and validated
- Existing old order still viewable and processable

---

## Open Questions (to resolve before implementation)

1. For `User` receiver path, how to validate contract items if items endpoint currently uses corporation route?
2. Should `receiverName` be immutable snapshot or always resolved dynamically?
3. Should legacy `receivers` property table be removed or retained for other modules?
4. Should receiver type be editable after order creation?

---

## Files Most Likely Affected (implementation phase)

- UI:
  - `ui/.../main/managment/create/ParametersOrderView.java`
  - `ui/.../main/managment/api/ICreateOrderController.java`
  - `ui/.../main/managment/create/CreateOrderViewController.java`
  - `ui/.../resources/.../translate_en.properties`
  - `ui/.../resources/.../translate_ru.properties`

- Service:
  - `service/.../impl/contract/ContractService.java`
  - `service/.../impl/intergarion/EveContractsIntegrationService.java` (if needed for user contract item flow)
  - `service/.../impl/order/OrderService.java` (mapping/persistence adjustments)

- Domain/DB:
  - `domain/.../db/Order.java`
  - Liquibase changelog in `db/src/main/resources/db/changelog/...`


## Legacy Orders Safety (non-NULL fields + non-ARCHIVED flow)

Because historical orders/contracts do not have values for new receiver fields, and new columns must be `NOT NULL`, rollout must be split into safe stages.

### Mandatory migration strategy

1. Add new columns with temporary defaults (or nullable in first migration), backfill, then enforce `NOT NULL`.
2. Backfill all existing `orders` rows before enabling new runtime logic.
3. Only after backfill + verification, apply final `NOT NULL` constraint.

### Backfill policy for existing orders

For all existing orders:
- `receiver_type`: set to `CORPORATION` by default (legacy behavior)
- `receiver_ref_id`: set to legacy corporation id source (currently equivalent to old static route)
- `receiver_name`: keep existing `receiver` text when present, otherwise derive from target corporation name

This preserves old behavior for contract lookup and avoids runtime null checks in critical flow.

### Special rule for non-ARCHIVED orders

Non-ARCHIVED orders (e.g. `NEW`, `IN_PROGRESS`, `DISTRIBUTED`, `WAITING_FOR_APPROVAL`, `COMPLETED`, `STOPPED`, `DISCARDED`, `EXPIRED`) must be fully backfilled before release cutover.

Release guard:
- If any non-ARCHIVED order has missing/invalid receiver routing fields, startup job (or migration verification query) must fail deployment.

Reason:
- non-ARCHIVED orders can still enter contract validation/status transitions; null/invalid routing would break application behavior.

### ARCHIVED orders

ARCHIVED rows can be backfilled in same migration (preferred) or in secondary batch, but still must satisfy final `NOT NULL` before constraint enforcement.

### Runtime fallback (temporary)

During transition window only:
- if order routing fields are missing, fallback to legacy corporation path and log warning with order number.
- remove fallback after data verification and one release cycle.

### Verification SQL checklist (pre-constraint)

- no nulls in new fields:
  - `receiver_type IS NULL` = 0
  - `receiver_ref_id IS NULL` = 0
  - `receiver_name IS NULL` = 0
- no invalid non-ARCHIVED rows:
  - status != `ARCHIVED` and (receiver fields invalid) = 0

### Rollback safety

If migration/backfill partially fails:
- keep old logic active,
- do not enforce `NOT NULL`,
- block feature toggle for typed receiver routing.

