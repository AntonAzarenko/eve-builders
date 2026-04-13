# Discord Webhook Card Migration Plan

## Goal
Always send Discord notifications as webhook cards (`embeds`) while preserving existing business event flow and notification routing.

## Non-goals
- No changes to business event emit points.
- No changes to Telegram notification behavior.
- No runtime flag for switching between plain and card format.

## Current State
- `DiscordIntegrationService` posts plain text payload:
  - `{ "content": "..." }`
- Message text is built in `DiscordMessageCreatorService`.
- Event routing is centralized in `NotificationRouterService`.

## Target State
- `DiscordIntegrationService` always posts card payload:
  - `{ "content": "@everyone ...", "embeds": [ ... ], "allowed_mentions": { "parse": ["everyone"] } }`
- `embeds` provide the visual card structure.
- `content` includes `@everyone` so all channel members are notified.
- `content` is kept as a short fallback summary after the mention.

## Implementation Plan

## 1) Introduce payload builder for Discord cards
- Add `DiscordWebhookPayloadBuilder`.
- Responsibilities:
  - parse source message text,
  - produce embed title/description/fields,
  - enforce Discord text limits.

Result:
- Card formatting is isolated and easy to test.

## 2) Keep existing event call contracts intact
- Keep `IDiscordIntegrationService` public method signatures unchanged.
- Keep `NotificationRouterService` event dispatch unchanged.
- Keep current `DiscordMessageCreatorService` message generation unchanged for now.

Result:
- No application logic regression in event flow.

## 3) Migrate sender to model-based JSON serialization
- Replace manual JSON string building in `DiscordIntegrationService`.
- Use Spring `ObjectMapper` to serialize payload from `DiscordWebhookPayloadBuilder`.
- Keep endpoint/thread query logic unchanged.
- Keep sanitized error logging unchanged.

Result:
- Safer payload generation and less escaping risk.

## 4) Card templates by event
- `order_created`: fields for order id, ship name, qty, price, priority, fit link, deadline.
- `order_taken`: worker, qty, remaining, unit price, total.
- `progress_updated`: progress/finish report.
- `waiting_for_approval`: approval waiting status.
- `order_discarded`: discarded status.
- `order_removed`: compact removal card.
- For every event, prepend `@everyone` in `content` and allow everyone mentions in webhook payload.

Result:
- Consistent card-style rendering across all Discord events.

## 5) Discord limits and safety guards
- Enforce limits:
  - `content` <= 2000
  - `embed.title` <= 256
  - `embed.description` <= 4096
  - `embed.field.name` <= 256
  - `embed.field.value` <= 1024
  - max 25 fields per embed
- Truncate with ellipsis where required.

Result:
- Avoid avoidable Discord 400 responses due to payload limits.

## 6) Verification
- Compile build (`mvnw -DskipTests compile`).
- Validate all notification routes:
  - `provider=telegram|discord|both|none`.
- Validate webhook payload structure in logs/mock endpoint:
  - `embeds` always present for Discord sends.

## Rollout
1. Deploy with card payload always enabled.
2. Validate order lifecycle events in Discord channels.
3. Monitor Discord send errors after deploy.

## Acceptance Criteria
- Discord webhook messages are rendered as cards in all supported events.
- Existing business actions still trigger exactly the same notification events.
- Telegram path remains unchanged.
- Discord send failures do not break core order transactions.
