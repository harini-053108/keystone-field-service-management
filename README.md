# KEYSTONE — Field Service Management Platform

**Client:** Meridian Facilities Management · **Stack:** Spring Boot 3 (Java 21) · React + TypeScript (Vite) · PostgreSQL · Flyway

KEYSTONE is an end-to-end field-service platform: dispatchers raise and assign work orders,
technicians update them from the field, managers watch SLAs and dashboards, and customers
raise requests and track progress — with a governed work-order lifecycle and server-side
role-based security.

---

## 1. Overview

| Area            | Technology |
|-----------------|------------|
| Language        | Java 21 (records, pattern matching) |
| Back-end        | Spring Boot 3 (Web, Validation, Security, Data JPA) |
| Database        | PostgreSQL 16, schema managed by versioned **Flyway** migrations |
| ORM             | Spring Data JPA / Hibernate (`ddl-auto: validate`) |
| Auth            | Spring Security, **stateless JWT**, **BCrypt** password hashing |
| API docs        | springdoc-openapi (**Swagger UI**) |
| Front-end       | React + TypeScript + Vite (Axios, React Router) |
| Build / deploy  | Maven · Docker · docker-compose |

### Seed logins — password for **all** accounts is `Keystone@123`

| Role       | Email                  |
|------------|------------------------|
| Manager    | `manager@example.com`    |
| Dispatcher | `dispatcher@example.com` |
| Technician | `tech1@example.com`      |
| Customer   | `customer@example.com`   |

---

## 2. Quick start (Docker)

```bash
cd keystone
docker compose up --build
```

This starts Postgres, the Spring Boot backend, and the nginx-served React front end.

| Service     | URL                                  |
|-------------|--------------------------------------|
| Front-end   | http://localhost:5173                |
| Backend API | http://localhost:8080                |
| Swagger UI  | http://localhost:8080/swagger-ui.html |

Migrations and seed data run automatically via Flyway on backend startup — nothing manual needed.

---

## 3. Local development (without Docker)

### 3.1 Backend

Prerequisites: JDK 21, Maven 3.9+, a running PostgreSQL.

```bash
# create the database (adjust for your setup)
createdb keystone
psql keystone -c "CREATE USER keystone WITH PASSWORD 'keystone';"
psql keystone -c "GRANT ALL PRIVILEGES ON DATABASE keystone TO keystone;"

cd backend
export DB_URL=jdbc:postgresql://localhost:5432/keystone
export DB_USERNAME=keystone
export DB_PASSWORD=keystone
export JWT_SECRET=change-me-to-a-long-random-secret-for-production
mvn spring-boot:run
```

The app is on http://localhost:8080, Swagger on `/swagger-ui.html`.

### 3.2 Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server (http://localhost:5173) proxies `/api` to `http://localhost:8080` by default.
Override with `VITE_API_TARGET` if your backend is elsewhere.

### 3.3 Migrations & seed

All schema changes live in `backend/src/main/resources/db/migration/` as versioned Flyway scripts
(`V1__init.sql`, `V2__seed.sql`). They run automatically on startup. To run them against an empty
database manually:

```bash
cd backend
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/keystone \
                   -Dflyway.user=keystone -Dflyway.password=keystone
```

Never edit the schema by hand — add a new `V<n>__description.sql` script instead.

---

## 4. Environment variables

| Variable             | Default                                    | Purpose |
|----------------------|--------------------------------------------|---------|
| `DB_URL`             | `jdbc:postgresql://localhost:5432/keystone`| JDBC URL |
| `DB_USERNAME`        | `keystone`                                 | DB user |
| `DB_PASSWORD`        | `keystone`                                 | DB password |
| `JWT_SECRET`         | dev-only default                           | HMAC key for signing JWTs (≥32 bytes; **set in prod**) |
| `JWT_EXPIRATION_MS`  | `86400000` (24h)                           | Token lifetime |
| `SERVER_PORT`        | `8080`                                     | Backend port |
| `SLA_RISK_THRESHOLD_FRACTION` | `0.25`                          | Fraction of the SLA window that marks a job `AT_RISK` |
| `SLA_SCHEDULER_ENABLED`       | `true`                         | Enables the scheduled SLA breach scan |

---

## 5. Domain model

```
Customer 1───N Site
Customer 1───N WorkOrder
Site     1───N WorkOrder
User (technician) 1───N WorkOrder   (assignee)
WorkOrder 1───N WorkOrderStatusHistory   (append-only audit)
Part 1───N PartUsage N───1 WorkOrder
WorkOrder 1───N TimeLog
Notification ──N User
```

### Work-order lifecycle (enforced **server-side**)

```
NEW ──▶ ASSIGNED ──▶ IN_PROGRESS ──▶ COMPLETED ──▶ CLOSED
             ▲           │  ▲              │
             │           ▼  │              ▼
             │         ON_HOLD           CANCELLED
             └───────────────┘ (NEW/ASSIGNED only)
```

- **Terminal states:** `CLOSED`, `CANCELLED` — no further transitions (409 if attempted).
- **Every transition** writes a `WorkOrderStatusHistory` row (`from, to, who, when, note`).
- **Only a manager** can `CLOSE`; **only the assigned technician** can start / hold / resume / complete their own job.
- All other transitions are rejected with **409 Conflict**.

### Role-based access (enforced server-side with `@PreAuthorize` + service checks)

