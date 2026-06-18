# Admin API

Contract for the access-control admin UI.

This API runs on top of the existing EVE Online SSO flow:
- `login` via EVE SSO does not change
- `auth/profile` and `auth/me` are used only to fetch the current user
- `SUPER_ADMIN` bypasses all permission checks

## Common Rules

- Base path: `/api`
- Authentication is required for all endpoints except `GET /api/ping`
- Content type: `application/json`
- Errors:
  - `401 Unauthorized` - unauthenticated
  - `403 Forbidden` - missing permission
  - `404 Not Found` - entity not found
  - `400 Bad Request` - invalid payload
  - `409 Conflict` - unique constraint conflict, for example when a role code already exists

## Current User Profile

### `GET /api/auth/profile`

Returns the current authenticated user.

`GET /api/auth/me` is an alias with the same response.

Required:
- authenticated user

Response:

```json
{
  "userId": "123",
  "eveCharacterId": "987654321",
  "characterName": "Some Pilot",
  "corporationId": "456",
  "corporationName": "Corp Name",
  "roles": ["MANAGER", "MINER"],
  "permissions": ["CONTRACTS_VIEW", "DASHBOARD_VIEW"],
  "superAdmin": false
}
```

For `SUPER_ADMIN`:

```json
{
  "userId": "1",
  "eveCharacterId": "123456789",
  "characterName": "Admin Pilot",
  "corporationId": "456",
  "corporationName": "Corp Name",
  "roles": ["SUPER_ADMIN"],
  "permissions": [],
  "superAdmin": true
}
```

Notes:
- `corporationId` can be `null` if it is not available in character info
- `permissions` for `SUPER_ADMIN` are empty because bypass is represented by `superAdmin: true`

## Admin API

### Roles

#### `GET /api/admin/roles`

Required permission:
- `ROLES_VIEW`

Response:

```json
[
  {
    "id": 1,
    "code": "CEO",
    "name": "CEO",
    "description": "Executive role",
    "systemRole": true,
    "permissions": [
      {
        "id": 10,
        "code": "CORPORATION_VIEW",
        "name": "View corporation",
        "description": "Read corporation data",
        "groupName": "CORPORATION"
      }
    ]
  }
]
```

Errors:
- `401`
- `403`

#### `GET /api/admin/roles/{code}`

Required permission:
- `ROLES_VIEW`

Path params:
- `code` - role code, for example `CEO`

Response:

```json
{
  "id": 1,
  "code": "CEO",
  "name": "CEO",
  "description": "Executive role",
  "systemRole": true,
  "permissions": []
}
```

Errors:
- `401`
- `403`
- `404`

#### `POST /api/admin/roles`

Required permission:
- `ROLES_CREATE`

Request body:

```json
{
  "code": "QA",
  "name": "QA",
  "description": "Quality assurance role"
}
```

Response:
- created `RoleDto`

Errors:
- `400`
- `401`
- `403`
- `409` if role code already exists

#### `PUT /api/admin/roles/{code}`

Required permission:
- `ROLES_EDIT`

Request body:

```json
{
  "name": "Updated name",
  "description": "Updated description"
}
```

Response:
- updated `RoleDto`

Errors:
- `400`
- `401`
- `403`
- `404`

#### `DELETE /api/admin/roles/{code}`

Required permission:
- `ROLES_DELETE`

Response:
- `204 No Content`

Rules:
- system roles cannot be deleted

Errors:
- `401`
- `403`
- `404`
- `409` or `400` if delete is blocked for a system role

### Permissions

#### `GET /api/admin/permissions`

Required permission:
- `PERMISSIONS_VIEW`

Response:

```json
[
  {
    "id": 10,
    "code": "DASHBOARD_VIEW",
    "name": "View dashboard",
    "description": "Access dashboard overview",
    "groupName": "DASHBOARD"
  }
]
```

Notes:
- permissions are read-only in the API

Errors:
- `401`
- `403`

### Role Permissions

#### `GET /api/admin/roles/{code}/permissions`

Required permission:
- `ROLES_VIEW`

Response:

```json
[
  {
    "id": 10,
    "code": "CONTRACTS_VIEW",
    "name": "View contracts",
    "description": "Read contract data",
    "groupName": "CONTRACTS"
  }
]
```

Errors:
- `401`
- `403`
- `404`

#### `PUT /api/admin/roles/{code}/permissions`

Required permission:
- `PERMISSIONS_ASSIGN`

Request body:

```json
{
  "permissionCodes": ["CONTRACTS_VIEW", "CONTRACTS_EDIT", "CORPORATION_VIEW"]
}
```

Behavior:
- replaces current role permissions with the provided set
- duplicate codes are ignored by the backend

Response:
- updated set of `PermissionDto`

Errors:
- `400`
- `401`
- `403`
- `404` role not found
- `404` permission not found

### User Access

#### `GET /api/admin/users/{userId}/access`

Required permission:
- `USERS_VIEW`

Response:

```json
{
  "userId": "123",
  "username": "Some Pilot",
  "roles": [
    {
      "id": 1,
      "code": "MANAGER",
      "name": "Manager",
      "description": "Management role",
      "systemRole": true,
      "permissions": []
    }
  ],
  "directPermissions": [
    {
      "id": 10,
      "code": "CONTRACTS_VIEW",
      "name": "View contracts",
      "description": "Read contract data",
      "groupName": "CONTRACTS"
    }
  ],
  "finalPermissions": [
    {
      "id": 11,
      "code": "DASHBOARD_VIEW",
      "name": "View dashboard",
      "description": "Access dashboard overview",
      "groupName": "DASHBOARD"
    }
  ],
  "superAdmin": false
}
```

Notes:
- `finalPermissions` = permissions from roles + direct permissions
- `finalPermissions` is empty for `SUPER_ADMIN` because bypass is represented by `superAdmin: true`

Errors:
- `401`
- `403`
- `404`

#### `PUT /api/admin/users/{userId}/roles`

Required permission:
- `ROLES_ASSIGN`

Request body:

```json
{
  "roleCodes": ["MANAGER", "MINER"]
}
```

Behavior:
- replaces current user roles with the provided set
- duplicate codes are ignored by the backend

Response:
- updated `UserAccessDto`

Errors:
- `400`
- `401`
- `403`
- `404` user not found
- `404` role not found

#### `PUT /api/admin/users/{userId}/direct-permissions`

Required permission:
- `PERMISSIONS_ASSIGN`

Request body:

```json
{
  "permissionCodes": ["CONTRACTS_VIEW", "DASHBOARD_VIEW"]
}
```

Behavior:
- replaces current user direct permissions with the provided set
- duplicate codes are ignored by the backend

Response:
- updated `UserAccessDto`

Errors:
- `400`
- `401`
- `403`
- `404` user not found
- `404` permission not found

## Permission Matrix

- view roles: `ROLES_VIEW`
- create role: `ROLES_CREATE`
- edit role: `ROLES_EDIT`
- delete role: `ROLES_DELETE`
- assign role permissions: `PERMISSIONS_ASSIGN`
- view permissions: `PERMISSIONS_VIEW`
- view user access: `USERS_VIEW`
- edit user roles: `ROLES_ASSIGN`
- edit direct user permissions: `PERMISSIONS_ASSIGN`

## Frontend Usage

Recommended flow for Angular UI:

1. After SSO login and refresh, call `GET /api/auth/profile`
2. Use returned `roles`, `permissions`, and `superAdmin` to render menus and admin routes
3. Use `/api/admin/*` endpoints for the access-control admin panel
4. Keep `GET /api/auth/me` only as a backward-compatible alias
