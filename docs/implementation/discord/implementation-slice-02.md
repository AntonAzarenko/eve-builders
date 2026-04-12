# Implementation Slice 02 - Config Contract (Env-driven)

## Goal
Add notification provider configuration contract without changing runtime behavior.

## In Scope
- Define properties for provider routing and Discord credentials.
- Keep current Telegram keys intact.

## Properties
- `app.notifications.provider=telegram|discord|both|none`
- `app.notifications.telegram.enabled`
- `app.notifications.discord.enabled`
- `app.discord.webhook.order_url`
- `app.discord.webhook.request_url`
- `app.discord.thread.order_id`
- `app.discord.thread.request_id`
- `app.discord.wait_response`

## Changes Planned (later)
- Add properties to `application-local.yml`, `application-prod.yml`, and env file templates.
- Defaults keep existing behavior (`telegram`).

## Regression Checklist
- App starts with existing env values only.
- Telegram notifications continue unchanged.

## Exit Criteria
- Config schema merged with backward-compatible defaults.
