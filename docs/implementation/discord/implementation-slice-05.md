# Implementation Slice 05 - Event Mapping for Discord

## Goal
Map existing business events to Discord message payloads.

## In Scope
- Add provider-specific message builder for Discord.
- Cover same event set as Telegram:
  - order created
  - order removed
  - order taken
  - progress updated
  - waiting for approval
  - order discarded

## Changes Planned (later)
- Add size guards (2000 char content limit).
- Add safe mentions policy.

## Regression Checklist
- Telegram output unchanged.
- Discord payloads valid for each event type in tests.

## Exit Criteria
- Event-to-discord mapping complete and testable.
