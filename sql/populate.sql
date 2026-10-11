-- =============================================================================
-- SMARTCAMPUS SYSTEM: DML SAMPLE DATA (populate.sql)
-- Target RDBMS: PostgreSQL 16+
-- Description: Inserts coherent mock data for testing graph routing, CSP scheduling, 
--              RBAC security, and mobile check-ins.
-- =============================================================================

-- Clean existing data in correct dependency order (foreign-key safe)
TRUNCATE TABLE access_logs, dynamic_access_beacons, reservations, room_equipment, 
             equipment, rooms, campus_edges, campus_nodes, buildings, users, roles RESTART IDENTITY CASCADE;

-- -----------------------------------------------------------------------------
-- 1. ROLES & USERS (RBAC & Security)
-- -----------------------------------------------------------------------------

INSERT INTO roles (role_name, description) VALUES
('ADMIN', 'System administrator with full database and server privileges'),
('PROFESSOR', 'Faculty member capable of managing courses and priority room bookings'),
('STUDENT', 'Enrolled undergraduate/graduate student utilizing campus navigation and study pods');

-- -----------------------------------------------------------------------------
-- 2. BUILDINGS & SPATIAL GRAPH NODES (Discrete Math & A* Pathfinding)
-- Represents Building B1 (Ground Floor and 1st Floor layout)
-- -----------------------------------------------------------------------------

INSERT INTO buildings (code, name) VALUES
('B1', 'Faculdade de Design, Tecnologia e Comunicação - Edifício Principal');

-- Nodes for Floor 0 (Ground) and Floor 1
INSERT INTO campus_nodes (building_id, floor_number, coord_x, coord_y, node_type, is_accessible) VALUES
(1, 0, 10.00, 10.00, 'ENTRANCE', TRUE),  -- Node 1: Main Entrance
(1, 0, 15.00, 10.00, 'CORRIDOR', TRUE),  -- Node 2: Ground Hallway A
(1, 0, 20.00, 10.00, 'ROOM', TRUE),      -- Node 3: Room B1.01 (Study Pod)
(1, 0, 15.00, 15.00, 'STAIRS', FALSE),   -- Node 4: Staircase to Floor 1 (Not wheelchair accessible)
(1, 0, 15.00, 16.00, 'ELEVATOR', TRUE),  -- Node 5: Elevator to Floor 1
(1, 1, 15.00, 16.00, 'ELEVATOR', TRUE),  -- Node 6: Floor 1 Elevator Exit
(1, 1, 15.00, 20.00, 'CORRIDOR', TRUE),  -- Node 7: First Floor Hallway B
(1, 1, 20.00, 20.00, 'ROOM', TRUE);      -- Node 8: Room B1.10 (Advanced Lab)

-- Weighted Edges representing walkable paths (distances in meters)
-- Return paths (bidirectional connectivity)
INSERT INTO campus_edges (source_node_id, target_node_id, weight_distance, requires_stairs) VALUES
(2, 1, 5.00, FALSE),
(3, 2, 5.00, FALSE),
(4, 2, 5.00, TRUE),
(5, 2, 6.00, FALSE),
(6, 5, 4.00, FALSE),
(6, 4, 4.00, TRUE),
(7, 6, 4.00, FALSE),
(8, 7, 5.00, FALSE);

-- -----------------------------------------------------------------------------
-- 3. FACILITIES & CSP SCHEDULING RESOURCES
-- -----------------------------------------------------------------------------

INSERT INTO rooms (node_id, room_number, capacity, room_type) VALUES
(3, 'B1.01', 6, 'STUDY_POD'),
(8, 'B1.10', 25, 'LABORATORY');

INSERT INTO equipment (equipment_name, description) VALUES
('DUAL_MONITOR', 'Workstation equipped with dual 27-inch 4K monitors'),
('PROJECTOR', 'High-definition ceiling projector with HDMI/Wireless casting'),
('VR_HEADSET', 'Virtual Reality headset kit for development and simulation');

INSERT INTO room_equipment (room_id, equipment_id, quantity) VALUES
(1, 1, 2), -- Room B1.01 has 2 dual monitor setups
(2, 2, 1), -- Room B1.10 has 1 projector
(2, 3, 5); -- Room B1.10 has 5 VR headsets

-- -----------------------------------------------------------------------------
-- 4. RESERVATIONS & ACCESS CONTROL (Mobile QR / BLE Integration)
-- -----------------------------------------------------------------------------

INSERT INTO reservations (user_id, room_id, start_time, end_time, status) VALUES
(3, 1, CURRENT_TIMESTAMP + INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '3 hours', 'CONFIRMED'),
(4, 2, CURRENT_TIMESTAMP + INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '4 hours', 'CONFIRMED');

INSERT INTO dynamic_access_beacons (room_id, current_token, expires_at) VALUES
(1, 'tok_sec_b101_99482a', CURRENT_TIMESTAMP + INTERVAL '15 minutes'),
(2, 'tok_sec_b110_11029c', CURRENT_TIMESTAMP + INTERVAL '15 minutes');

INSERT INTO access_logs (reservation_id, verification_method, success) VALUES
(1, 'QR_SCAN', TRUE);