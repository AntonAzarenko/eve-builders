# Implementation Slice 01 - Baseline and Safety Net

## Goal
Freeze current behavior and define regression checks before any notification refactor.

## In Scope
- Capture current Telegram-triggered flows and expected outcomes.
- Define smoke tests for core order functionality.

## Changes Planned (later)
- No code changes in this slice.

## Regression Checklist
- Order create works.
- Order take/distribute works.
- Order progress update works.
- Send-for-approval works.
- Discard works.
- Remove order works.
- `/api/orders` and `/api/orders/validator` still behave as before.

## Exit Criteria
- Baseline behavior documented and test checklist agreed.
