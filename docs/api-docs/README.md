# REST API Documentation — Mekong Mobile Smart CRM, Module L5

- **Project:** Mekong Mobile Smart CRM — Module L5 (Replacement Parts Inventory Management)
- **Track:** Software Engineering (SE)
- **Student:** Pham Tri Trong (Student ID: 2374802010525)
- **Institution:** Van Lang University
- **Instructor:** Dr. Nguyen Tri Hai
- **Specification Version:** v1.0
- **Git Commit:** `3b08107` (code baseline); release tag `v1.0`
- **Date:** November 2026
- **Base URL:** `http://localhost:8088`
- **Container Port:** `8080` (mapped to host port `8088`)
- **Transport Security:** HTTP (Local Prototype / Docker Compose environment)

---

## 1. API Overview & Endpoint Index

#### Interactive Swagger UI & OpenAPI Specification
In addition to this Markdown documentation, an interactive Swagger UI explorer and standard OpenAPI 3.0 specification files are provided in this directory:
- **Interactive Swagger UI (Browser):** [`index.html`](index.html) — Open directly in any modern web browser or serve statically to interactively test all 17 endpoints against `http://localhost:8088`. Includes pre-inlined OpenAPI specifications to avoid local CORS restrictions under `file://` execution.
- **OpenAPI 3.0 Specification (JSON):** [`swagger.json`](swagger.json) — Full OpenAPI 3.0.3 specification in JSON format, ready for direct import into Postman, Insomnia, or Swagger Editor.
- **OpenAPI 3.0 Specification (YAML):** [`openapi.yaml`](openapi.yaml) — Standard OpenAPI 3.0.3 definition in YAML format.

This specification documents all 17 REST operations implemented in Module L5 across five controllers (`AuthController`, `HealthController`, `PartController`, `StockController`, `TransactionController`). All operations use `http://localhost:8088` as their base path.

| # | HTTP Method | Path | Authentication | Required Role / Scope | Description |
|:---:|:---|:---|:---|:---|:---|
| 1 | `POST` | `/api/v1/auth/login` | Public | None | Authenticate credentials and issue mock session token |
| 2 | `GET` | `/api/v1/auth/me` | Bearer Token | Any authenticated user | Retrieve currently logged-in user profile from session |
| 3 | `GET` | `/api/health` | Public | None | Service readiness and database operational health check |
| 4 | `POST` | `/api/v1/parts` | Public | None (Unauthenticated) | Register a new spare part into the central catalog |
| 5 | `GET` | `/api/v1/parts` | Public | None | Paginated search and listing of spare parts catalog |
| 6 | `GET` | `/api/v1/parts/{partCode}` | Public | None | Retrieve specific spare part metadata by unique part code |
| 7 | `PUT` | `/api/v1/parts/{partCode}` | Public | None (Unauthenticated) | Update spare part catalog details and active status |
| 8 | `DELETE` | `/api/v1/parts/{partCode}` | Public | None (Unauthenticated) | Perform soft delete (sets isActive=false) on spare part |
| 9 | `GET` | `/api/v1/stock` | Public | Center Scope (`X-Center-Id`, default 1) | List inventory stock levels with low-stock filter |
| 10 | `GET` | `/api/v1/stock/{partCode}` | Public | Center Scope (`X-Center-Id`, default 1) | Retrieve real-time stock balance and safety threshold |
| 11 | `POST` | `/api/v1/stock` | Bearer Token | `MANAGER` (`X-Center-Id` matching token) | Initialize inventory balance record for a part at center |
| 12 | `PUT` | `/api/v1/stock/threshold` | Bearer Token | `MANAGER` (`X-Center-Id` matching token) | Adjust minimum safety threshold for inventory alerting |
| 13 | `DELETE` | `/api/v1/stock/{partCode}` | Bearer Token | `MANAGER` (`X-Center-Id` matching token) | Remove zero-quantity inventory record from service center |
| 14 | `POST` | `/api/v1/transactions/import` | Bearer Token | `MANAGER` (`X-Center-Id` matching token) | Record warehouse inbound shipment and increment balance |
| 15 | `POST` | `/api/v1/transactions/export` | Bearer Token | `TECHNICIAN` (`X-Center-Id` matching token) | Export part for warranty repair with pessimistic locking |
| 16 | `GET` | `/api/v1/transactions` | Public | Center Scope (`X-Center-Id`, default 1) | Query immutable transaction ledger with filters and paging |
| 17 | `GET` | `/api/v1/tickets/{ticketId}/parts` | Public | None | List parts consumed by a specific warranty ticket |

---

## 2. General Conventions & Protocol Architecture

#### 2.1 Uniform Response Envelope
All business API endpoints (with the exception of `/api/health` which outputs a flat operational health map) wrap responses inside a uniform generic structure (`ApiResponseDTO<T>`):

```json
{
  "success": true,
  "data": {},
  "meta": {},
  "timestamp": "2026-11-12T00:35:08.716739692"
}
```

Envelope attributes:
- `success` (boolean, mandatory): Indicates whether the request was completed successfully (`true`) or resulted in an error (`false`).
- `data` (T, conditional): The primary payload returned on successful operations. Omitted during error responses.
- `meta` (object, optional): Pagination metadata returned exclusively for paginated list endpoints.
- `error` (object, conditional): Error envelope populated exclusively when `success` is `false`. Contains:
  - `code` (string): Standard machine-readable classification code.
  - `message` (string): Human-readable error explanation.
  - `details` (object, optional): Map of `fieldName -> validationMessage` describing each constraint failure (e.g. `{"quantity":"Quantity must be a positive integer > 0"}`).
