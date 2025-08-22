# Application for Industrial Management in EVE Online

* This application is designed to support industrial operations within the EVE Online universe. It provides tools for managing corporation production orders, specifically tailored for the Hold My Probs alliance. The application enables streamlined tracking, assignment, and fulfillment of manufacturing requests, improving efficiency and coordination across the corporation. While currently configured for Hold My Probs, the system is modular and can be adapted for use by other alliances or corporations as needed.

# Release notes'

## Version 1.6.3

### Fixes
- Move CorporationContraction tab to order tab
- Fix Color on UI dor dark theme
- Add sorting for requests tab
- Fix calculation issue for last components
- Change role distributing 

## Version 1.6.2 

### Fixes
- Fixed **FitOff display**.
- Fixed **FitOff issue in the error window** — buttons and their labels now display correctly.
- Fixed **quantity display position** in the *Distribution* section on the **Orders** tab — now the **order number** is shown instead, with the ability to copy it.
- Fixed an issue where all items were incorrectly added to the **middle panel** in the *Assembly* tab — this error no longer occurs.
- Fixed **trash calculations** in the calculator when one of the items was excluded from the calculation.

### Improvements
- Added **dark theme** for the application.
- Added functionality for **coordinators to delete fits**.

## Version 1.6.1

### UI Improvements
- Implement metadata panel in distribution orders
- Add ability to remove fits for Coordinator role
- Add ability to watch fits for admin in Requests tab

## Version 1.6.0.4

### HOT FIX
- Fix issue related to stage calculation

## Version 1.6.0.3

### HOT FIX
- Fix issue related to send message during crating order 

## Version 1.6.0.2

### HOT FIX
- Reimplement calculation formula

## Version 1.6.0.1 

### HOT FIX
- Fix loading indicator
- Fix Info application window

## Version 1.6.0 – Enhancements & Fixes

### UI/UX Improvements
- Added new **loading indicator** and implemented **screen lock during loading**.
- Redesigned the **option selection window** for each blueprint.
- Added **resize support** and **close button** to modal headers.
- Added **calculation window for each stage**, showing components, stock, deficit, and total cost in Jita.

### Core Logic Updates
- Fully **reworked calculation logic** for both general flow and the **list view**.
- Improved logic for **adding characters with main association**.
- Blueprint **properties setup** implemented.
- Enhanced **Assembly tab rendering logic**.

### Backend & Integration
- Fully redesigned the **ESI asset retrieval service**.
- Implemented **database + service** to fetch asset data either from ESI or from cache.
- Implemented a service to **fetch item prices from Jita via ESI**.

### Bug Fixes
- Fixed various issues in the **Assembly tab**.
- Filters now **auto-apply saved selections** on page load.
- Minor fixes for **InfoLocation**.
- Fixed a **coordinator bug** ("PerfectionBug") where pressing *Approve* in the context menu incorrectly created a new order.


## Version 1.5.4
Release Date: 2025-07-18

## UI and UX Improvements
- Reimplement filter logic
- add functionality to save personal filter

## Version 1.5.3
Release Date: 2025-07-14

## Request Tab Enhancements
- Improved **visual layout and usability** of the Request tab.
- Added **context menu** (right-click on grid row) that mirrors all available action buttons.
- Implemented **ship fit display** directly in the Request tab for better clarity.

## UI Improvements
- Added **badge indicator** on the main menu tab `Orders` showing the number of orders with `NEW` status.
- Introduced new **"Available Quantity" column** to display the number of unassigned ships.

## Version 1.5.2
Release Date: 2025-07-13

## Contract Submission Fixes
- Fixed an issue causing **duplicate contract submissions** when users repeatedly clicked the "Submit Contract" button.
- Resolved logic issues preventing proper contract validation on submission.

## Request Management Enhancements
- Added **"Force Close Request"** button — available even if validation failed.
- Added **"Close Request Without Contract"** button — useful when no contract was created.
- Fully refactored **request creation logic**:
  - Improved stability and behavior.
  - Updated input columns in the request creation form.

