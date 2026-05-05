# Order Properties Page Research

## Context
Current `PropertiesView` now shows only one editable dictionary: `Destination`.
Business request: this page must also manage **preset defaults** for order creation (used by `ParametersOrderView`).

Current defaults are hardcoded in UI (`ParametersOrderView`):
- Order type: `REDEMPTION`
- Receiver type: `CORPORATION`
- Corporation: `Scan Stakan` (if exists)
- Priority: `MEDIUM`
- Blueprint for issue: `NO`
- Rights holder: `GROUP`

## Goal
Design a clear admin page section where defaults are visible, editable, and validated, so order creation defaults are configurable without code changes.

## Non-goals
- No implementation details of persistence in this document.
- No migration scripts.
- No permission model redesign beyond existing admin access.

## User Roles
- `ROLE_ADMIN`, `ROLE_SUPER_ADMIN` can view and update defaults.
- Regular users do not access this page.

## Information Architecture
Page structure recommendation:
1. Header
2. Section A: "Reference Properties" (current destination dictionary)
3. Section B: "Preset Order Defaults" (new)

Rationale:
- Keeps existing behavior intact.
- Separates dictionaries from behavioral config.
- Reduces confusion for admins.

## Preset Defaults Section: Fields
Required configurable fields:
1. Order type (`OrderType`): `REDEMPTION | MARKET`
2. Receiver type (`ReceiverTargetType`): `CORPORATION | USER`
3. Receiver value:
   - if `CORPORATION`: managed corporation selector
   - if `USER`: receiver user selector
4. Priority (`PriorityOption`): `LOW | MEDIUM | HIGH`
5. Blueprint (`BlueprintOption`): `YES | NO`
6. Rights holder (`OrderRights`): `CORPORATION | GROUP | PERSONAL`

## UX Behavior
- Load current saved defaults on page open.
- Save action validates and persists as one configuration set.
- Cancel action restores last saved values.
- Show success/error notification.
- If referenced corporation/user is missing:
  - show warning badge "Value not found"
  - do not auto-clear silently
  - save should be blocked until corrected

## Validation Rules
- `receiverType` is mandatory.
- `receiverRefId` + `receiverName` must match selected `receiverType`.
- All enum-backed defaults are mandatory.
- Invalid enum string values from storage should fail fast and show admin-visible error.

## Suggested Visual Layout
Use a card-like form in one column:
- Row 1: Order type, Priority
- Row 2: Receiver type, Receiver value
- Row 3: Blueprint, Rights holder
- Footer: `Save`, `Reset`

Keep labels translated via `getTranslation(...)` keys.

## Localization Keys Needed (proposal)
- `properties.preset.title`
- `properties.preset.order_type`
- `properties.preset.receiver_type`
- `properties.preset.receiver_value`
- `properties.preset.priority`
- `properties.preset.blueprint`
- `properties.preset.rights_holder`
- `properties.preset.save`
- `properties.preset.reset`
- `properties.preset.missing_value`

## Edge Cases
- Managed corporation list is empty.
- User receiver list is empty.
- Stored receiver references deleted entity.
- Concurrent updates by two admins (last write wins unless optimistic locking is added).

## Acceptance Criteria (Design)
- Admin can see all default order parameters in one place.
- Admin can edit and save defaults.
- Invalid/missing linked entities are explicit in UI.
- Existing destination management remains available.
- All visible labels/messages are translatable.