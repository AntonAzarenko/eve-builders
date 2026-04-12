# Implementation Slice 01 - Baseline and Safety Net

## Goal
Freeze current Telegram behavior and define a repeatable regression runbook before notification refactor.

## Scope
- No application code changes.
- Baseline only: current emit points, API contracts, and smoke validation steps.

## Current Baseline (As-Is)

### Outbound notification emit points
1. Order created
- Emitter: `OrderService.saveOrder(...)`
- Channel: Telegram `thread_ping_id`

2. Order removed
- Emitter: `OrderService.removeOrder(...)`
- Channel: Telegram `thread_ping_id`

3. Order taken/distributed
- Emitter: `DistributedOrderService.save(...)` and `/api/orders` flow via `distributeOrder(...)`
- Channel: Telegram `thread_request_id`

4. Order progress/finish updated
- Emitter: `DistributedOrderService.update(...)`
- Channel: Telegram `thread_request_id`

5. Send for approval
- Emitter: `DistributedOrderService.sendOrderForApproval(...)`
- Channel: Telegram `thread_request_id`

6. Distributed order discarded
- Emitter: `DistributedOrderService.discardOrder(...)`
- Channel: Telegram `thread_request_id`

Known baseline risk:
- Potential duplicate finish notification path through controller-level send call in addition to service-level send. Keep as-is for slice 01 and verify in slice 07.

### Inbound order intake APIs (must remain unchanged)
1. `POST /api/orders`
- Body: `TelegramRequestOrder` (`user_name`, `order_number`, `count`)
- Expected: `201` + `DistributedOrder`

2. `POST /api/orders/validator`
- Body: `TelegramRequestOrder` (`user_name`, `order_number`, `count`)
- Expected: `201` + `List<String>` errors

## Regression Runbook (To Execute Before and After Each Next Slice)

### Preconditions
- App starts successfully with current Telegram env values.
- Test users/orders available in test environment.

### Smoke scenarios
1. Create order from UI.
- Expect DB order persisted.
- Expect Telegram message in ping thread.

2. Take/distribute order from UI.
- Expect in-progress counters updated.
- Expect Telegram take-order message in request thread.

3. Report progress/finish.
- Expect ready counters/status updated.
- Expect Telegram finish/progress message in request thread.

4. Send for approval.
- Expect status changed to waiting-for-approval when contract precondition passes.
- Expect Telegram approval message in request thread.

5. Discard distributed order.
- Expect status/counters rolled back correctly.
- Expect Telegram discard message in request thread.

6. Remove order.
- Expect order deleted.
- Expect Telegram info message in ping thread.

7. Validate external order intake API.
- `POST /api/orders/validator` with valid payload returns empty error list.
- `POST /api/orders` with valid payload creates/updates distributed order as expected.

### Evidence capture template
- Date/time:
- Environment:
- Scenario id:
- Result: `PASS|FAIL`
- Notes (IDs, order number, screenshots/log references):

## Exit Criteria
- Baseline matrix accepted.
- Regression runbook accepted and used as gate for slice 02+.
