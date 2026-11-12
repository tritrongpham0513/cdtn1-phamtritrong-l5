# Deployment Guide — Mekong Mobile Smart CRM, flow L5 (track SE), tag v1.0

Deployment and verification instructions for the Mekong Mobile Smart CRM Module L5 prototype.

---

## 1. Requirements

- **OS:** Windows 10/11, macOS, Linux.
- **Docker:** Engine 24+ and Compose v2 (tested on 28.5.1 / v2.40.3).
- **Docker must be running:** start Docker Desktop (Windows/macOS) or the Docker daemon (Linux) before step 3.
- **Git:** 2.30+.
- **Resources:** 2 GB RAM, 2 GB disk.
- **Ports:** `8088` and `3307` available.
- **Testing:** JDK 17 for host tests.
- **No Extra Tools:** MySQL, Maven, Java not needed for Docker.

---

## 2. Setup steps

After cloning, the stack starts with exactly two commands (steps 2 and 3).

1. Clone the repository and enter the project directory:
```bash
git clone --branch v1.0 https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5.git && cd cdtn1-phamtritrong-l5
```

2. Copy the environment template:
```bash
cp .env.example .env
```
*(Windows Command Prompt: `copy .env.example .env`)*

3. Build and start the stack:
```bash
docker compose up -d --build --wait
```

**Expected Result:**
Build takes 2-4 minutes. Blocks until containers report `Healthy`. Exit code 0.

---

## 3. Environment variables

| Variable | Meaning | Sample value | Required |
| :--- | :--- | :--- | :--- |
| `APP_PORT` | Web host port | `8088` | No (default: `8088`) |
| `MYSQL_HOST_PORT` | MySQL host port | `3307` | No (default: `3307`) |
| `MYSQL_ROOT_PASSWORD` | Root password | `root` | No (default: `root`) |
| `MYSQL_DATABASE` | DB name | `mekong_crm_inventory` | No (default: `mekong_crm_inventory`) |
| `MYSQL_USER` | App DB user | `springstudent` | No (default: `springstudent`) |
| `MYSQL_PASSWORD` | App DB password | `springstudent` | No (default: `springstudent`) |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `dev` | No (default: `dev`) |
| `SEED_ENABLED` | Seed ingestion | `true` | No (default: `true`) |
| `SEED_DATA_PATH` | CSV folder | `seeds` | No (default: `seeds`) |
| `PORT` | Container-internal value, fixed in `docker-compose.yml` | `8080` | Fixed — not configurable via `.env` |
| `TZ` | OS timezone | `Asia/Ho_Chi_Minh` | No (default: `Asia/Ho_Chi_Minh`) |
| `DB_HOST` | Container-internal value, fixed in `docker-compose.yml` | `mysql-db` | Fixed — not configurable via `.env` |
| `DB_PORT` | Container-internal value, fixed in `docker-compose.yml` | `3306` | Fixed — not configurable via `.env` |

> `DB_NAME`, `DB_USER`, and `DB_PASSWORD` are derived from `MYSQL_DATABASE`, `MYSQL_USER`, and `MYSQL_PASSWORD`; changing the `MYSQL_*` values also changes the application datasource.
>
> All other variables are optional because `docker-compose.yml` defines a default for each.

> [!WARNING]
> Changing `MYSQL_*` on an existing volume has no effect. Run `docker compose down -v` to reset. `APP_PORT` is host port; `PORT=8080` is internal. `TZ` is listed in `.env.example` and defaults to `Asia/Ho_Chi_Minh`.

---

## 4. Sample data

`DataInitializer` seeds 5 CSV files from `seeds/` on startup if empty. Idempotent.

Verify database records:
```bash
docker compose exec -T mysql-db mysql -uspringstudent -pspringstudent mekong_crm_inventory -e "SELECT 'service_centers' AS table_name, COUNT(*) AS rows_count FROM service_centers UNION ALL SELECT 'parts', COUNT(*) FROM parts UNION ALL SELECT 'tickets', COUNT(*) FROM tickets UNION ALL SELECT 'part_stock', COUNT(*) FROM part_stock UNION ALL SELECT 'part_transactions', COUNT(*) FROM part_transactions;"
```
*(MySQL stderr password warning is normal).*

**Expected Counts:**
- `service_centers`: 6
- `parts`: 180
- `tickets`: 7800
- `part_stock`: 717
- `part_transactions`: 4954

**Demo Accounts (seeded in `users` with BCrypt hashes):**

| Username | Password | Role | Center ID |
| :--- | :--- | :--- | :---: |
| `ktv_trong` | `123456` | `TECHNICIAN` | 1 |
| `quanly_kho` | `123456` | `MANAGER` | 1 |
| `ktv_cantho` | `123456` | `TECHNICIAN` | 4 |

