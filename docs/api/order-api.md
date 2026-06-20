# Order API

Contract for the Angular orders screen that replaces the old Vaadin `OrdersView`.

This API exposes the same data that the old UI rendered:
- the order list table
- the selected order metadata panel
- the distributed orders list shown in the order info dialog

## Common Rules

- Base path: `/api/orders`
- Authentication is required
- The endpoint uses the same access rule as the old screen:
  - `CONTRACTS_VIEW`
  - `CONTRACTS_EDIT`
  - `CONTRACTS_ACCEPT`
  - `CONTRACTS_CANCEL`
  - `CONTRACTS_DISCARD`
  - `CORPORATION_VIEW`
  - `CORPORATION_CONTRACT_VIEW`
  - `CORPORATION_CONTRACT_EDIT`
- Content type: `application/json`
- `superAdmin` bypass is handled by the global authorization layer

## Order List

### `GET /api/orders`

Returns the list that was shown in the main grid of `OrdersView`.

Query params:
- `statuses` - optional list of order statuses
- `orderTypes` - optional list of order types
- `minFreeCount` - optional minimum free ship count
- `distributed` - optional boolean flag
- `search` - optional text search across:
  - `orderNumber`
  - `itemName`
  - `orderStatus`

Response:

```json
[
  {
    "id": "uuid-1",
    "orderNumber": "N2026061801",
    "itemName": "Hecate",
    "count": 5,
    "inProgressCount": 5,
    "freeCount": 0,
    "countReady": 5,
    "price": 12345.67,
    "orderType": "MARKET",
    "destination": "Jita",
    "receiver": "Receiver Name",
    "priority": "High",
    "bluePrint": true,
    "orderStatus": "DISTRIBUTED",
    "createdBy": "pilot",
    "createdDate": "2026-06-12",
    "updatedBy": "pilot",
    "updatedDate": "2026-06-18",
    "fitId": "fit-1",
    "orderRights": "CORPORATION",
    "rightsholder": "holder",
    "category": "Cruiser",
    "finishDate": "2026-06-20",
    "distributionStatus": "FULL",
    "daysToFinish": 2,
    "progressPercent": 100
  }
]
```

Field notes:
- `itemName` is the same value the old UI showed in the grid
- `freeCount` is `count - inProgressCount`
- `distributionStatus` is derived from the old metadata panel:
  - `NO` when `inProgressCount == 0`
  - `PARTIAL` when `inProgressCount > 0 && count > inProgressCount`
  - `FULL` when `count == inProgressCount`
- `progressPercent` is the same completion percentage the old panel displayed
- `daysToFinish` is calculated from `finishDate - current date`

Errors:
- `401 Unauthorized`
- `403 Forbidden`

## Selected Order

### `GET /api/orders/{orderNumber}`

Returns the same payload shape as `GET /api/orders`, but for one order only.

Path params:
- `orderNumber` - order number, for example `N2026061801`

Response:
- one `OrderViewDto`

Errors:
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found` if the order does not exist

## Distributed Orders

### `GET /api/orders/{orderNumber}/distributed-orders`

Returns the records that were shown in the old order info dialog.

Path params:
- `orderNumber` - parent order number

Response:

```json
[
  {
    "id": "uuid-1",
    "orderNumber": "N2026061801",
    "shipName": "Hecate",
    "userName": "pilot",
    "count": 3,
    "countReady": 2,
    "fitId": "fit-1",
    "orderRights": "CORPORATION",
    "orderStatus": "IN_PROGRESS",
    "createdDate": "2026-06-12",
    "appliedDate": "2026-06-18",
    "finishedDate": null,
    "category": "Cruiser",
    "price": 12345.67,
    "isAssembly": true
  }
]
```

Field notes:
- this is the data behind the old `OrderDetailsWindow`
- the Angular UI can render the grid columns directly from:
  - `orderNumber`
  - `orderStatus`
  - `shipName`
  - `count`
  - `countReady`
  - `userName`

Errors:
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found` if the parent order does not exist
