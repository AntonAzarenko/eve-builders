# Discord Webhook Integration (Design Only)

## Goal
Add outbound Discord notifications from this app by sending messages to Discord webhooks.

This document is architecture guidance only. No code changes are included.

## Current Project Context
- Stack: Spring Boot 3.x, Java 17/21 multi-module Maven.
- Existing pattern: Telegram integration already sends outbound HTTP requests from `service` module via `java.net.http.HttpClient`.
- `service` module already includes `spring-boot-starter-webflux`, so `WebClient` is available.

## Recommended Integration Scope
Use **Discord Incoming Webhooks** only:
- App sends HTTP `POST` to Discord webhook URL.
- No bot gateway connection required.
- No slash commands required.
- This is the simplest path for app-to-channel notifications.

## Discord Endpoint and Request Model
Webhook send endpoint format:
- `POST https://discord.com/api/webhooks/{webhook.id}/{webhook.token}`

Useful query params:
- `wait=true` to get message response and stronger delivery confirmation.
- `thread_id=<id>` to post into a specific thread.

Minimal JSON payload:
- `content` (string, up to 2000 chars)

Common optional payload fields:
- `username`
- `avatar_url`
- `embeds` (up to 10)
- `allowed_mentions` (important to control pings)

## Libraries to Add

### Required libraries
**None required** for webhook sending in this codebase.

Reason:
- You already can use either `java.net.http.HttpClient` (already used by Telegram service) or Spring `WebClient` (already present via `spring-boot-starter-webflux` in `service/pom.xml`).

### Optional libraries (only if needed)
1. `io.github.resilience4j:resilience4j-spring-boot3`
- Add if you want standardized retry/backoff/circuit-breaker behavior around webhook calls.

2. `org.springframework.boot:spring-boot-starter-actuator`
- Add if you want delivery metrics/health visibility (success rate, 429s, failures).

3. Discord-specific helper libraries
- Not recommended for this scope unless you need advanced Discord bot features.
- For plain webhooks, direct HTTP is lower complexity and easier to maintain.

## Suggested Internal Architecture
1. Introduce interface:
- `IDiscordIntegrationService` in `service.api.integration`.

2. Add implementation:
- `DiscordIntegrationService` in `service.impl.intergarion` (keep existing package convention).

3. Add message builder utility:
- `DiscordMessageCreatorService` similar to Telegram message creator, but for Discord JSON payload shape.

4. Call sites:
- Reuse same business triggers currently used for Telegram (order created, taken, progress updated, approval requested, discard, delete).
- Optionally run both channels in parallel (Telegram + Discord) behind config flags.

## Configuration Design
Add app settings in `application-local.yml` and `application-prod.yml`:
- `app.discord.enabled`
- `app.discord.webhook.order_url`
- `app.discord.webhook.request_url`
- `app.discord.thread.order_id` (optional)
- `app.discord.thread.request_id` (optional)
- `app.discord.wait_response` (`true/false`)

Back with env vars:
- `APP.DISCORD.ENABLED`
- `APP.DISCORD.WEBHOOK.ORDER.URL`
- `APP.DISCORD.WEBHOOK.REQUEST.URL`
- `APP.DISCORD.THREAD.ORDER.ID`
- `APP.DISCORD.THREAD.REQUEST.ID`

Security note:
- Webhook URL contains secret token. Treat it like a password.
- Never log full webhook URLs.
- Store only in environment/secret manager, not in git.

## Delivery, Reliability, and Error Handling
Handle responses explicitly:
- `2xx`: success.
- `4xx`: payload/config problem (do not blindly retry, except 429).
- `429`: rate limited; obey `Retry-After` and/or `retry_after`.
- `5xx`: transient server issue; retry with capped backoff.

Recommended retry policy:
- Retry only on `429` and `5xx`.
- Small capped backoff with jitter.
- Protect business flow: webhook failure should not roll back core DB transaction unless explicitly required.

## Rate Limit Considerations
- Do not hardcode request-per-second assumptions.
- Parse and respect Discord rate-limit headers (`X-RateLimit-*`) and `Retry-After`.
- If event volume grows, consider queue-based async delivery (outbox pattern) instead of inline send.

## Payload Limits to Respect
- `content`: up to 2000 characters.
- `embeds`: up to 10.
- Total embed text limits apply (combined embed text budget is limited by Discord).
- Validate/truncate long generated messages before sending.

## Mention Safety
Use `allowed_mentions` to avoid accidental mass ping spam from user-generated content:
- Prefer disabling broad mentions by default.
- Allow specific user/role mentions only when explicitly intended.

## Observability and Operations
Log per send attempt with:
- event type
- destination key (not full URL)
- status code
- short error body (sanitized)
- retry count

Recommended metrics:
- `discord_webhook_sent_total`
- `discord_webhook_failed_total`
- `discord_webhook_rate_limited_total`
- send latency histogram

## Rollout Plan (No Code Yet)
1. Define config schema and secret injection in environments.
2. Implement Discord service parallel to Telegram service.
3. Start with one low-risk event (for example: order created).
4. Validate formatting and rate-limit behavior.
5. Expand to full event set.
6. Optionally de-duplicate/coordinate Telegram+Discord notifications.

## Important Known Risk in Current Flow
Current Telegram flow appears to have a duplicate finish-notification path in one controller flow. When adding Discord, avoid copying this behavior. Ensure one business event produces one outbound Discord message.

## Primary References
- Discord Webhook resource: https://docs.discord.com/developers/resources/webhook/
- Discord rate limits: https://docs.discord.com/developers/topics/rate-limits
- Discord message/embed limits: https://docs.discord.com/developers/resources/message
- Discord threads + webhooks note: https://docs.discord.com/developers/topics/threads