- `timestamp` (string, mandatory): ISO-8601 formatted server timestamp.

#### 2.2 Jackson Serialization Rule (`NON_NULL`)
The backend is configured with `spring.jackson.default-property-inclusion=non_null`. Any field containing a `null` reference is completely omitted from the generated JSON response text. For example, successful responses omit `error`, whereas failure responses omit `data` and `meta`.

#### 2.3 Pagination Metadata Structure
Paginated query endpoints return a nested `meta` object with 1-based indexing:
- `page` (integer): Current page number requested (defaults to 1).
- `limit` (integer): Number of items per page (default is always `20`; callers may override it with the `limit` query parameter).
- `total` (long): Total number of matching records in the database matching criteria.
- `totalPages` (integer): Total available pages computed as `ceil(total / limit)`.

#### 2.4 Error Handling & Status Classification
The API utilizes a custom error envelope and does **not** adhere to RFC-7807 (Problem Details). All exceptions are intercepted by `GlobalExceptionHandler` and converted into standardized HTTP status codes and error bodies:

| HTTP Status | error.code | Triggering Exception | Description |
|:---|:---|:---|:---|
| 400 Bad Request | `BAD_REQUEST` | `IllegalArgumentException` | Invalid request parameter or illegal argument |
| 403 Forbidden | `FORBIDDEN` | `ForbiddenException` | Missing token, malformed token, unauthorized role, or center mismatch |
| 404 Not Found | `NOT_FOUND` | `ResourceNotFoundException` | Target entity not found in database |
| 409 Conflict | `BUSINESS_RULE_VIOLATION` | `BusinessRuleException`, `IllegalStateException` | Duplicate code, ticket not ready for export, insufficient stock, non-zero stock deletion |
| 422 Unprocessable Entity | `VALIDATION_ERROR` | `MethodArgumentNotValidException` | Jakarta validation constraint failure; includes `error.details` |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | `Exception`, `HttpMessageNotReadableException` | Unhandled runtime exception or malformed JSON payload |

---

## 3. Detailed Endpoint Specifications

### 1. Authenticate User — POST /api/v1/auth/login

#### 1. Method & Path
- **HTTP Method:** `POST`
- **Path:** `/api/v1/auth/login`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Authenticates user credentials against the `users` table. Passwords are stored as BCrypt hashes (`$2a$10$...`), and the server verifies the submitted password with `BCryptPasswordEncoder.matches()`. Upon successful verification, generates and returns a session Bearer token containing user identity, role, and assigned service center ID.

#### 3. Authentication & Authorization
- **Requirement:** Public (No authentication required)
- **Required Header:** None

#### 4. Request Parameters & Body
- **Content-Type:** `application/json`
- **Request Body Fields:**

| Field | Type | Required | Description | Constraints |
|:---|:---|:---:|:---|:---|
| `username` | string | Yes | Account username | Non-blank |
| `password` | string | Yes | Account password | Non-blank |

```bash
curl.exe -X POST "http://localhost:8088/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"quanly_kho","password":"123456"}'
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "userId": 2,
    "username": "quanly_kho",
    "fullName": "Qu\u1ea3n L\u00fd Kho",
    "role": "MANAGER",
    "centerId": 1,
    "centerName": "TT01 \u2014 TTBH Qu\u1eadn 10 (TP.HCM)",
    "token": "mk_token_MjpxdWFubHlfa2hvOk1BTkFHRVI6MToxNzk0NDIyMTk4MTEw"
  },
  "timestamp": "2026-11-12T01:36:38.115193824"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 409 Conflict | `BUSINESS_RULE_VIOLATION` | Username does not exist or password is incorrect |
| 422 Unprocessable Entity | `VALIDATION_ERROR` | Username or password is blank or missing |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | Malformed JSON body |

#### 7. Limitations & Implementation Notes
- Credentials are stored in the MySQL `users` table; `password_hash` is a BCrypt hash (cost 10), never plaintext.
- Demo accounts are seeded by Flyway `V2__add_users_table.sql` (MySQL) or by `DataInitializer` on H2/local profiles.
- Token format is `mk_token_<base64(userId:username:role:centerId:timestamp)>` without cryptographic signing.

---

### 2. Get Current User Profile — GET /api/v1/auth/me

#### 1. Method & Path
- **HTTP Method:** `GET`
- **Path:** `/api/v1/auth/me`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Parses the provided session Bearer token from the `Authorization` request header, decodes the user identity, and returns the current user profile.

#### 3. Authentication & Authorization
- **Requirement:** Authenticated
- **Required Header:** `Authorization: Bearer <token>`
- **Required Role:** Any valid authenticated role (`MANAGER` or `TECHNICIAN`)

#### 4. Request Parameters & Body
- **Headers:** `Authorization: Bearer mk_token_...`
- **Request Body:** None

```bash
curl.exe -X GET "http://localhost:8088/api/v1/auth/me" \
  -H "Authorization: Bearer mk_token_MTAyOnF1YW5seV9raG86TUFOQUdFUjoxOjE3OTQ0MTg1MDg3MTY="
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "userId": 102,
    "username": "quanly_kho",
    "fullName": "Qu\u1ea3n L\u00fd Kho",
    "role": "MANAGER",
    "centerId": 1,
    "centerName": "TT01 \u2014 TTBH Qu\u1eadn 10 (TP.HCM)",
    "token": "mk_token_MTAyOnF1YW5seV9raG86TUFOQUdFUjoxOjE3OTQ0MTg1MDg3MTY="
  },
  "timestamp": "2026-11-12T00:35:08.740385599"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 403 Forbidden | `FORBIDDEN` | Missing `Authorization` header, invalid token format, or malformed base64 |

