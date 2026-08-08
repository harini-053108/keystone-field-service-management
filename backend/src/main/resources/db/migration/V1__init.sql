-- KEYSTONE schema (v1)
-- Layered: users, customers, sites, work_orders, status history (append-only), parts, part_usage, time_logs, notifications.

CREATE TABLE customers (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255),
    phone      VARCHAR(64),
    address    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE sites (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    name        VARCHAR(255) NOT NULL,
    address     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sites_customer ON sites(customer_id);

CREATE TABLE parts (
    id         BIGSERIAL PRIMARY KEY,
    code       VARCHAR(64)  NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    unit_cost  NUMERIC(12,2) NOT NULL DEFAULT 0,
    stock_qty  INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,        -- DISPATCHER, TECHNICIAN, MANAGER, CUSTOMER
    customer_id   BIGINT REFERENCES customers(id), -- links a CUSTOMER-role user to their tenant
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_customer ON users(customer_id);

CREATE TABLE work_orders (
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(32)  NOT NULL UNIQUE,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    priority      VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    sla_status    VARCHAR(20)  NOT NULL DEFAULT 'ON_TRACK',
    sla_due_at    TIMESTAMPTZ,
    customer_id   BIGINT NOT NULL REFERENCES customers(id),
    site_id       BIGINT NOT NULL REFERENCES sites(id),
    assignee_id   BIGINT REFERENCES users(id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at  TIMESTAMPTZ
);
CREATE INDEX idx_wo_customer ON work_orders(customer_id);
CREATE INDEX idx_wo_site     ON work_orders(site_id);
CREATE INDEX idx_wo_assignee ON work_orders(assignee_id);
CREATE INDEX idx_wo_status   ON work_orders(status);

CREATE TABLE work_order_status_history (
    id             BIGSERIAL PRIMARY KEY,
    work_order_id  BIGINT NOT NULL REFERENCES work_orders(id),
    from_status    VARCHAR(20),
    to_status      VARCHAR(20) NOT NULL,
    changed_by_id  BIGINT REFERENCES users(id),
    note           TEXT,
    changed_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_woh_wo ON work_order_status_history(work_order_id);

CREATE TABLE part_usage (
    id            BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL REFERENCES work_orders(id),
    part_id       BIGINT NOT NULL REFERENCES parts(id),
    quantity      INT NOT NULL,
    unit_cost     NUMERIC(12,2) NOT NULL,
    note          TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pu_wo ON part_usage(work_order_id);

CREATE TABLE time_logs (
    id            BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL REFERENCES work_orders(id),
    minutes       INT NOT NULL,
    note          TEXT,
    logged_by_id  BIGINT NOT NULL REFERENCES users(id),
    logged_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tl_wo ON time_logs(work_order_id);

CREATE TABLE notifications (
    id            BIGSERIAL PRIMARY KEY,
    recipient_id  BIGINT NOT NULL REFERENCES users(id),
    message       VARCHAR(1000) NOT NULL,
    type          VARCHAR(40)  NOT NULL,
    work_order_id BIGINT REFERENCES work_orders(id),
    is_read       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notif_recipient ON notifications(recipient_id);