Passwords are stored only as BCrypt hashes (`$2a$10$...`, 60 chars); plaintext `123456` is listed here solely for grading.

---

## 5. Verify it works

Verification steps:

### 1. Container Status
```bash
docker compose ps
```
Expected: Both containers show `Up` and `(healthy)`.

### 2. Health Endpoint
```bash
curl.exe -s http://localhost:8088/api/health
```
*(Linux/macOS: `curl -s http://localhost:8088/api/health`)*

Expected: HTTP 200 with JSON:
```json
{"status":"UP","timestamp":"<current time, GMT+7>","service":"inventory-service","module":"L5 - Kho linh kien thay the"}
```
*(the timestamp will differ on your machine)*

### 3. Web UI
```bash
curl.exe -s -o NUL -w "%{http_code}" http://localhost:8088/
```
*(Linux/macOS: `curl -s -o /dev/null -w "%{http_code}" http://localhost:8088/`)*

Expected: `200`.

### 4. Non-root User
```bash
docker compose exec -T inventory-service id
```
Expected: `uid=100(mekonguser) gid=101(mekonggroup) groups=101(mekonggroup)`.

### 5. Row Counts
Run SQL query in Section 4. Expected: matches 5 numbers.

### 6. Automated Tests
```powershell
.\mvnw.cmd test
```
*(Linux/macOS: `./mvnw test`)*

Expected: `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`.

---

## Troubleshooting

- **Port Conflict:** If `8088` is blocked, set `APP_PORT=18080` in `.env` and run `docker compose up -d --wait`.
- **Stale Volume:** Run `docker compose down -v` then `docker compose up -d --build --wait`.
- **Logs:** Inspect with `docker compose logs -f inventory-service` or `docker compose logs -f mysql-db`.

---

## 6. Optional: Steps Performed for Public Cloud Deployment (+1.0 Bonus Credit)

To satisfy the optional criteria for a fully functional public cloud deployment (+1.0 point on Assignment 3), the prototype was deployed and verified on managed cloud infrastructure with zero secrets stored in the repository:

- **Public Web Application:** [https://mekong-inventory-service.onrender.com](https://mekong-inventory-service.onrender.com)
- **Public Health Endpoint:** [https://mekong-inventory-service.onrender.com/api/health](https://mekong-inventory-service.onrender.com/api/health)

### Step-by-Step Procedure:

1. **Step 1: Managed Cloud Database Setup (Aiven Cloud MySQL 8.4):**
   - Provisioned a free managed MySQL 8.4 instance in Singapore region (`ap-southeast-1`).
   - Created the target database `mekong_crm_inventory` with TLS/SSL encryption enforced (`sslMode=REQUIRED`).
   - Isolated master credentials solely within the cloud provider's secure dashboard.

2. **Step 2: Web Service Creation (Render.com):**
   - Connected Render to the public GitHub repository (`main` branch).
   - Configured service runtime as **Docker**, automatically compiling via the multi-stage `Dockerfile` (Eclipse Temurin 17 Alpine JRE, non-root user `uid=100`).

3. **Step 3: Platform Environment Variables Configuration:**
   - In Render Dashboard (*Settings → Environment*), injected required environment variables securely:
     - `SPRING_PROFILES_ACTIVE`: `dev`
     - `DB_HOST`: `<aiven-mysql-host>`
     - `DB_PORT`: `<aiven-mysql-port>`
     - `DB_NAME`: `mekong_crm_inventory`
     - `DB_USER`: `avnadmin`
     - `DB_PASSWORD`: `<cloud-platform-secret-only>`
     - `PORT`: `8080`
     - `APP_PORT`: `8080`
     - `SEED_ENABLED`: `true`
     - `TZ`: `Asia/Ho_Chi_Minh`
   - Verified that no production passwords or credentials exist in git tracking or Docker image layers (satisfying OWASP A02 and Rubric Criterion C7).

4. **Step 4: Automated Build, Migration & Seed Verification:**
   - Render triggered the multi-stage Docker build and deployed the container.
   - On container bootstrap, Flyway applied both migrations (`V1__init_schema.sql` and `V2__add_users_table.sql`).
   - `DataInitializer` seeded 6 centers, 180 parts, 7,800 tickets, 717 stock records, 4,954 audit transactions, and 3 BCrypt-hashed user accounts (`$2a$10$`).
   - Validated live operational status: `GET /api/health` returned HTTP 200 with `status: UP`, and web prototype dashboard successfully authenticated demo users (`quanly_kho` / `123456`).
