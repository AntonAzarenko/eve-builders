# Implementation Slice 03 - Notification Facade and Router (Telegram Only)

## Goal
Introduce a channel-agnostic facade while keeping Telegram as the only active provider.

## In Scope
- Add `INotificationService` abstraction.
- Add router using `app.notifications.provider`.
- Route to Telegram implementation only for now.

## Changes Planned (later)
- Replace direct Telegram calls in service layer with facade calls.
- No Discord send path yet.

## Regression Checklist
- All baseline flows still send Telegram notifications.
- No duplicate notifications introduced.

## Exit Criteria
- Business services no longer depend directly on Telegram integration class.