#### 7. Limitations & Implementation Notes
- Token validity is verified by parsing the encoded base64 payload.
- Tokens do not enforce an absolute expiration check in the prototype.

---

### 3. Application Health Check — GET /api/health

#### 1. Method & Path
- **HTTP Method:** `GET`
- **Path:** `/api/health` (alias: `/api/v1/health`)
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Returns the operational health and readiness state of the Spring Boot application and database initializer. Used by Docker Compose healthcheck probes.

#### 3. Authentication & Authorization
- **Requirement:** Public (No authentication required)
- **Required Header:** None

#### 4. Request Parameters & Body
- **Parameters:** None
- **Request Body:** None

```bash
curl.exe -s -X GET "http://localhost:8088/api/health"
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "status": "UP",
  "service": "inventory-service",
  "module": "L5 - Kho linh kien thay the",
  "timestamp": "2026-11-12T00:35:08.745762591"
}
```

#### 6. Error Codes Table
| HTTP Status | Status Value | Condition |
|:---|:---|:---|
| 503 Service Unavailable | `STARTING` | Startup seeder or Flyway migration is actively running (`startupReadiness.isReady() == false`) |

#### 7. Limitations & Implementation Notes
- This endpoint returns a flat JSON structure rather than the `ApiResponseDTO` envelope to ensure compatibility with container orchestration tools.
- Dual mapped under both `/api/health` and `/api/v1/health`.

---

### 4. Create Part — POST /api/v1/parts

#### 1. Method & Path
- **HTTP Method:** `POST`
- **Path:** `/api/v1/parts`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Registers a new spare part into the central master catalog. Generates an auto-incremented internal `partId` and enforces unique constraints on `partCode`.

#### 3. Authentication & Authorization
- **Requirement:** Public (No token validation performed in controller)
- **Required Header:** None

#### 4. Request Parameters & Body
- **Content-Type:** `application/json`
- **Request Body Fields:**

| Field | Type | Required | Description | Constraints |
|:---|:---|:---:|:---|:---|
| `partCode` | string | Yes | Unique spare part SKU code | Max 30 chars, non-blank |
| `partName` | string | Yes | Display name of the part | Max 150 chars, non-blank |
| `category` | string | Yes | Part category (e.g. SCREEN, BATTERY) | Max 50 chars, non-blank |
| `unit` | string | Yes | Unit of measure (default: Piece) | Max 20 chars, non-blank |
| `unitPrice` | number | Yes | Base unit price in VND | Decimal >= 0.0 |

```bash
curl.exe -X POST "http://localhost:8088/api/v1/parts" \
  -H "Content-Type: application/json" \
  -d '{"partCode":"PART-API-01","partName":"OLED Display Assembly","category":"SCREEN","unit":"Piece","unitPrice":1250000.00}'
```

#### 5. Success Response Body
- **HTTP Status:** `201 Created`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "partId": 187,
    "partCode": "PART-API-01",
    "partName": "OLED Display Assembly",
    "category": "SCREEN",
    "unit": "Piece",
    "unitPrice": 1250000.00,
    "isActive": true,
    "createdAt": "2026-11-12T00:35:08.755963039"
  },
  "timestamp": "2026-11-12T00:35:08.771146932"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 409 Conflict | `BUSINESS_RULE_VIOLATION` | `partCode` already exists in catalog |
| 422 Unprocessable Entity | `VALIDATION_ERROR` | Validation constraint violation on request fields |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | Malformed JSON payload |

#### 7. Limitations & Implementation Notes
- Newly registered parts are active (`isActive = true`) by default.
- Does not automatically create inventory records in `part_stock`; inventory must be initialized via `POST /api/v1/stock`.

---

### 5. List Parts with Pagination — GET /api/v1/parts

#### 1. Method & Path
- **HTTP Method:** `GET`
- **Path:** `/api/v1/parts`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Retrieves a paginated list of active spare parts from the master catalog. Supports filtering by name/code keyword and category.

#### 3. Authentication & Authorization
- **Requirement:** Public (No authentication required)
- **Required Header:** None

#### 4. Request Parameters & Body
- **Query Parameters:**

| Parameter | Type | Required | Default | Description |
|:---|:---|:---:|:---:|:---|
| `keyword` | string | No | None | Filter by substring matching part code or part name |
| `category` | string | No | None | Exact filter by category code |
| `page` | integer | No | 1 | 1-based page number |
| `limit` | integer | No | 20 | Number of items per page |

```bash
curl.exe -s -X GET "http://localhost:8088/api/v1/parts?page=1&limit=2"
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": [
    {
      "partId": 70,
      "partCode": "CAM-AP-01",
      "partName": "Camera sau Apple lo\u1ea1i 1",
      "category": "CAMERA",
      "unit": "C\u00e1i",
      "unitPrice": 420000.00,
      "isActive": true,
      "createdAt": "2026-11-12T00:20:50"
    },
    {
      "partId": 71,
      "partCode": "CAM-AP-02",
      "partName": "Camera sau Apple lo\u1ea1i 2",
      "category": "CAMERA",
      "unit": "C\u00e1i",
      "unitPrice": 420000.00,
      "isActive": true,
      "createdAt": "2026-11-12T00:20:50"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 2,
    "total": 187,
    "totalPages": 94
  },
  "timestamp": "2026-11-12T00:35:08.784349505"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 400 Bad Request | `BAD_REQUEST` | Negative or invalid `page` or `limit` parameter |

#### 7. Limitations & Implementation Notes
- Only parts with `isActive = true` are returned by default.

---

### 6. Get Part by Code — GET /api/v1/parts/{partCode}

#### 1. Method & Path
- **HTTP Method:** `GET`
- **Path:** `/api/v1/parts/{partCode}`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Retrieves detailed metadata for a specific spare part identified by its unique string code (`partCode`).

#### 3. Authentication & Authorization
- **Requirement:** Public (No authentication required)
- **Required Header:** None

#### 4. Request Parameters & Body
- **Path Parameters:**
  - `partCode` (string, mandatory): Unique spare part code (e.g. `PART-API-01`).

```bash
curl.exe -s -X GET "http://localhost:8088/api/v1/parts/PART-API-01"
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "partId": 187,
    "partCode": "PART-API-01",
    "partName": "OLED Display Assembly",
    "category": "SCREEN",
    "unit": "Piece",
    "unitPrice": 1250000.00,
    "isActive": true,
    "createdAt": "2026-11-12T00:35:09"
  },
  "timestamp": "2026-11-12T00:35:08.794577656"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 404 Not Found | `NOT_FOUND` | No part found matching the specified `partCode` |

