# Implementation Slice 07 - Duplicate Notification Fix

## Goal
Ensure one business action emits one notification event.

## In Scope
- Remove controller-level duplicate emit path(s).
- Keep emission in authoritative service layer.

## Changes Planned (later)
- Fix known duplicate finish-report path.
- Add tests for exactly-once event emission at action level.

## Regression Checklist
- No duplicate finish notifications.
- Other notification events remain unchanged.

## Exit Criteria
- Duplicate path removed and covered by tests.