| Capability                            | Dispatcher | Technician | Manager | Customer |
|---------------------------------------|:---:|:---:|:---:|:---:|
| Create / edit customers & sites       | ✓ | – | ✓ | – |
| Create work orders / raise requests   | ✓ | – | ✓ | ✓ (own sites only) |
| Assign / reassign technicians         | ✓ | – | ✓ | – |
| View all work orders + board          | ✓ | – | ✓ | – |
| View own assigned jobs                | – | ✓ | – | – |
| Start / hold / resume / complete      | – | ✓ (own only) | – | – |
| Log parts & time                      | ✓ | ✓ (own only) | ✓ | – |
| Close work orders                     | – | – | ✓ | – |
| Manage users & parts                  | – | – | ✓ | – |
| View reports / dashboard              | – | – | ✓ | – |
| Customer portal (own data only)       | – | – | – | ✓ |

---

## 6. API

REST, noun-based, consistent error shape (`timestamp, status, error, message, fieldErrors`) —
never a stack trace. List endpoints are paginated/filtered/sorted. Live docs: `/swagger-ui.html`.

Key endpoints:

| Method | Endpoint                            | Purpose |
|--------|-------------------------------------|---------|
| POST   | `/api/auth/login`                   | Authenticate → JWT + role |
| GET    | `/api/auth/me`                      | Profile + (customer) sites |
| GET/POST | `/api/customers`                  | List / create customers |
| GET/PUT/DELETE | `/api/customers/{id}`       | Read / update / delete |
| GET/POST | `/api/customers/{id}/sites`     | Sites for a customer |
| GET/POST | `/api/work-orders`              | List (role-scoped) / create |
| GET/PUT | `/api/work-orders/{id}`         | Fetch (with history) / edit while open |
| GET    | `/api/work-orders/board`           | All open orders (Kanban) |
| POST   | `/api/work-orders/{id}/assign`     | Assign / reassign technician |
| POST   | `/api/work-orders/{id}/status`     | Transition status (409 on illegal) |
| POST   | `/api/work-orders/{id}/parts`      | Log parts (decrements stock atomically) |
| POST   | `/api/work-orders/{id}/time`       | Log labour time |
| GET    | `/api/parts` / POST/PUT            | Parts inventory |
| GET    | `/api/users`, `/api/users/technicians` | User management |
| GET    | `/api/reports/summary`             | Dashboard metrics (manager) |
| GET    | `/api/notifications` …             | In-app notifications |

**Status codes:** `200/201` success · `400` validation · `401` bad/missing token · `403` role denied ·
`404` not found / hidden · `409` illegal transition or stock shortfall.

---

## 7. Architecture

Layered Spring Boot behind a React SPA:

```
Browser (React SPA, JWT)
        │  /api/*
        ▼
Controllers      ─ thin: HTTP, validation, auth (maps DTOs, delegates)
        ▼
Services         ─ business rules: state machine, dispatch, SLA, transactional stock/time
        ▼
Repositories     ─ Spring Data JPA
        ▼
PostgreSQL        ─ schema owned by Flyway migrations
```

Principles honoured:

- **Thin controllers, rich services** — business decisions live in `@Transactional` services; the
  state machine is isolated in `WorkOrderStateMachine`.
- **DTOs at the boundary** — JPA entities are never serialized to clients.
- **Transactional integrity** — logging parts and decrementing stock commit in **one transaction**;
  stock can never go negative (validated before decrement).
- **Append-only audit** — `WorkOrderStatusHistory` is never edited or deleted.
- **Server-side security** — every protected endpoint re-checks role via `@PreAuthorize` and
  ownership in services. A customer can never read another customer's orders (404).
- **Migrations, not magic** — every schema change is a versioned Flyway script.

Directory layout:

```
keystone/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/meridian/keystone/
│       │   ├── controller/    # thin REST controllers + @PreAuthorize
│       │   ├── service/       # business logic + state machine + SLA
│       │   ├── repository/    # Spring Data JPA
│       │   ├── domain/        # entities + enums
│       │   ├── security/      # JWT, filter, principals, current-user
│       │   ├── dto/           # request/response records
│       │   ├── exception/     # @RestControllerAdvice error handling
│       │   └── config/        # Security, OpenAPI, Web, scheduler
│       └── resources/db/migration/   # Flyway V1..Vn
├── frontend/
│   └── src/{pages,components,api,auth,types}
├── docker-compose.yml
└── README.md
```

### Scheduling & SLA

- SLA due date = `created_at + priority window` (URGENT 8h, HIGH 24h, MEDIUM 48h, LOW 72h).
- A scheduled job (`SlaBreachScheduler`, configurable interval) flags open orders `AT_RISK` /
  `BREACHED` and raises **in-app notifications for managers** on new breaches.
- SLA health is surfaced on the board, work-order detail, and the dashboard.

---

## 8. Testing

Lifecycle and SLA logic are covered by unit tests in
`backend/src/test/java/com/meridian/keystone/service/WorkOrderStateMachineTest.java`:

```bash
cd backend
mvn test
```

---

## 9. Architecture & acceptance mapping

| Brief requirement | Where it lives |
|---|---|
| JWT login + 4-role RBAC | `security/`, `config/SecurityConfig`, `@PreAuthorize` on controllers |
| Customer & Site CRUD | `CustomerController/Service`, `SiteController/Service` |
| Work-order CRUD + unique code | `WorkOrderService` (code `WO-####`, immutable when terminal) |
| Dispatch / assignment / board | `POST /assign`, `GET /work-orders/board`, `Board.tsx` |
| Technician field view | `MyJobs.tsx`, role-scoped list, start/hold/complete/log |
| Parts & time logging | `PartUsageService` (atomic stock decrement), `TimeLogService` |
| SLA tracking & notifications | `SlaService`, `SlaBreachScheduler`, `NotificationService` |
| Manager dashboard | `ReportController/Service`, `Dashboard.tsx` |
| Customer portal | `Portal.tsx` (own sites + own orders only) |
| Swagger/OpenAPI | springdoc on `/swagger-ui.html` |
| Deploy | `docker-compose.yml` |