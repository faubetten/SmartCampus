# SmartCampus — Intelligent Campus Navigation & Resource Allocation System

SmartCampus is a distributed, full-stack mobile and backend platform designed to modernize university campus operations. It combines **Constraint Satisfaction Problem (CSP) scheduling** with **$A^*$ indoor spatial pathfinding** to provide a seamless experience: discovering available campus facilities, inspecting installed room equipment, booking conflict-free time slots, and navigating multi-floor university buildings.

---

## Table of Contents

1. [Introduction & Project Goals](#1-introduction--project-goals)
2. [Who Is SmartCampus For?](#2-who-is-smartcampus-for)
3. [Key Features & User Flow](#3-key-features--user-flow)
4. [System Architecture & Tech Stack](#4-system-architecture--tech-stack)
5. [Core AI & Algorithmic Pillars](#5-core-ai--algorithmic-pillars)
6. [Repository Structure](#6-repository-structure)
7. [Getting Started & How to Run](#7-getting-started--how-to-run)
8. [API Endpoints Reference](#8-api-endpoints-reference)
9. [Testing & Verification](#9-testing--verification)
10. [Development Roadmap](#10-development-roadmap)

---

## 1. Introduction & Project Goals

University campuses are complex environments where students, faculty, and visitors frequently face two interconnected challenges:
1. **Resource Contention & Scheduling Friction**: Finding an available study pod, laboratory, or classroom that fits a specific group size and has the required hardware (such as projectors, dual monitors, or VR headsets) without double-booking conflicts.
2. **Indoor Wayfinding**: Navigating multi-story campus buildings where standard outdoor GPS signals fail and where accessibility constraints (such as avoiding stairs in favor of elevators) must be respected.

**SmartCampus** addresses both problems in a unified ecosystem. Built with a high-availability **Spring Boot 3** backend cluster load-balanced by **Nginx**, backed by **PostgreSQL 16**, and consumed by a native **Android (Jetpack Compose)** client, the platform guides a user from initial room discovery all the way to the physical door of their reserved facility.

---

## 2. Who Is SmartCampus For?

* **Students**: Quickly search for available study pods or group workspaces filtered by seating capacity and time window, inspect room amenities, reserve a slot, and navigate directly to the room.
* **Professors & Researchers**: Book specialized laboratories, classrooms, and auditoriums equipped with specific hardware (`PROJECTOR`, `VR_HEADSET`, `DUAL_MONITOR`) with guaranteed conflict detection.
* **Mobility-Impaired Campus Members**: Compute step-free indoor routes across floors using the wheelchair-accessible navigation mode, which automatically prunes staircases from the search graph.
* **Campus Administrators & Facility Managers**: Manage building topologies, indoor graph nodes and edges, rooms, equipment inventories, user roles, and reservations through dedicated RESTful CRUD endpoints and database audit views.

---

## 3. Key Features & User Flow

1. **Authentication & Account Recovery**:
   * Stateless JWT authentication with BCrypt password hashing.
   * Self-service account registration (`STUDENT` role provisioning).
   * Two-step **Forgot Password** workflow using time-bounded 6-digit One-Time Passwords (OTP) with an integrated mobile simulation helper.
2. **Multi-Room CSP Discovery & Preference Filtering**:
   * Users specify group capacity, facility classification (`ALL`, `STUDY_POD`, `LABORATORY`, `AUDITORIUM`, `CLASSROOM`), and an ISO-8601 time window.
   * The backend prunes conflicting reservations and returns all matching rooms sorted by best capacity fit.
3. **Rich Facility Inspection & Instant Booking**:
   * Users inspect a room's building name, floor number, 2D spatial coordinates, and installed equipment quantities before confirming their reservation.
4. **Indoor $A^*$ Wayfinding**:
   * Seamless handoff from a confirmed room booking to the navigation engine, calculating the optimal path across corridors, staircases, and elevators.

---

## 4. System Architecture & Tech Stack

### High-Availability Distributed Architecture

```text
+-------------------------------------------------------------------+
|                 Android Client (Jetpack Compose)                  |
|      EncryptedSharedPreferences (JWT) + Retrofit2 / OkHttp3       |
+-------------------------------------------------------------------+
                                  |
                                  | HTTP / REST (Port 8080)
                                  v
+-------------------------------------------------------------------+
|               Nginx Reverse Proxy & Load Balancer                 |
|                  (Round-Robin Upstream Routing)                   |
+-------------------------------------------------------------------+
                  /                               \
                 v                                 v
+---------------------------------+ +---------------------------------+
|   Spring Boot Replica (Node 1)  | |   Spring Boot Replica (Node 2)  |
|   - Auth & Security (JWT)       | |   - Auth & Security (JWT)       |
|   - A* Pathfinding Engine       | |   - A* Pathfinding Engine       |
|   - CSP Room Scheduler          | |   - CSP Room Scheduler          |
|   - Admin CRUD Controllers      | |   - Admin CRUD Controllers      |
+---------------------------------+ +---------------------------------+
                  \                               /
                   v                             v
+-------------------------------------------------------------------+
|                PostgreSQL 16 Relational Database                  |
|     Spatial Graph (Nodes/Edges), Rooms, Equipment, RBAC, Logs     |
+-------------------------------------------------------------------+
```

### Technology Stack

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Mobile Client** | Kotlin 2.0, Jetpack Compose (Material 3) | Declarative Android UI & state management |
| **Mobile Navigation** | AndroidX Navigation Compose | Multi-screen routing & Bottom Navigation dashboard |
| **Networking & Auth** | Retrofit 2.11, OkHttp 4.12, Gson, Security Crypto | REST consumption, Bearer token interception & Keystore persistence |
| **Backend Framework** | Java 21, Spring Boot 3.2.12 | Stateless REST API services |
| **Security** | Spring Security 6, JJWT (`0.12.5`), BCrypt | Role-Based Access Control (RBAC) & JWT signing/validation |
| **Persistence** | Spring Data JPA (Hibernate) & `JdbcTemplate` | ORM entity management & spatial/equipment joins |
| **Database** | PostgreSQL 16 (Alpine) | ACID-compliant storage, topological graph data & analytical views |
| **Infrastructure** | Docker, Docker Compose, Nginx (Alpine) | Container orchestration & horizontal load balancing |
| **Testing & QA** | JUnit 5, Spring MockMvc, Postman, Python 3 | Automated unit/integration tests, API collections & smoke scripts |

---

## 5. Core AI & Algorithmic Pillars

### Pillar 1: $A^*$ Indoor Spatial Pathfinding (`PathfindingService`)
The campus interior is modeled as a weighted directed graph $G = (V, E)$, where vertices $V$ (`campus_nodes`) represent physical waypoints (`ENTRANCE`, `HALLWAY`, `ROOM`, `STAIRS`, `ELEVATOR`) and edges $E$ (`campus_edges`) represent walkable segments weighted by metric distance.

For any start node and target node, the engine evaluates states using the cost function:

$$f(n) = g(n) + h(n)$$

* $g(n)$: Exact accumulated metric distance along traversed edges from the start node to node $n$.
* $h(n)$: Admissible 3D Euclidean-inspired heuristic combining 2D planar distance with a vertical floor-transition penalty:

$$h(n) = \sqrt{(x_n - x_{\text{goal}})^2 + (y_n - y_{\text{goal}})^2} + 10.0 \times | \text{floor}_n - \text{floor}_{\text{goal}} |$$

* **Accessibility Constraint**: When `accessibleOnly = true`, any edge with `requires_stairs = TRUE` is pruned during neighbor expansion, guaranteeing step-free elevator routing.

### Pillar 2: Constraint Satisfaction Problem (CSP) Room Scheduler (`CSPSchedulingService`)
Room allocation is formulated as a Constraint Satisfaction Problem defined by variables, domains, and hard constraints:
* **Domain Pruning (Unary Constraints)**: Filters candidate rooms where $\text{capacity} \ge C_{\text{requested}}$ and $\text{roomType} = T_{\text{requested}}$ (or across all types when `ALL` is selected).
* **Best-Fit Ordering Heuristic**: Candidate rooms are ordered by ascending capacity so smaller groups are assigned appropriately sized rooms first, preserving large laboratories and auditoriums for larger classes.
* **Temporal Conflict Checking (Binary Constraints)**: Queries `reservations` to verify that no `CONFIRMED` or `ACTIVE` booking overlaps the requested $[\text{startTime}, \text{endTime}]$ interval:

$$\neg \exists r \in R_{\text{room}} : (\text{startTime} < r.\text{endTime}) \land (\text{endTime} > r.\text{startTime})$$

---

## 6. Repository Structure

```text
SmartCampus/
├── android/                  # Native Android Client (Kotlin + Jetpack Compose)
│   ├── app/src/main/java/com/smartcampus/client/
│   │   ├── api/              # Retrofit interfaces, NetworkClient & AuthInterceptor
│   │   ├── data/             # Domain models, Request/Response DTOs
│   │   ├── security/         # EncryptedSharedPreferences TokenManager
│   │   └── ui/               # Compose Screens, Components, Navigation & Theme
├── backend/                  # Spring Boot 3 Backend Application (Java 21)
│   ├── src/main/java/com/smartcampus/
│   │   ├── controller/       # Auth, Navigation, Scheduling & Admin CRUD Controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── model/            # JPA Entities (User, Role, Room, Equipment, CampusNode, etc.)
│   │   ├── repository/       # Spring Data JPA Repositories
│   │   ├── security/         # JWT Provider, Filter & SecurityConfig
│   │   └── service/          # PathfindingService (A*) & CSPSchedulingService (CSP)
│   └── src/test/             # JUnit 5 & MockMvc Integration Tests
├── docker/                   # Containerization & Reverse Proxy
│   ├── docker-compose.yml    # Orchestrates Postgres, 2x API Nodes, and Nginx
│   ├── .env.example          # Template for environment variables
│   └── nginx/nginx.conf      # Round-robin load balancer configuration
├── postman/                  # Postman Collections & Local/Production Environments
├── sql/                      # PostgreSQL Schema & Seed Data
│   ├── create.sql            # DDL tables, constraints, indexes & analytical views
│   ├── populate.sql          # Initial seed graph, rooms, equipment & demo users
│   └── queries.sql           # Reference analytical SQL queries
├── Documentos/               # Project reports, architecture notes & TODOs
├── test_db.py                # Python database connectivity & schema verification script
└── test_endpoints.py         # Python end-to-end API & load-balancer verification script
```

---

## 7. Getting Started & How to Run

### Prerequisites
* **Docker Desktop** (or Docker Engine + Docker Compose)
* **Android Studio** (Ladybug / Koala or newer, with Android SDK 35)
* **Java Development Kit (JDK) 21** *(optional, for running Maven outside Docker)*
* **Python 3.x** *(optional, for running automated verification scripts)*

---

### Step 1: Configure Environment Variables

Navigate to the `docker/` directory and create a `.env` file from `.env.example`:

```bash
cd docker
cp .env.example .env
```

Ensure `docker/.env` contains the following values:

```env
# PostgreSQL Database Configuration
DB_NAME=smartcampus_db
DB_USER=campus_admin
DB_PASSWORD=SuperSecretDatabasePassword2026!

# JWT Secret & Expiration (256-bit Base64-encoded HMAC-SHA256 key)
JWT_SECRET=3VTyJzMC2kNYnfPYeD5AX5IqDiImPQwS54Zf07M2524=
JWT_EXPIRATION_MS=86400000
```

---

### Step 2: Launch the Distributed Backend & Database

From inside the `docker/` directory, build and start all containers in detached mode:

```bash
docker compose up --build -d
```

This command launches four containers:
1. `smartcampus-postgres` (PostgreSQL 16 initialized with `sql/create.sql` and `sql/populate.sql`)
2. `smartcampus-api-1` (Spring Boot replica 1)
3. `smartcampus-api-2` (Spring Boot replica 2)
4. `smartcampus-loadbalancer` (Nginx listening on `http://localhost:8080`)

To inspect live server logs:
```bash
docker compose logs -f api-node-1 api-node-2
```

---

### Step 3: Run the Android Mobile App

1. Open **Android Studio** and select **Open** $\rightarrow$ choose the `android/` directory.
2. Wait for Gradle to sync and generate `local.properties`.
3. **Configure Device Networking**:
   * **Android Emulator**: Out of the box, `NetworkClient.kt` uses `http://10.0.2.2:8080/` (or `http://localhost:8080/`), which routes directly to your host machine's Docker load balancer.
   * **Physical Android Device (via USB)**: Connect your phone with USB Debugging enabled and run:
     ```bash
     adb reverse tcp:8080 tcp:8080
     ```
     *(Alternatively, set `BASE_URL` in `NetworkClient.kt` to your computer's local Wi-Fi IPv4 address, e.g., `http://192.168.1.X:8080/`).*
4. Click **Run 'app'** (`Shift + F10`).

### Default Demo Credentials

The backend automatically seeds a default student account on startup:
* **Email**: `joao.silva@student.universidadeeuropeia.pt`
* **Password**: `SecurePass123!`

---

## 8. API Endpoints Reference

All requests are routed through the Nginx Load Balancer at `http://localhost:8080`.

### Public Authentication Endpoints (`/api/auth`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticates user credentials and returns a Bearer JWT. |
| `POST` | `/api/auth/register` | Registers a new student account with a BCrypt-hashed password. |
| `POST` | `/api/auth/forgot-password` | Generates a 15-minute 6-digit recovery OTP for an institutional email. |
| `POST` | `/api/auth/reset-password` | Validates the 6-digit OTP and updates the user's password. |

### Protected Core Endpoints (Requires `Authorization: Bearer <JWT>`)
| Method | Endpoint | Parameters | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/navigation/route` | `startId`, `targetId`, `accessibleOnly` | Computes the optimal $A^*$ indoor path between two campus graph nodes. |
| `GET` | `/api/scheduling/available` | `capacity`, `roomType`, `startTime`, `endTime` | Returns all available rooms matching preferences, sorted by fit and enriched with building, floor, coordinates, and equipment. |
| `POST` | `/api/scheduling/book` | `roomId`, `startTime`, `endTime` | Books a specific room for the authenticated user and persists a `CONFIRMED` reservation. |
| `POST` | `/api/scheduling/allocate` | `capacity`, `roomType`, `startTime`, `endTime` | Auto-allocates and books the single best-fitting room via CSP. |

### Admin Management Endpoints (`/api/admin/**`)
Full CRUD (`GET`, `POST`, `PUT`, `DELETE`) controllers are available under:
* `/api/admin/rooms`
* `/api/admin/campus-nodes`
* `/api/admin/campus-edges`
* `/api/admin/reservations`
* `/api/admin/users`
* `/api/admin/roles`

---

## 9. Testing & Verification

### 1. Python Database & Endpoint Smoke Tests
From the root directory, install the test dependencies and run the verification scripts:

```bash
pip install psycopg2-binary requests
python test_db.py
python test_endpoints.py
```

### 2. Backend JUnit 5 & MockMvc Suite
Run the automated CRUD and controller test suite via Maven:

```bash
cd backend
mvn test
```

### 3. Postman Collection
Import the collection and environments from the `postman/` directory into Postman:
* Collection: `postman/collections/SmartCampus API`
* Environment: `postman/environments/SmartCampus Local.environment.yaml`

---

## 10. Development Roadmap

- [x] **Phase 1 — Foundation & Distributed Infrastructure**: PostgreSQL 16 schema, Docker Compose cluster, Nginx round-robin load balancer, Stateless JWT security, and Admin CRUD controllers.
- [x] **Phase 2 — Core AI Services & Mobile Auth**: Backend $A^*$ pathfinding and CSP solver implementation; Android Login, Registration, and OTP Password Recovery screens.
- [x] **Sprint 1 — Multi-Room Discovery & Rich Facility Booking**: Multi-room CSP search, equipment/building enrichment, room details view, reservation confirmation, and navigation handoff.
- [ ] **Sprint 2 — Interactive 2D Campus Floorplan Visualizer**: Custom Jetpack Compose `Canvas` rendering building floors, walkable corridor edges, elevators, stairs, and room markers.
- [ ] **Sprint 3 — Live Turn-by-Turn Indoor Navigation**: Animated route progression along graph edges with real-time distance/ETA updates, floor transition prompts, and arrival detection.
- [ ] **Sprint 4 — QR Code Check-In & Active Session Timers**: CameraX QR validation against room access beacons (`CONFIRMED` $\rightarrow$ `ACTIVE`), live reservation countdown timer, and expiration notifications.