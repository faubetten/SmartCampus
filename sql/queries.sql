-- =============================================================================
-- SMARTCAMPUS SYSTEM: ANALYTICAL QUERIES (queries.sql)
-- Target RDBMS: PostgreSQL 16+
-- Description: Demonstrates advanced SQL competency (joins, aggregations, 
--              window functions, graph lookups, and security audit metrics).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- QUERY 1: Facility Resource Inventory & Equipment Audit
-- Purpose: Multi-table join with aggregation to audit available equipment per room and building.
-- Evaluates: Relational mapping across buildings, rooms, and equipment.
-- -----------------------------------------------------------------------------

SELECT 
    b.code AS building_code,
    b.name AS building_name,
    r.room_number,
    r.room_type,
    r.capacity,
    COALESCE(STRING_AGG(CONCAT(e.equipment_name, ' (Qty: ', re.quantity, ')'), ', '), 'No Special Equipment') AS installed_equipment
FROM buildings b
JOIN campus_nodes cn ON b.building_id = cn.building_id
JOIN rooms r ON cn.node_id = r.node_id
LEFT JOIN room_equipment re ON r.room_id = re.room_id
LEFT JOIN equipment e ON re.equipment_id = e.equipment_id
GROUP BY b.code, b.name, r.room_number, r.room_type, r.capacity
ORDER BY b.code, r.room_number;


-- -----------------------------------------------------------------------------
-- QUERY 2: Room Utilization Ranking (Window Functions)
-- Purpose: Uses window functions (RANK() / COUNT()) to rank study spaces and labs 
--          by reservation volume and capacity.
-- Evaluates: Advanced SQL reporting features for administrative dashboards.
-- -----------------------------------------------------------------------------

SELECT 
    r.room_number,
    r.room_type,
    r.capacity,
    COUNT(res.reservation_id) AS total_reservations,
    RANK() OVER (ORDER BY COUNT(res.reservation_id) DESC, r.capacity DESC) AS utilization_rank
FROM rooms r
LEFT JOIN reservations res ON r.room_id = res.room_id
GROUP BY r.room_id, r.room_number, r.room_type, r.capacity
ORDER BY utilization_rank;


-- -----------------------------------------------------------------------------
-- QUERY 3: Spatial Graph Edge & Adjacency Lookup (A* Pathfinding Support)
-- Purpose: Retrieves walkable paths, physical distances, and accessibility constraints 
--          between connected campus nodes to feed the backend A* routing engine.
-- Evaluates: Integration between graph structures and database storage.
-- -----------------------------------------------------------------------------

SELECT 
    e.edge_id,
    b.code AS building_code,
    src.floor_number AS source_floor,
    src.node_type AS source_type,
    tgt.floor_number AS target_floor,
    tgt.node_type AS target_type,
    e.weight_distance AS distance_meters,
    e.requires_stairs
FROM campus_edges e
JOIN campus_nodes src ON e.source_node_id = src.node_id
JOIN campus_nodes tgt ON e.target_node_id = tgt.node_id
JOIN buildings b ON src.building_id = b.building_id
ORDER BY e.weight_distance ASC;


-- -----------------------------------------------------------------------------
-- QUERY 4: Security & Access Control Audit (GDPR & Check-In Success Rate)
-- Purpose: Evaluates verification methods (QR vs. BLE) and calculates success rates 
--          for access control logging.
-- Evaluates: Security metrics and audit trail generation.
-- -----------------------------------------------------------------------------

SELECT 
    al.verification_method,
    COUNT(*) AS total_attempts,
    SUM(CASE WHEN al.success THEN 1 ELSE 0 END) AS successful_scans,
    SUM(CASE WHEN NOT al.success THEN 1 ELSE 0 END) AS failed_scans,
    ROUND((SUM(CASE WHEN al.success THEN 1 ELSE 0 END)::DECIMAL / COUNT(*)) * 100, 2) AS success_rate_percentage
FROM access_logs al
GROUP BY al.verification_method;


-- -----------------------------------------------------------------------------
-- QUERY 5: Active User Reservations with RBAC & Role Filtering
-- Purpose: Subquery filtering users by role ('STUDENT' vs 'PROFESSOR') with active bookings.
-- Evaluates: Role-Based Access Control (RBAC) persistence and filtering.
-- -----------------------------------------------------------------------------

SELECT 
    u.name AS user_name,
    u.email,
    rol.role_name,
    rm.room_number,
    res.start_time,
    res.end_time,
    res.status
FROM reservations res
JOIN users u ON res.user_id = u.user_id
JOIN roles rol ON u.role_id = rol.role_id
JOIN rooms rm ON res.room_id = rm.room_id
WHERE res.status IN ('CONFIRMED', 'ACTIVE')
  AND u.role_id IN (SELECT role_id FROM roles WHERE role_name IN ('STUDENT', 'PROFESSOR'))
ORDER BY res.start_time ASC;