#### 7. Limitations & Implementation Notes
- Case-sensitive lookup based on MySQL `utf8mb4` collation.

---

### 7. Update Part — PUT /api/v1/parts/{partCode}

#### 1. Method & Path
- **HTTP Method:** `PUT`
- **Path:** `/api/v1/parts/{partCode}`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Updates the catalog metadata, pricing, or active status of an existing spare part identified by `partCode`.

#### 3. Authentication & Authorization
- **Requirement:** Public (No token validation performed in controller)
- **Required Header:** None

#### 4. Request Parameters & Body
- **Path Parameters:**
  - `partCode` (string, mandatory): Unique spare part code.
- **Content-Type:** `application/json`
- **Request Body Fields:**

| Field | Type | Required | Description | Constraints |
|:---|:---|:---:|:---|:---|
| `partName` | string | Yes | Updated part display name | Max 150 chars, non-blank |
| `category` | string | Yes | Updated category code | Max 50 chars, non-blank |
| `unit` | string | Yes | Unit of measure | Max 20 chars, non-blank |
| `unitPrice` | number | Yes | Unit price in VND | Decimal >= 0.0 |
| `isActive` | boolean | No | Active catalog flag | `true` or `false` |

```bash
curl.exe -X PUT "http://localhost:8088/api/v1/parts/PART-API-01" \
  -H "Content-Type: application/json" \
  -d '{"partName":"OLED Display Assembly V2","category":"SCREEN","unit":"Piece","unitPrice":1300000.00,"isActive":true}'
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "partId": 187,
    "partCode": "PART-API-01",
    "partName": "OLED Display Assembly V2",
    "category": "SCREEN",
    "unit": "Piece",
    "unitPrice": 1300000.00,
    "isActive": true,
    "createdAt": "2026-11-12T00:35:09"
  },
  "timestamp": "2026-11-12T00:35:08.813817896"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 404 Not Found | `NOT_FOUND` | Part code not found in database |
| 422 Unprocessable Entity | `VALIDATION_ERROR` | Validation failure on request body fields |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | Malformed JSON body |

#### 7. Limitations & Implementation Notes
- `partCode` cannot be modified via this endpoint.

---

### 8. Soft Delete Part — DELETE /api/v1/parts/{partCode}

#### 1. Method & Path
- **HTTP Method:** `DELETE`
- **Path:** `/api/v1/parts/{partCode}`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Performs a logical soft delete on a spare part by setting `isActive = false`. The part remains in the database to preserve historical integrity in transaction records.

#### 3. Authentication & Authorization
- **Requirement:** Public (No token validation performed in controller)
- **Required Header:** None

#### 4. Request Parameters & Body
- **Path Parameters:**
  - `partCode` (string, mandatory): Target spare part code.
- **Request Body:** None

```bash
curl.exe -X DELETE "http://localhost:8088/api/v1/parts/PART-API-01"
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": "X\u00f3a linh ki\u1ec7n PART-API-01 th\u00e0nh c\u00f4ng",
  "timestamp": "2026-11-12T00:35:08.827920152"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 404 Not Found | `NOT_FOUND` | Target `partCode` does not exist |

#### 7. Limitations & Implementation Notes
- Physical rows are not deleted from MySQL to maintain foreign key integrity with `part_stock` and `part_transactions`.

---

### 9. List Stock Balances — GET /api/v1/stock

#### 1. Method & Path
- **HTTP Method:** `GET`
- **Path:** `/api/v1/stock`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Retrieves a paginated list of inventory balances for a designated service center. Supports searching by keyword and filtering for parts below safety stock threshold (`isLowStock=true`).

#### 3. Authentication & Authorization
- **Requirement:** Public (Accepts optional center scope header)
- **Required Header:** `X-Center-Id: <id>` (defaults to `1` if omitted)

#### 4. Request Parameters & Body
- **Headers:** `X-Center-Id: 1`
- **Query Parameters:**

| Parameter | Type | Required | Default | Description |
|:---|:---|:---:|:---:|:---|
| `keyword` | string | No | None | Keyword filter on part code or name |
| `isLowStock` | boolean | No | None | Filter parts where `quantity <= minThreshold` |
| `page` | integer | No | 1 | 1-based page number |
| `limit` | integer | No | 10 | Records per page |

