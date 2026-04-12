# Implementation Slice 04 - Discord Provider (Disabled by Default)

## Goal
Add Discord webhook sender implementation behind config toggle.

## In Scope
- Add Discord integration interface/impl.
- Add HTTP send + response handling + sanitized logging.

## Changes Planned (later)
- Implement webhook POST and minimal payload (`content`).
- Keep provider disabled by default.

## Regression Checklist
- With `provider=telegram`, behavior is unchanged.
- Startup works even if Discord env vars are absent.

## Exit Criteria
- Discord sender exists and can be enabled in non-prod without affecting default path.
