# Implementation Slice 06 - Dual Mode and Staging Rollout

## Goal
Enable `both` mode and verify side-by-side delivery in staging.

## In Scope
- Router sends to both providers when configured.
- Add non-blocking failure policy per provider.

## Changes Planned (later)
- Implement per-provider error isolation.
- Add structured logs for provider/event/status.

## Regression Checklist
- Core order transactions complete if one provider fails.
- Telegram and Discord both receive expected events in staging.

## Exit Criteria
- `both` mode validated with no functional regressions.