```bash
curl.exe -s -X GET "http://localhost:8088/api/v1/stock?page=1&limit=2" \
  -H "X-Center-Id: 1"
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": [
    {
      "partId": "MH-SS-01",
      "partName": "M\u00e0n h\u00ecnh Samsung lo\u1ea1i 1",
      "category": "MAN_HINH",
      "unit": "C\u00e1i",
      "unitPrice": 2200000.00,
      "quantity": 25,
      "minThreshold": 12,
      "isLowStock": false,
      "updatedAt": "2026-11-12T00:30:11"
    },
    {
      "partId": "MH-SS-02",
      "partName": "M\u00e0n h\u00ecnh Samsung lo\u1ea1i 2",
      "category": "MAN_HINH",
      "unit": "C\u00e1i",
      "unitPrice": 150000.00,
      "quantity": 10,
      "minThreshold": 3,
      "isLowStock": false,
      "updatedAt": "2026-11-12T00:20:56"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 2,
    "total": 125,
    "totalPages": 63
  },
  "timestamp": "2026-11-12T00:35:08.838907475"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 400 Bad Request | `BAD_REQUEST` | Invalid pagination parameter values |

#### 7. Limitations & Implementation Notes
- If `X-Center-Id` is not provided, defaults to center 1 (`TT01 — TTBH Quan 10`).

---

### 10. Get Part Stock Balance — GET /api/v1/stock/{partCode}

#### 1. Method & Path
- **HTTP Method:** `GET`
- **Path:** `/api/v1/stock/{partCode}`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Retrieves the real-time stock balance, minimum threshold, and low-stock warning flag for a specific part at a designated service center.

#### 3. Authentication & Authorization
- **Requirement:** Public
- **Required Header:** `X-Center-Id: <id>` (defaults to `1` if omitted)

#### 4. Request Parameters & Body
- **Headers:** `X-Center-Id: 1`
- **Path Parameters:**
  - `partCode` (string, mandatory): Target spare part code (e.g. `MH-SS-01`).

```bash
curl.exe -s -X GET "http://localhost:8088/api/v1/stock/MH-SS-01" \
  -H "X-Center-Id: 1"
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "partId": "MH-SS-01",
    "partName": "M\u00e0n h\u00ecnh Samsung lo\u1ea1i 1",
    "category": "MAN_HINH",
    "unit": "C\u00e1i",
    "unitPrice": 2200000.00,
    "quantity": 25,
    "minThreshold": 12,
    "isLowStock": false,
    "updatedAt": "2026-11-12T00:30:11"
  },
  "timestamp": "2026-11-12T00:35:08.848965751"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 404 Not Found | `NOT_FOUND` | No stock record exists for this part at the specified center |

#### 7. Limitations & Implementation Notes
- In the response DTO, `partId` represents the part code string (e.g. `MH-SS-01`).

---

### 11. Initialize Stock Record — POST /api/v1/stock

#### 1. Method & Path
- **HTTP Method:** `POST`
- **Path:** `/api/v1/stock`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Initializes an inventory balance record in `part_stock` for a spare part at the authenticated manager's service center. Enforces unique constraint per `(part_id, center_id)`.

#### 3. Authentication & Authorization
- **Requirement:** Authenticated Bearer Token
- **Required Role:** `MANAGER`
- **Center Scope:** `X-Center-Id` must match the `centerId` encoded in the token

#### 4. Request Parameters & Body
- **Headers:**
  - `Authorization: Bearer <manager_token>`
  - `X-Center-Id: 1`
  - `Content-Type: application/json`
- **Request Body Fields:**

| Field | Type | Required | Description | Constraints |
|:---|:---|:---:|:---|:---|
| `partCode` | string | Yes | Code of existing part in catalog | Non-blank |
| `initialQuantity` | integer | Yes | Initial quantity to establish | Integer >= 0 |
| `minThreshold` | integer | Yes | Safety stock alert threshold | Integer >= 0 |

> **Prerequisite:** `PART-STK-REAL` must already exist in the catalog. Create it first with `POST /api/v1/parts`.

```bash
curl.exe -X POST "http://localhost:8088/api/v1/stock" \
  -H "Authorization: Bearer mk_token_MTAyOnF1YW5seV9raG86TUFOQUdFUjoxOjE3OTQ0MTg1MDg3MTY=" \
  -H "X-Center-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{"partCode":"PART-STK-REAL","initialQuantity":25,"minThreshold":8}'
```

#### 5. Success Response Body
- **HTTP Status:** `201 Created`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "partId": "PART-STK-REAL",
    "partName": "Lithium Polymer Battery 5000mAh",
    "category": "BATTERY",
    "unit": "Piece",
    "unitPrice": 450000.00,
    "quantity": 25,
    "minThreshold": 8,
    "isLowStock": false,
    "updatedAt": "2026-11-12T00:35:47.932623989"
  },
  "timestamp": "2026-11-12T00:35:47.93870459"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 403 Forbidden | `FORBIDDEN` | Missing token, non-MANAGER role, or center mismatch |
| 404 Not Found | `NOT_FOUND` | Part code does not exist in central catalog |
| 409 Conflict | `BUSINESS_RULE_VIOLATION` | Stock record already initialized for this part at this center |
| 422 Unprocessable Entity | `VALIDATION_ERROR` | Validation constraint violation (e.g. negative quantity) |

#### 7. Limitations & Implementation Notes
- Requires that the part first exists in the `parts` table.

---

### 12. Update Safety Stock Threshold — PUT /api/v1/stock/threshold

#### 1. Method & Path
- **HTTP Method:** `PUT`
- **Path:** `/api/v1/stock/threshold`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Updates the minimum safety stock alert threshold (`minThreshold`) for a spare part at the manager's center. Re-evaluates the `isLowStock` boolean indicator immediately.

#### 3. Authentication & Authorization
- **Requirement:** Authenticated Bearer Token
- **Required Role:** `MANAGER`
- **Center Scope:** `X-Center-Id` must match the token's `centerId`

