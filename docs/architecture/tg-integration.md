# Telegram Integration

## Scope
This document describes how Telegram is integrated in the application as of 2026-04-12.

Integration has two directions:
1. Inbound HTTP endpoints used by an external Telegram-side client/bot to assign orders.
2. Outbound notifications sent from the app to Telegram topics (threads) via Telegram Bot API.

## Configuration
Telegram settings are loaded from `ui/src/main/resources/application-local.yml` and `application-prod.yml`:
- `app.telegram_bot.token` <- `${APP.TELEGRAM_BOT.TOKEN}`
- `app.telegram_chat_id` <- `${APP.TELEGRAM_CHAT_ID}`
- `app.telegram_thread_ping_id` <- `${APP.TELEGRAM_THREAD_PING_ID}`
- `app.telegram_thread_request_id` <- `${APP.TELEGRAM_THREAD_REQUEST_ID}`

Example local env variable names are in `env/env.local.properties`.

## Endpoints

### Inbound (into this app)
Defined in `ui/src/main/java/com/azarenka/evebuilders/rest/OrderReceiverController.java`.

1. `POST /api/orders`
- Consumes/produces: `application/json`
- Body DTO: `TelegramRequestOrder`
  - `user_name` (string)
  - `order_number` (string)
  - `count` (int)
- Behavior: assigns part of an order to a user by calling `distributedOrderService.distributeOrder(...)`.
- Response: `201 Created` with `DistributedOrder`.

2. `POST /api/orders/validator`
- Consumes/produces: `application/json`
- Body DTO: `TelegramRequestOrder`
- Behavior: validates request data before assignment (`order exists`, `user exists`, `enough free quantity`).
- Response: `201 Created` with `List<String>` validation errors (empty list means valid).

Notes:
- `@CrossOrigin(origins = "*")` is enabled on this controller.
- No Telegram webhook endpoint exists in this repository (no `setWebhook`/update handling found).

### Outbound (from this app to Telegram)
Implemented in `service/src/main/java/com/azarenka/evebuilders/service/impl/intergarion/TelegramIntegrationService.java`.

Telegram API call:
- `POST https://api.telegram.org/bot{token}/sendMessage`
- Payload fields:
  - `chat_id`
  - `message_thread_id`
  - `text`
  - optional `parse_mode: MarkdownV2` (enabled for `sendMessage`, disabled for `sendInfoMessage`)

If Telegram responds with non-200, the app logs an error.

## Functional Triggers (When Telegram is Used)

### Thread: `app.telegram_thread_ping_id` ("ping"/new-order channel)
Used from `OrderService`:
1. `saveOrder(...)`
- Trigger: manager/admin creates a new order in UI.
- Telegram action: send new order summary (`createOrderMessage(...)`).
- Purpose: notify builders/staff that a new order is available.

2. `removeOrder(...)`
- Trigger: manager removes an order.
- Telegram action: send info message "order deleted".
- Purpose: operational cleanup signal.

### Thread: `app.telegram_thread_request_id` (work-progress/request channel)
Used mainly from `DistributedOrderService`:
1. `save(...)` / `distributeOrder(...)`
- Trigger: user takes order quantity (from UI or `/api/orders` Telegram flow).
- Telegram action: `createTakeOrderMessage(...)`.
- Purpose: announce who took how many units.

2. `update(...)`
- Trigger: user reports produced/ready quantity.
- Telegram action: `createFinishOrderMessage(...)`.
- Purpose: progress/contract completion reporting.

3. `sendOrderForApproval(...)`
- Trigger: user sends order to approval state.
- Telegram action: `createWaitingForApprovalMessage(...)`.
- Purpose: ask coordinators/managers to validate contract.

4. `discardOrder(...)`
- Trigger: user cancels their accepted distributed order.
- Telegram action: `createDiscardOrderMessage(...)`.
- Purpose: communicate cancellation and released capacity.

Additional caller:
- `CorporationConstructionController.saveOrder(...)` also sends `createFinishOrderMessage(...)` after calling `distributedOrderService.update(...)`.
- Since `update(...)` already sends the same message, this path can produce duplicate finish notifications.

## Message Formatting
`TelegramMessageCreatorService` builds message text templates:
- New order
- Take order
- Finish/progress report
- Waiting for approval
- Discard/cancel

Most messages are escaped for MarkdownV2 before sending.

## Data/Flow Summary
1. External Telegram-side tool validates candidate assignment via `POST /api/orders/validator`.
2. External Telegram-side tool commits assignment via `POST /api/orders`.
3. App updates DB state (`Order`, `DistributedOrder`, audit) and sends operational notifications to Telegram threads.
4. Internal UI flows (create, take, progress, approval, discard, delete) also emit Telegram notifications through the same integration service.
