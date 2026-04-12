# Orders Managment Tab - Implementation Plan

## Goal
Add a new `Managment` tab to the Orders managment menu and route it to a dedicated view page.

## Current Structure
- Main manager tabs are registered in `MenuManagerPage`:
  - `DashboardView`
  - `AddOrderPage`
  - `PropertiesView`
- Manager routing base is `manager` (`@RoutePrefix("manager")`).
- Existing nested page pattern uses a parent page (for example `AddOrderPage`) with child views.

## Planned Implementation (No code yet)
1. Create new package and view classes for the tab content.
   - Package: `com.azarenka.evebuilders.main.managment.orders`
   - New main page class (example): `OrdersManagmentPage`.
   - Optional child/default view class (example): `OrdersManagmentView`.

2. Add routing for the new tab.
   - Register route under manager menu so URL is consistent with existing tabs.
   - Follow existing access control annotations (`ROLE_ADMIN`, `ROLE_SUPER_ADMIN`).

3. Register the new tab in `MenuManagerPage`.
   - Add `addView(...)` for the new page.
   - Provide icon and test id (`tab-...`) consistent with existing tab naming.

4. Update locale change handling in `MenuManagerPage`.
   - Add translation update for the new tab index in `localeChange(...)`.

5. Add i18n key for tab label.
   - Add new key in translation files (for example `tab.manager.management`).
   - Use same naming convention as existing keys like `tab.manager.dashboard`.

6. Implement initial tab content.
   - Start with a minimal placeholder layout (header + description), then evolve with business controls.
   - Keep structure compatible with existing `View`/`NavigableParentView` components.

## File-Level Change Plan
- `ui/src/main/java/com/azarenka/evebuilders/main/menu/MenuManagerPage.java`
  - Add the new tab registration and locale label refresh.
- `ui/src/main/java/com/azarenka/evebuilders/main/managment/orders/...`
  - Add new page/view classes for the tab.
- `ui/src/main/resources/...` translation files
  - Add label key for new tab title.

## Acceptance Criteria
- Manager menu shows a new tab named `Managment`.
- Clicking tab opens its page without affecting existing tabs.
- Tab label is localized like other manager tabs.
- Role restrictions match the current manager menu behavior.

## Notes
- This document intentionally describes implementation only.
- No production Java code was implemented in this step.
