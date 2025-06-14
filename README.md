# Application for Industrial Management in EVE Online

* This application is designed to support industrial operations within the EVE Online universe. It provides tools for managing corporation production orders, specifically tailored for the Hold My Probs alliance. The application enables streamlined tracking, assignment, and fulfillment of manufacturing requests, improving efficiency and coordination across the corporation. While currently configured for Hold My Probs, the system is modular and can be adapted for use by other alliances or corporations as needed.

# Release notes

## Version 1.2.0
Release Date: 2025-06-14

## Header
- Added an info button that displays application developer information.
- Includes links to the Git repository and donation page.
## Order Management
- Added logic allowing staff to cancel an accepted order, but only within the first half of the time between the order's publication and its due date.
- Added a warning message when accepting an order if less than half of the time between publication and due date remains.
## Bug Fixes
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