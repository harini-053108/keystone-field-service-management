-- KEYSTONE seed data (v2)
-- Demo users: one login per role. Password for ALL seed accounts: Keystone@123
INSERT INTO customers (id, name, email, phone, address) VALUES
    (1, 'Northwind Office Towers', 'facilities@northwind.example', '+1 555-0101', '100 Northwind Ave, Metropolis'),
    (2, 'Bluebird Medical Center', 'ops@bluebird.example', '+1 555-0102', '88 Bluebird Lane, Westbrook');

INSERT INTO sites (id, customer_id, name, address) VALUES
    (1, 1, 'Tower A', '100 Northwind Plaza, Metropolis'),
    (2, 1, 'Tower B', '102 Northwind Plaza, Metropolis'),
    (3, 2, 'Cloud Campus', '20 Bluebird Lane, Westbrook');

INSERT INTO users (id, email, password_hash, full_name, role, customer_id) VALUES
    (1, 'manager@example.com',    '$2a$10$PSY4tmFBqu8C.cB879n6b.UauEqS1SkZ5W4iOXegmJ2FZiJau.hHq', 'Maya Rahman', 'MANAGER', NULL),
    (2, 'dispatcher@example.com', '$2a$10$PSY4tmFBqu8C.cB879n6b.UauEqS1SkZ5W4iOXegmJ2FZiJau.hHq', 'Dev Patel',   'DISPATCHER', NULL),
    (3, 'tech1@example.com',      '$2a$10$PSY4tmFBqu8C.cB879n6b.UauEqS1SkZ5W4iOXegmJ2FZiJau.hHq', 'Tom Okafor', 'TECHNICIAN', NULL),
    (4, 'tech2@example.com',      '$2a$10$PSY4tmFBqu8C.cB879n6b.UauEqS1SkZ5W4iOXegmJ2FZiJau.hHq', 'Lena Cruz',  'TECHNICIAN', NULL),
    (5, 'customer@example.com',   '$2a$10$PSY4tmFBqu8C.cB879n6b.UauEqS1SkZ5W4iOXegmJ2FZiJau.hHq', 'Sana Ali',   'CUSTOMER', 1);

INSERT INTO parts (id, code, name, unit_cost, stock_qty) VALUES
    (1, 'FILT-001', 'Air filter 24x24',  18.50, 200),
    (2, 'HVAC-FAN', 'HVAC blower fan',   145.00, 25),
    (3, 'PMP-001',  'Circulating pump',  220.00, 10),
    (4, 'BELT-12',  'Drive belt A12',    12.00, 300),
    (5, 'BRK-20',   'Circuit breaker 20A', 9.75, 120),
    (6, 'VALVE-24', 'Ball valve 2in',    34.00, 60);

INSERT INTO work_orders (id, code, title, description, priority, status, sla_status, sla_due_at, customer_id, site_id, assignee_id, created_at, updated_at) VALUES
    (1, 'WO-0001', 'AC compressor not working', 'Tenant reports warm air on floor 3.', 'URGENT', 'IN_PROGRESS', 'AT_RISK',   now() + interval '3 hours', 1, 1, 3, now() - interval '2 days', now()),
    (2, 'WO-0002', 'Washroom plumbing leak',    'Slow leak under sink, main washroom.', 'HIGH',   'ASSIGNED',   'ON_TRACK', now() + interval '12 hours', 1, 1, 4, now() - interval '1 day', now()),
    (3, 'WO-0003', 'Floor 5 lighting out',       'Emergency lights not working.',       'URGENT', 'ASSIGNED',   'BREACHED', now() - interval '1 hour', 2, 3, 4, now() - interval '2 days', now()),
    (4, 'WO-0004', 'Quarterly HVAC tune-up',     'Preventive maintenance Tower B.',    'MEDIUM', 'ON_HOLD',    'ON_TRACK', now() + interval '3 days', 1, 2, 3, now() - interval '4 days', now()),
    (5, 'WO-0005', 'Lobby thermostat swap',      'Replace faulty lobby thermostat.',   'LOW',    'COMPLETED',  'ON_TRACK', now() + interval '1 day', 2, 3, 4, now() - interval '6 days', now()),
    (6, 'WO-0006', 'Annual generator service',   'Routine inspection, awaiting sign-off.','MEDIUM','CLOSED',   'ON_TRACK', now() - interval '2 days', 1, 1, 3, now() - interval '20 days', now() - interval '3 days');

INSERT INTO work_order_status_history (work_order_id, from_status, to_status, changed_by_id, note, changed_at) VALUES
    (1, 'NEW', 'ASSIGNED', 2, 'Assigned to Tom',  now() - interval '2 days'),
    (1, 'ASSIGNED', 'IN_PROGRESS', 3, 'On site',  now() - interval '5 hours'),
    (2, 'NEW', 'ASSIGNED', 2, 'Assigned to Lena', now() - interval '1 day'),
    (3, 'NEW', 'ASSIGNED', 2, NULL,               now() - interval '1 day'),
    (4, 'NEW', 'ASSIGNED', 2, 'Assigned to Tom',  now() - interval '4 days'),
    (4, 'ASSIGNED', 'IN_PROGRESS', 3, NULL,       now() - interval '2 days'),
    (4, 'IN_PROGRESS', 'ON_HOLD', 3, 'Waiting on parts', now() - interval '1 day'),
    (5, 'NEW', 'ASSIGNED', 2, NULL,               now() - interval '6 days'),
    (5, 'ASSIGNED', 'IN_PROGRESS', 4, NULL,       now() - interval '5 days'),
    (5, 'IN_PROGRESS', 'COMPLETED', 4, 'Thermostat replaced', now() - interval '4 days'),
    (6, 'NEW', 'ASSIGNED', 2, NULL,               now() - interval '20 days'),
    (6, 'ASSIGNED', 'IN_PROGRESS', 3, NULL,       now() - interval '18 days'),
    (6, 'IN_PROGRESS', 'COMPLETED', 3, NULL,      now() - interval '15 days'),
    (6, 'COMPLETED', 'CLOSED', 1, 'Signed off',   now() - interval '3 days');

INSERT INTO part_usage (work_order_id, part_id, quantity, unit_cost, note, created_at) VALUES
    (1, 1, 2, 18.50, 'Replaced filters', now() - interval '1 day'),
    (4, 6, 1, 34.00, 'Valve fitted',     now() - interval '2 days'),
    (5, 4, 1, 12.00, 'Belt fitted',      now() - interval '4 days');

INSERT INTO time_logs (work_order_id, minutes, note, logged_by_id, logged_at) VALUES
    (1, 90, 'Diagnosed compressor', 3, now() - interval '5 hours'),
    (1, 60, 'Replaced filters',     3, now() - interval '4 hours'),
    (4, 45, 'Pre-inspection',       3, now() - interval '2 days'),
    (5, 30, 'Swap complete',        4, now() - interval '4 days');

SELECT setval('customers_id_seq', 2, true);
SELECT setval('sites_id_seq', 3, true);
SELECT setval('users_id_seq', 5, true);
SELECT setval('parts_id_seq', 6, true);
SELECT setval('work_orders_id_seq', 6, true);
SELECT setval('work_order_status_history_id_seq', 14, true);
SELECT setval('part_usage_id_seq', 3, true);
SELECT setval('time_logs_id_seq', 4, true);