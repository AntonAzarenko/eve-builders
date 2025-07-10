# Application for Industrial Management in EVE Online

* This application is designed to support industrial operations within the EVE Online universe. It provides tools for managing corporation production orders, specifically tailored for the Hold My Probs alliance. The application enables streamlined tracking, assignment, and fulfillment of manufacturing requests, improving efficiency and coordination across the corporation. While currently configured for Hold My Probs, the system is modular and can be adapted for use by other alliances or corporations as needed.

# Release notes

## Version 1.5.1
Release Date: 2025-07-10

### HOT FIX
- Fix issue related validation order window and permission to check contracts
  
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
