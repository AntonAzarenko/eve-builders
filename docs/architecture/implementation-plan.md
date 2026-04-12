# Notification Integration Implementation Plan

## Objective
Implement Discord outbound notifications while preserving Telegram integration, and support runtime channel selection via environment properties now, with UI-based control later.

Scope of this plan:
- Keep both implementations in code.
- Switch active channel(s) via `env`/application properties.
- Do not remove existing Telegram inbound endpoints (`/api/orders`, `/api/orders/validator`).
- No code implementation in this document.

## Baseline (Current State)
1. Telegram outbound is implemented and used by business flows:
- order created
- order removed
- order taken/distributed
- progress/finish update
- send for approval
- discard

2. Telegram inbound order intake API exists and must remain unchanged.

3. Discord integration is planned as outbound webhook-only.

## Target Architecture

### 1. Introduce a channel-agnostic notification facade
Create one internal abstraction for business services to call, for example:
- `INotificationService` (or `IOrderNotificationService`)

Business services (`OrderService`, `DistributedOrderService`, etc.) should call this facade, not Telegram/Discord implementations directly.

### 2. Keep provider implementations side-by-side
- Telegram provider: existing `ITelegramIntegrationService` implementation.
- Discord provider: new webhook implementation.

Both remain available in code regardless of active configuration.

### 3. Add routing layer (switch logic)
Add a router/dispatcher that selects provider(s) by config:
- `telegram`
- `discord`
- `both`
- `none` (optional safe mode)

This routing decision should be centralized in one place.

### 4. Normalize event model
Define explicit domain notification events instead of raw text at call sites, e.g.:
- `ORDER_CREATED`
- `ORDER_REMOVED`
- `ORDER_TAKEN`
- `ORDER_PROGRESS_UPDATED`
- `ORDER_WAITING_FOR_APPROVAL`
- `ORDER_DISCARDED`

Then provider-specific formatters map event payload to Telegram/Discord message structures.

## Configuration Plan (Env-driven now)

### 1. Core switch
Add property:
- `app.notifications.provider` with values: `telegram|discord|both|none`

Environment variable example:
- `APP.NOTIFICATIONS.PROVIDER=both`

### 2. Provider-specific enable flags (optional but recommended)
- `app.notifications.telegram.enabled=true|false`
- `app.notifications.discord.enabled=true|false`

### 3. Discord config
- `app.discord.webhook.order_url`
- `app.discord.webhook.request_url`
- `app.discord.thread.order_id` (optional)
- `app.discord.thread.request_id` (optional)
- `app.discord.wait_response`

Env examples:
- `APP.DISCORD.WEBHOOK.ORDER.URL=...`
- `APP.DISCORD.WEBHOOK.REQUEST.URL=...`

### 4. Telegram config
Keep existing keys unchanged to avoid breaking deployments:
- `app.telegram_bot.token`
- `app.telegram_chat_id`
- `app.telegram_thread_ping_id`
- `app.telegram_thread_request_id`

### 5. Environment file strategy
Use existing `env/env.local.properties` pattern for local development and deployment env vars for prod.

## Message Composition Plan

### 1. Separate builders per provider
- `TelegramMessageCreatorService` (existing)
- `DiscordMessageCreatorService` (new)

### 2. Single source payload
Notification event payload should be neutral (structured fields). Builders then transform to provider format:
- Telegram: MarkdownV2 escaped text
- Discord: `content` (and later `embeds` if needed)

### 3. Limit handling
- Truncate/split Discord `content` > 2000 chars.
- Keep mention behavior explicit (`allowed_mentions` for Discord).

## Business Flow Refactor Plan

### Phase A: Safe abstraction
1. Introduce notification facade + router.
2. Rewire existing Telegram call sites to facade (no behavior change).
3. Validate parity with current Telegram behavior.

### Phase B: Discord provider
1. Implement Discord webhook sender.
2. Implement Discord formatter for same event set.
3. Enable with `app.notifications.provider=discord` in test env.

### Phase C: Dual-send mode
1. Enable `both` mode for staged rollout.
2. Compare delivery and formatting side-by-side.
3. Finalize operational defaults.

## Duplicate-Notification Risk Handling
Current flow indicates a likely duplicate finish notification path (`DistributedOrderService.update(...)` and `CorporationConstructionController.saveOrder(...)`).

Plan:
1. Define authoritative emission points in service layer only.
2. Remove/avoid controller-level direct notification sends.
3. Add tests asserting one business action => one notification event.

## Error Handling and Reliability Plan
1. Provider failures must not break core order transaction by default.
2. Retry policy:
- Telegram: keep current behavior first, then align with shared policy.
- Discord: retry only on 429/5xx with bounded backoff.
3. Sanitize logs:
- never log webhook URL/token
- log channel key + status + event type

## Observability Plan
Add metrics and structured logs by provider/event:
- sent count
- failed count
- rate-limited count
- latency

This enables confidence during `both` rollout.

## Testing Plan
1. Unit tests:
- router selection by property value
- event-to-message mapping per provider
- truncation and mention rules

2. Integration tests (mock HTTP server):
- Telegram request payload/endpoint
- Discord webhook payload/endpoint
- retry behavior on 429/5xx

3. Regression checks:
- existing Telegram inbound APIs unchanged
- no duplicate event sends

## UI Switching (Future Plan)

### Goal
Allow admins to switch notification provider from UI without code changes.

### Proposed approach
1. Persist notification settings in DB (new settings table or existing config mechanism).
2. Add admin UI page with fields:
- active provider (`telegram|discord|both|none`)
- per-provider enabled flags
- destination mapping (order/request channels)
3. Implement runtime-refresh strategy:
- option A: cached settings with periodic refresh
- option B: explicit "Apply" action that reloads settings bean

### Transition strategy
Until UI is implemented, env/application properties remain source of truth.
UI implementation should override properties only after explicit migration.

## Rollout Checklist
1. Implement facade and router with Telegram only.
2. Add Discord provider and config schema.
3. Deploy with `telegram` mode first.
4. Test in staging with `both` mode.
5. Verify logs/metrics and duplicate prevention.
6. Move production to desired mode (`both` or `discord`).
7. Implement UI switching in a separate iteration.

## Non-Goals
- Implementing Discord bot/gateway functionality.
- Replacing Telegram inbound `/api/orders*` flow.
- Full async outbox/queue architecture in this iteration.