## UI and UX Improvements
- Fixed incorrect **price display** and adjusted font sizes for better readability.
- Corrected filter behavior in **Orders** and **Production** tabs — status filtering now works as expected.
- Added **sorting by creation date** to order-related tables.

## Version 1.5.1
Release Date: 2025-07-10

### HOT FIX
- Fix issue related to disability to assembly order with wrong name in fit modules validation order window and permission to check contracts
- 
## Version 1.5.0
Release Date: 2025-07-09

## Persistent Login
- Implemented cookie.
  - Users no longer need to log in repeatedly — session cookies now persist for **7 days**.

## Contract Validation
- Added automatic **contract validator**.
  - When users submit contracts, the system now validates them automatically before acceptance.

## Database Migrations
- Performed internal migration of several database columns to ensure correct data types and improved consistency.

## Request System Improvements
- Reworked the **request submission flow** for coordinators:
  - Improved stability and fixed issues related to request creation.
- Updated EVE mail notification logic:
  - Coordinators now receive **in-game mail** when their request has been accepted.

## Version 1.4.1.1
Release Date: 2025-06-24

### HOT FIX
- Fix issue related to disability to assembly order with wrong name in fit modules 

## Version 1.4.1
Release Date: 2025-06-24

### General
- Increase performance during loading a lot of images to UI and calculating materials for order with adding cache services for calculation items and for load images
- Improve UI for Assembly tab

## Version 1.4.0
Release Date: 2025-06-20

### ## Authorization System Migration
- Fully migrated authorization system from legacy HTTP-based authentication to **OAuth 2.0 Authorization Server (OAS 2.0)** standard.


## Version 1.3.3
Release Date: 2025-06-20

### Bug Fixes
- fix translation for metadapanel
- fix auth issue related to permissions

## Version 1.3.2
Release Date: 2025-06-19

### Metadata panel Improvements
- Organize information on metadata panel

## Version 1.3.1
Release Date: 2025-06-18

### Access Control Improvements
- Added informational message for unauthorized users without sufficient roles, including instructions whom to contact for access.

### Concurrency Fixes
- Fixed a race condition issue where users could simultaneously take an order, leading to incorrect number of assigned items.

### EVE Mail Integration
- Added integration for sending in-game mails via EVE Online API.
  - Currently used for coordinators: when their request is processed, a notification is automatically sent to their EVE Online mail.

### Administrator Features
- Added "Process Request" button for administrators.
- Administrators can now directly create orders based on submitted requests.

## Version 1.3.0
Release Date: 2025-06-17

### Roles and Permissions
- Introduced the `ROLE_COORDINATOR` role.
    - Coordinators can create and view their own order requests.
- Users with `ROLE_ADMIN` or `ROLE_SUPER_ADMIN` can:
    - View all submitted requests.
    - Create full orders based on submitted requests.
### Order Workflow
- Added logic for **processing submitted requests** by administrators.
- Implemented order creation flow based on coordinator-submitted requests.

### Request Center
- Added a new menu section: **Request Center**.
- The section contains three tabs:
    - **Create Request** – available for users with the `ROLE_COORDINATOR` role.
    - **My Requests** – available for users with the `ROLE_COORDINATOR` role.
    - **Requests** – available for users with `ROLE_ADMIN` and `ROLE_SUPER_ADMIN` roles.

## Version 1.2.0
Release Date: 2025-06-14

### Header
- Added an info button that displays application developer information.
- Includes links to the Git repository and donation page.
### Order Management
- Added logic allowing staff to cancel an accepted order, but only within the first half of the time between the order's publication and its due date.
- Added a warning message when accepting an order if less than half of the time between publication and due date remains.
### Bug Fixes
- Fixed a bug where the notification about staff taking an order was sent to the wrong Telegram channel.
- Fixed a bug when after removed order did not update the UI

## Version 1.1.0 
Release Date: 2025-06-13

### Orders Tab
- Add **order delivery date** field.
### "Take Order" Window
- Add **number of available ships** indicator.
### Assembly Tab
- Added **fit preview** during order assembly.
- Fixed **material calculation** when changing material efficiency or number of root components.
### General
- Fixed **translation issues** in the UI.