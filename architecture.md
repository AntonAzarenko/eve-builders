# eve-builders Architecture

## Overview
`eve-builders` is a modular Java 17/21 Spring Boot application for industrial workflow management in EVE Online.
Core runtime model:
- Server-side UI on Vaadin (`ui` module).
- Business logic and integrations in `service`.
- Persistence adapters in `repository`.
- Shared entities/DTO/enums in `domain`.
- Database migrations in `db` (Liquibase).

## Module Structure
- `domain`: JPA entities (`domain.db`, `domain.casino`), SQLite models (`domain.sqllite`), DTOs and enums.
- `repository`: Spring Data repositories for:
  - PostgreSQL/main data (`repository.database`)
  - MariaDB auth/group data (`repository.auth`)
  - SQLite static EVE data (`repository.litesql`)
- `service`: use-case/business services, external integrations (EVE ESI, Telegram, Timrod), auth/token management, scheduled jobs.
- `ui`: Spring Boot entrypoint, Vaadin routes/layouts/components, Spring Security config, REST API controllers.
- `db`: Liquibase changelogs (`db.changelog-master.yaml` + versioned changesets).

Dependency direction is mostly one-way:
`ui -> service -> repository -> domain` (+ `ui` uses `domain`/`db` resources directly).

## Runtime Layers
1. Presentation layer
- Vaadin routes/views (`/orders`, `/manager`, `/construction`, `/trade`, `/request-center`, etc.).
- Small REST surface:
  - `/api/orders*` for order intake/validation (Telegram flow).
  - `/api/v1/casino/**` for casino subsystem.

2. Application/Domain layer
- Services orchestrate workflows (orders, requests, contracts, asset sync, market, statistics, casino).
- Domain objects and DTOs are shared between UI/REST and persistence.

3. Data layer
- Spring Data JPA repositories + specifications.
- Separate datasource/entity-manager/transaction-manager per storage.

## Data Sources
The app is configured with 3 data stores:
- PostgreSQL (primary transactional storage: orders, users, requests, market, casino state).
- MariaDB (alliance/auth-group related storage via `repository.auth`).
- SQLite (EVE static reference data: item types, groups, dogma, icons).

Liquibase changelogs are packaged in `db` and applied from the UI app depending on profile/config.

## Security and Access Model
- Spring Security + VaadinWebSecurity.
- OAuth2 login against EVE Online (`eveonline` client registration).
- Post-login user provisioning/role mapping in `EveOAuth2UserService`.
- Persistent session behavior via `UID` cookie + `CookieAuthFilter` (non-`/api/**` requests).
- Role-based route access (`@RolesAllowed`) for major UI sections.
- Dedicated API token filter (`X-API-TOKEN`) for `/api/v1/casino/**`.

## Key Business Flows
1. Order management
- Admin creates orders -> `OrderService` persists order, writes audit, sends operational notifications (Telegram and/or Discord via notification router).
- Staff takes/distributes/completes orders via Vaadin views + service layer.

2. Request center
- Coordinators create requests.
- Admin processes requests into production orders.
- Status changes trigger EVE mail notifications.

3. Assembly and material calculation
- Static EVE datasets loaded from CSV (`StaticMaterialLoader`).
- `ProductionTreeService` builds recursive production trees with in-memory cache.
- `AssetService` merges user/alts assets; uses ESI + DB cache with ETag strategy.

4. Casino API
- Separate REST endpoints and services for rewards/boxes/users/history.
- Token-protected by custom API filter.

## Integrations and Background Jobs
- EVE ESI integrations via `WebClient`-based services (`service.impl.intergarion`).
- Telegram integration for operational notifications.
- Discord integration for operational notifications via webhook cards (`embeds`):
  - payload is built in `DiscordWebhookPayloadBuilder`.
  - webhook `content` starts with `@everyone` on a dedicated first line, followed by a short summary line.
  - payload includes `allowed_mentions.parse=["everyone"]` to enable channel-wide mention delivery.
- Timrod group membership validation integration.
- Scheduled jobs (`@EnableScheduling`), e.g. weekly user-group validation and periodic fleet info sync.

## Deployment
- Build: Maven multi-module project.
- Production profile builds Vaadin frontend via `vaadin-maven-plugin`.
- Packaging/runtime: executable Spring Boot JAR (containerized in Docker).
- CI example: `Jenkinsfile` builds JAR, builds Docker image, runs container on port `9191`.