#### 4. Request Parameters & Body
- **Headers:**
  - `Authorization: Bearer <manager_token>`
  - `X-Center-Id: 1`
  - `Content-Type: application/json`
- **Request Body Fields:**

| Field | Type | Required | Description | Constraints |
|:---|:---|:---:|:---|:---|
| `partId` | string | Yes | Part code to adjust (e.g. `MH-SS-01`) | Non-blank |
| `minThreshold` | integer | Yes | New minimum threshold | Integer >= 0 |
| `reason` | string | No | Reason for threshold adjustment | Max 255 chars |

```bash
curl.exe -X PUT "http://localhost:8088/api/v1/stock/threshold" \
  -H "Authorization: Bearer mk_token_MTAyOnF1YW5seV9raG86TUFOQUdFUjoxOjE3OTQ0MTg1MDg3MTY=" \
  -H "X-Center-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{"partId":"MH-SS-01","minThreshold":12,"reason":"Buffer threshold increased for Q4"}'
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "partId": "MH-SS-01",
    "partName": "M\u00e0n h\u00ecnh Samsung lo\u1ea1i 1",
    "category": "MAN_HINH",
    "unit": "C\u00e1i",
    "unitPrice": 2200000.00,
    "quantity": 25,
    "minThreshold": 12,
    "isLowStock": false,
    "updatedAt": "2026-11-12T00:35:08.962080254"
  },
  "timestamp": "2026-11-12T00:35:08.967480962"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 403 Forbidden | `FORBIDDEN` | Missing token, non-MANAGER role, or center mismatch |
| 404 Not Found | `NOT_FOUND` | Stock record not found for specified part and center |
| 422 Unprocessable Entity | `VALIDATION_ERROR` | Validation constraint violation (e.g. negative threshold) |

#### 7. Limitations & Implementation Notes
- The DTO field is named `partId` in `UpdateThresholdRequestDTO`, but holds the part code string.

---

### 13. Delete Empty Stock Record — DELETE /api/v1/stock/{partCode}

#### 1. Method & Path
- **HTTP Method:** `DELETE`
- **Path:** `/api/v1/stock/{partCode}`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Deletes an inventory balance record from `part_stock` at the service center. Enforces the business rule that stock deletion is only permitted when current quantity is exactly zero.

#### 3. Authentication & Authorization
- **Requirement:** Authenticated Bearer Token
- **Required Role:** `MANAGER`
- **Center Scope:** `X-Center-Id` must match the token's `centerId`

#### 4. Request Parameters & Body
- **Headers:**
  - `Authorization: Bearer <manager_token>`
  - `X-Center-Id: 1`
- **Path Parameters:**
  - `partCode` (string, mandatory): Target part code.

> **Prerequisite:** the part and its stock record must already exist at the center with `quantity = 0`. Create them with `POST /api/v1/parts` and `POST /api/v1/stock`.

```bash
curl.exe -X DELETE "http://localhost:8088/api/v1/stock/PART-DEL-01" \
  -H "Authorization: Bearer mk_token_MTAyOnF1YW5seV9raG86TUFOQUdFUjoxOjE3OTQ0MTg1MDg3MTY=" \
  -H "X-Center-Id: 1"
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": "X\u00f3a b\u1ea3n ghi t\u1ed3n kho PART-DEL-01 th\u00e0nh c\u00f4ng",
  "timestamp": "2026-11-12T00:35:09.004612617"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 403 Forbidden | `FORBIDDEN` | Missing token, non-MANAGER role, or center mismatch |
| 404 Not Found | `NOT_FOUND` | Stock record does not exist |
| 409 Conflict | `BUSINESS_RULE_VIOLATION` | Current stock quantity > 0 (deletion prohibited) |

#### 7. Limitations & Implementation Notes
- Only records with `quantity == 0` can be deleted. Non-zero inventory must be depleted or transferred first.

---

### 14. Import Replacement Parts — POST /api/v1/transactions/import

#### 1. Method & Path
- **HTTP Method:** `POST`
- **Path:** `/api/v1/transactions/import`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Records an inbound shipment of spare parts into the service center warehouse. Atomically increments `quantity` in `part_stock` and appends an immutable audit record into `part_transactions`.

#### 3. Authentication & Authorization
- **Requirement:** Authenticated Bearer Token
- **Required Role:** `MANAGER`
- **Center Scope:** `X-Center-Id` must match the token's `centerId`

#### 4. Request Parameters & Body
- **Headers:**
  - `Authorization: Bearer <manager_token>`
  - `X-Center-Id: 1`
  - `Content-Type: application/json`
- **Request Body Fields:**

| Field | Type | Required | Description | Constraints |
|:---|:---|:---:|:---|:---|
| `partId` | string | Yes | Target spare part code | Non-blank |
| `quantity` | integer | Yes | Quantity to receive | Integer > 0 |
| `performedBy` | string | Yes | Identity or name of warehouse manager | Non-blank |
| `note` | string | No | Shipment reference or notes | Max 255 chars |

```bash
curl.exe -X POST "http://localhost:8088/api/v1/transactions/import" \
  -H "Authorization: Bearer mk_token_MTAyOnF1YW5seV9raG86TUFOQUdFUjoxOjE3OTQ0MTg1MDg3MTY=" \
  -H "X-Center-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{"partId":"MH-SS-01","quantity":10,"performedBy":"quanly_kho","note":"Restock batch 10 units"}'
```

