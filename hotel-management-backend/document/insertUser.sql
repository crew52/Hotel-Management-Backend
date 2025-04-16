USE hotel_management_db;

INSERT INTO hotel_management_db.permissions (id, created_at, deleted, updated_at, name) VALUES (1, '2025-04-14 11:05:16.000000', false, '2025-04-14 11:05:16.000000', 'USER_READ');
INSERT INTO hotel_management_db.permissions (id, created_at, deleted, updated_at, name) VALUES (2, '2025-04-14 11:05:16.000000', false, '2025-04-14 11:05:16.000000', 'USER_WRITE');
INSERT INTO hotel_management_db.permissions (id, created_at, deleted, updated_at, name) VALUES (3, '2025-04-14 11:05:16.000000', false, '2025-04-14 11:05:16.000000', 'ROOM_READ');
INSERT INTO hotel_management_db.permissions (id, created_at, deleted, updated_at, name) VALUES (4, '2025-04-14 11:05:16.000000', false, '2025-04-14 11:05:16.000000', 'ROOM_WRITE');
INSERT INTO hotel_management_db.permissions (id, created_at, deleted, updated_at, name) VALUES (5, '2025-04-14 11:05:16.000000', false, '2025-04-14 11:05:16.000000', 'ADMIN_ACCESS');

INSERT INTO hotel_management_db.roles (id, created_at, deleted, updated_at, name) VALUES (1, '2025-04-14 11:05:16.000000', false, '2025-04-14 11:05:16.000000', 'ROLE_ADMIN');
INSERT INTO hotel_management_db.roles (id, created_at, deleted, updated_at, name) VALUES (2, '2025-04-14 11:05:16.000000', false, '2025-04-14 11:05:16.000000', 'ROLE_USER');


INSERT INTO hotel_management_db.role_permissions (role_id, permission_id) VALUES (1, 1);
INSERT INTO hotel_management_db.role_permissions (role_id, permission_id) VALUES (1, 2);
INSERT INTO hotel_management_db.role_permissions (role_id, permission_id) VALUES (1, 3);
INSERT INTO hotel_management_db.role_permissions (role_id, permission_id) VALUES (2, 3);
INSERT INTO hotel_management_db.role_permissions (role_id, permission_id) VALUES (1, 4);
INSERT INTO hotel_management_db.role_permissions (role_id, permission_id) VALUES (1, 5);

INSERT INTO hotel_management_db.users (id, created_at, deleted, updated_at, email, is_locked, password_hash, username) VALUES (1, '2025-04-14 11:36:30.606912', false, '2025-04-14 11:36:30.606912', 'test1@example.com', false, '$2a$10$YNI3aYl./6UoxPdvVXR1au8KxwpAa2NiZ0AmkaZ.QZGL0ZSUf/gfq', 'user_test_01');
INSERT INTO hotel_management_db.users (id, created_at, deleted, updated_at, email, is_locked, password_hash, username) VALUES (2, '2025-04-14 11:42:47.733942', false, '2025-04-14 11:42:47.733942', 'test02@example.com', false, '$2a$10$3VOHWFfku3bvyLDWbn4BwuZ/e3KEMqIdTo40fuhSbtivslnkQqeK2', 'user_test_02');
INSERT INTO hotel_management_db.users (id, created_at, deleted, updated_at, email, is_locked, password_hash, username) VALUES (3, '2025-04-14 14:55:42.211232', false, '2025-04-15 22:16:01.093426', 'admin@gmail.com', false, '$2a$10$Ll3hzSGNXjhjBC4/epoz6ezJ8cezE8fTdd9HDvoedAFW6rGSprMru', 'admin');
INSERT INTO hotel_management_db.users (id, created_at, deleted, updated_at, email, is_locked, password_hash, username) VALUES (4, '2025-04-14 15:13:28.891998', false, '2025-04-14 15:13:28.891998', 'employee@gmail.com', false, '$2a$10$CW.8IwmMo8oRuRdOoiOJ9.A8yEcINJGyjpg8.VoawgV1Udxg0u3rK', 'employee');
INSERT INTO hotel_management_db.users (id, created_at, deleted, updated_at, email, is_locked, password_hash, username) VALUES (5, '2025-04-16 08:41:57.153982', false, '2025-04-16 08:41:57.153982', 'employe1e@gmail.com', false, '$2a$10$NEJlxj4XV96Zj7Lg4glLuuT8GcaERVCTWoCuvlmrsAm82H27.n6hq', 'employee1');


INSERT INTO hotel_management_db.user_role (user_id, role_id) VALUES (2, 1);
INSERT INTO hotel_management_db.user_role (user_id, role_id) VALUES (3, 1);
INSERT INTO hotel_management_db.user_role (user_id, role_id) VALUES (1, 2);
INSERT INTO hotel_management_db.user_role (user_id, role_id) VALUES (4, 2);
INSERT INTO hotel_management_db.user_role (user_id, role_id) VALUES (5, 2);
