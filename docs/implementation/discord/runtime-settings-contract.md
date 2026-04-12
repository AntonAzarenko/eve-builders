# Runtime Settings Contract (Slice 08)

## Current Source of Truth
- Notification runtime settings are env/application-property driven only.
- Effective properties:
  - `app.notifications.provider`
  - `app.notifications.telegram.enabled`
  - `app.notifications.discord.enabled`
  - `app.discord.webhook.order_url`
  - `app.discord.webhook.request_url`
  - `app.discord.thread.order_id`
  - `app.discord.thread.request_id`
  - `app.discord.wait_response`

## Runtime Precedence
1. OS/container environment variables.
2. `application-*.yml` defaults.

No DB/UI override exists yet.

## Provider Behavior Contract
- `telegram`: only Telegram provider is attempted.
- `discord`: only Discord provider is attempted.
- `both`: both providers are attempted independently.
- `none`: no provider is called.

Provider failures are isolated: one provider failure must not prevent the other provider call or business transaction completion.

## Migration Path to UI/DB Settings
1. Add a `NotificationSettingsProvider` adapter interface in service layer.
2. Keep current env-backed implementation as default adapter.
3. Add DB-backed implementation and expose admin UI controls.
4. Introduce explicit precedence flag (example: `app.notifications.settings-source=env|db`).
5. Roll out in stages:
   - Stage 1: DB read-only mirror for observability.
   - Stage 2: DB active in staging.
   - Stage 3: controlled production switch.

## Backward Compatibility Requirements
- If UI/DB settings are unavailable or invalid, fallback to env-backed settings.
- Existing Telegram envs must continue to work with no required new secrets.