#### 5. Success Response Body
- **HTTP Status:** `201 Created`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "transactionId": "GD-NK-202611-90DE",
    "transactionType": "IMPORT",
    "partId": "MH-SS-01",
    "partName": "M\u00e0n h\u00ecnh Samsung lo\u1ea1i 1",
    "quantity": 10,
    "remainingStock": 26,
    "isLowStock": false,
    "performedBy": "quanly_kho",
    "note": "Restock batch 10 units",
    "createdAt": "2026-11-12T00:55:18.875060611"
  },
  "timestamp": "2026-11-12T00:55:18.886020417"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 403 Forbidden | `FORBIDDEN` | Missing token, non-MANAGER role, or center mismatch |
| 404 Not Found | `NOT_FOUND` | Stock record or part does not exist |
| 422 Unprocessable Entity | `VALIDATION_ERROR` | Blank `partId`/`performedBy`, or `quantity <= 0` (Jakarta `@Min(1)`) |

#### 7. Limitations & Implementation Notes
- Automatically generates a unique transaction code with format `GD-NK-YYYYMM-XXXX`.
- `remainingStock` reflects balance after increment.

---

### 15. Export Part for Warranty Ticket — POST /api/v1/transactions/export

#### 1. Method & Path
- **HTTP Method:** `POST`
- **Path:** `/api/v1/transactions/export`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Exports a spare part for replacement under a warranty repair ticket. Executes under a pessimistic write lock (`SELECT ... FOR UPDATE`) on `part_stock` to prevent race conditions. Enforces business rules: ticket must belong to the center, must be in status `DANG_XU_LY` or `CHO_LINH_KIEN`, and center inventory must be sufficient.

#### 3. Authentication & Authorization
- **Requirement:** Authenticated Bearer Token
- **Required Role:** `TECHNICIAN`
- **Center Scope:** `X-Center-Id` must match the token's `centerId`

#### 4. Request Parameters & Body
- **Headers:**
  - `Authorization: Bearer <technician_token>`
  - `X-Center-Id: 1`
  - `Content-Type: application/json`
- **Request Body Fields:**

| Field | Type | Required | Description | Constraints |
|:---|:---|:---:|:---|:---|
| `ticketId` | string | Yes | Warranty ticket code | Non-blank |
| `partId` | string | Yes | Target spare part code | Non-blank |
| `quantity` | integer | Yes | Quantity to consume | Integer > 0 |
| `performedBy` | string | Yes | Technician identity or name | Non-blank |
| `note` | string | No | Work notes | Max 255 chars |

```bash
curl.exe -X POST "http://localhost:8088/api/v1/transactions/export" \
  -H "Authorization: Bearer mk_token_MTAxOmt0dl90cm9uZzpURUNITklDSUFOOjE6MTc5NDQxODUwODczNg==" \
  -H "X-Center-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{"ticketId":"BH-000135/2025","partId":"MH-SS-01","quantity":1,"performedBy":"ktv_trong","note":"Warranty replacement test"}'
```

#### 5. Success Response Body
- **HTTP Status:** `201 Created`
- **Response Payload:**

```json
{
  "success": true,
  "data": {
    "transactionId": "GD-XK-202611-8E19",
    "transactionType": "EXPORT",
    "partId": "MH-SS-01",
    "partName": "M\u00e0n h\u00ecnh Samsung lo\u1ea1i 1",
    "ticketId": "BH-000135/2025",
    "quantity": 1,
    "remainingStock": 25,
    "isLowStock": false,
    "performedBy": "ktv_trong",
    "note": "Warranty replacement test",
    "createdAt": "2026-11-12T00:55:58.718641102"
  },
  "timestamp": "2026-11-12T00:55:58.731104798"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 400 Bad Request | `BAD_REQUEST` | Ticket does not exist, or ticket does not belong to center |
| 403 Forbidden | `FORBIDDEN` | Missing token, non-TECHNICIAN role (including MANAGER), or center mismatch |
| 404 Not Found | `NOT_FOUND` | Stock record for part does not exist at center |
| 409 Conflict | `BUSINESS_RULE_VIOLATION` | Ticket not in `DANG_XU_LY`/`CHO_LINH_KIEN` status, or insufficient stock |
| 422 Unprocessable Entity | `VALIDATION_ERROR` | Validation constraint failure |

#### 7. Limitations & Implementation Notes
- Pessimistic lock (`PESSIMISTIC_WRITE`) guarantees serialized inventory deduction under concurrent requests (NFR-02).
- Strict role matching: `MANAGER` accounts cannot invoke this endpoint.

---

### 16. List Transaction Ledger — GET /api/v1/transactions

#### 1. Method & Path
- **HTTP Method:** `GET`
- **Path:** `/api/v1/transactions`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Queries the immutable audit ledger of stock transactions for a service center. Supports multi-criteria filtering by part code, transaction type (`IMPORT` or `EXPORT`), and date ranges.

#### 3. Authentication & Authorization
- **Requirement:** Public
- **Required Header:** `X-Center-Id: <id>` (defaults to `1` if omitted)

#### 4. Request Parameters & Body
- **Headers:** `X-Center-Id: 1`
- **Query Parameters:**

| Parameter | Type | Required | Default | Description |
|:---|:---|:---:|:---:|:---|
| `partId` | string | No | None | Filter by part code string |
| `type` | string | No | None | Filter by transaction type (`IMPORT` or `EXPORT`) |
| `fromDate` | string (ISO date) | No | None | Filter transactions from date (`YYYY-MM-DD`) |
| `toDate` | string (ISO date) | No | None | Filter transactions through date (`YYYY-MM-DD`) |
| `page` | integer | No | 1 | 1-based page number |
| `limit` | integer | No | 20 | Number of records per page |

```bash
curl.exe -s -X GET "http://localhost:8088/api/v1/transactions?page=1&limit=2" \
  -H "X-Center-Id: 1"
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload:**

```json
{
  "success": true,
  "data": [
    {
      "transactionId": "GD-XK-202611-8E19",
      "transactionType": "EXPORT",
      "partId": "MH-SS-01",
      "partName": "M\u00e0n h\u00ecnh Samsung lo\u1ea1i 1",
      "ticketId": "BH-000135/2025",
      "quantity": 1,
      "performedBy": "ktv_trong",
      "note": "Warranty replacement test",
      "createdAt": "2026-11-12T00:55:58"
    },
    {
      "transactionId": "GD-XK-202611-3C0F",
      "transactionType": "EXPORT",
      "partId": "MH-SS-01",
      "partName": "M\u00e0n h\u00ecnh Samsung lo\u1ea1i 1",
      "ticketId": "BH-000022/2025",
      "quantity": 1,
      "performedBy": "ktv_trong",
      "note": "Replaced damaged screen for ticket BH-000022/2025",
      "createdAt": "2026-11-12T00:35:09"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 2,
    "total": 866,
    "totalPages": 433
  },
  "timestamp": "2026-11-12T00:38:51.220193812"
}
```

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 400 Bad Request | `BAD_REQUEST` | Illegal pagination argument (`page < 0` or `limit <= 0`) |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | Unparseable `fromDate`/`toDate` value; falls through to the generic exception handler |

#### 7. Limitations & Implementation Notes
- Transactions are immutable (append-only ledger). No update or delete operations exist.

---

### 17. List Exported Parts by Ticket — GET /api/v1/tickets/{ticketId}/parts

#### 1. Method & Path
- **HTTP Method:** `GET`
- **Path:** `/api/v1/tickets/{ticketId}/parts`
- **Base URL:** `http://localhost:8088`

#### 2. Business Description
Retrieves the list of all spare parts that have been exported and consumed for a specific warranty repair ticket.

#### 3. Authentication & Authorization
- **Requirement:** Public (No authentication required)
- **Required Header:** None

#### 4. Request Parameters & Body
- **Path Parameters:**
  - `ticketId` (string, mandatory): Ticket identifier without path slashes (e.g. `TK-2026-001`). Seed ticket codes use the `BH-xxxxxx/yyyy` format and therefore cannot be routed through this endpoint (see Limitations).

```bash
curl.exe -s -X GET "http://localhost:8088/api/v1/tickets/TK-2026-001/parts"
```

#### 5. Success Response Body
- **HTTP Status:** `200 OK`
- **Response Payload (actual output for a ticket with no exported parts):**

```json
{
  "success": true,
  "data": [],
  "timestamp": "2026-11-12T00:56:57.905657836"
}
```

> When a hyphenated ticket does have exported parts, each element of `data` uses the shape `{transactionId, partId, partName, quantity, performedBy, exportDate}`.

#### 6. Error Codes Table
| HTTP Status | Error Code | Condition |
|:---|:---|:---|
| 400 Bad Request | (Tomcat level) | Ticket identifier contains URL-encoded slash (`%2F`) |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | Unencoded forward slash in ticket path variable causes path mismatch |

#### 7. Limitations & Implementation Notes
- If no parts have been exported for the ticket, returns HTTP 200 with an empty array (`"data": []`).
- Ticket codes containing forward slashes (e.g. seed format `BH-000001/2025`) cannot be routed through this URL path due to Tomcat and Spring MVC path separator rules. Only hyphenated/alphanumeric ticket identifiers can be queried.

---

## 4. Known Limitations & Architectural Constraints

1. **Base64 Bearer Tokens:** Credentials are stored as BCrypt hashes in the `users` table, but session tokens are still Base64-encoded strings (`mk_token_<base64>`) without cryptographic signatures or server-side session stores. There is no logout/revocation endpoint, so a leaked token remains usable until the client discards it.
2. **Unauthenticated Read Operations:** Read-only endpoints (`GET /api/v1/parts`, `GET /api/v1/stock`, `GET /api/v1/transactions`, and `GET /api/v1/tickets/{ticketId}/parts`) do not validate credentials or authorization headers.
3. **Default Center ID Fallback:** The `X-Center-Id` header defaults to `1` when omitted by clients. In a multi-tenant or multi-center deployment, clients failing to specify this header will silently access Center 1 data.
4. **Malformed JSON Handling:** Malformed JSON request bodies cause `HttpMessageNotReadableException`, which is caught by the general exception handler and returns HTTP 500 `INTERNAL_SERVER_ERROR` instead of standard HTTP 400 `BAD_REQUEST`.
5. **Soft Delete Semantics:** Deleting a part via `DELETE /api/v1/parts/{partCode}` toggles `is_active = false`. The database record is retained to preserve historical integrity in `part_stock` and `part_transactions`.
6. **Path Variable Slash Routing:** The endpoint `GET /api/v1/tickets/{ticketId}/parts` takes `{ticketId}` as a path variable. Ticket codes containing forward slashes (such as seed codes `BH-000022/2025`) cannot be dispatched because Spring MVC treats `/` as a segment delimiter, and Tomcat rejects URL-encoded slashes (`%2F`) with HTTP 400. Tickets must use hyphenated identifiers.
7. **Strict Role Equality:** Authorization checks use exact string comparison (`requireRoleAndCenter`). The `MANAGER` role cannot perform operations reserved for `TECHNICIAN` (e.g., export), and `TECHNICIAN` cannot perform operations reserved for `MANAGER` (e.g., import, stock creation, threshold updates, or stock deletion).
