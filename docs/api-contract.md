# API Contract – Kho linh kiện thay thế (L5)

> **Dự án:** Smart CRM – Mekong Mobile  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế  
> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525 – Track SE  
> **Công nghệ:** Java 17 · Spring Boot · MySQL 8  
> **Base URL:** `http://localhost:8080/api`  
> **Phiên bản API:** v1

---

## Mục lục

1. [Quy ước chung](#1-quy-ước-chung)
2. [Xác thực và phân quyền](#2-xác-thực-và-phân-quyền)
3. [Danh sách endpoint](#3-danh-sách-endpoint)
4. [Chi tiết từng endpoint](#4-chi-tiết-từng-endpoint)
5. [Mã lỗi nghiệp vụ](#5-mã-lỗi-nghiệp-vụ)
6. [Bảng ánh xạ API – FR – UC](#6-bảng-ánh-xạ-api--fr--uc)

---

## 1. Quy ước chung

### 1.1. Định dạng dữ liệu

- Request/Response body: **JSON** (`Content-Type: application/json`).
- Mã hóa ký tự: **UTF-8**.
- Ngày giờ: chuẩn **ISO 8601** — `yyyy-MM-dd'T'HH:mm:ss` (múi giờ UTC+7 theo server).
- Số nguyên (quantity, threshold): kiểu `integer`.

### 1.2. Cấu trúc phản hồi thành công

```json
{
  "success": true,
  "message": "Mô tả ngắn gọn kết quả",
  "data": { }
}
```

### 1.3. Cấu trúc phản hồi lỗi

```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR_CODE",
    "message": "Thông báo lỗi chi tiết cho người dùng"
  }
}
```

### 1.4. Phân trang (Pagination)

Các endpoint trả về danh sách hỗ trợ phân trang qua query parameter:

| Parameter | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `page` | integer | `0` | Trang hiện tại (đánh số từ 0) |
| `size` | integer | `20` | Số phần tử mỗi trang (tối đa 100) |
| `sort` | string | *(tùy endpoint)* | Cột sắp xếp, thêm `,desc` để đảo chiều |

Phản hồi phân trang:

```json
{
  "success": true,
  "data": {
    "content": [ ],
    "page": 0,
    "size": 20,
    "totalElements": 180,
    "totalPages": 9
  }
}
```

### 1.5. HTTP Status Code sử dụng

| Code | Ý nghĩa | Khi nào dùng |
|:---:|---|---|
| **200** | OK | Truy vấn thành công, cập nhật thành công |
| **201** | Created | Tạo mới giao dịch nhập/xuất kho thành công |
| **400** | Bad Request | Dữ liệu đầu vào không hợp lệ (số lượng ≤ 0, mã không tồn tại…) |
| **401** | Unauthorized | Chưa đăng nhập hoặc token hết hạn |
| **403** | Forbidden | Không đủ quyền (sai vai trò hoặc truy cập dữ liệu trung tâm khác) |
| **404** | Not Found | Resource không tồn tại |
| **409** | Conflict | Xuất vượt tồn kho hoặc tranh chấp đồng thời |
| **500** | Internal Server Error | Lỗi hệ thống không mong đợi |

---

## 2. Xác thực và phân quyền

### 2.1. Đăng nhập (lấy JWT token)

Tất cả API (trừ `/api/auth/login` và `/api/health`) yêu cầu xác thực bằng **JWT Bearer Token** (NFR-03).

**Header bắt buộc:**

```
Authorization: Bearer <jwt_token>
```

### 2.2. Ma trận phân quyền theo vai trò

Token chứa thông tin `role` và `service_center_id`. Hệ thống kiểm tra quyền theo bảng dưới (QT-14, NFR-03):

| Endpoint | Kỹ thuật viên | Quản lý trung tâm |
|---|:---:|:---:|
| `GET /api/parts/stock` | ✅ | ✅ |
| `POST /api/transactions/import` | ❌ (403) | ✅ |
| `POST /api/transactions/export` | ✅ | ❌ (403) |
| `GET /api/parts/stock/low-stock` | ❌ (403) | ✅ |
| `GET /api/transactions` | ❌ (403) | ✅ |
| `GET /api/transactions/by-ticket/{ticketId}` | ❌ (403) | ✅ |
| `PATCH /api/parts/stock/{partStockId}/threshold` | ❌ (403) | ✅ |

> **Quy tắc QT-14:** Mọi truy vấn tự động lọc theo `service_center_id` của người dùng đang đăng nhập. Truy cập dữ liệu trung tâm khác bị từ chối HTTP 403.

---

## 3. Danh sách endpoint

| # | Method | Endpoint | Mô tả | FR | UC |
|:---:|:---:|---|---|:---:|:---:|
| 1 | `POST` | `/api/auth/login` | Đăng nhập, lấy JWT token | — | — |
| 2 | `GET` | `/api/health` | Kiểm tra hệ thống hoạt động | — | — |
| 3 | `GET` | `/api/parts/stock` | Xem tồn kho linh kiện tại trung tâm | FR-01 | UC-01 |
| 4 | `POST` | `/api/transactions/import` | Ghi nhận nhập kho linh kiện | FR-02 | UC-02 |
| 5 | `POST` | `/api/transactions/export` | Xuất linh kiện cho phiếu bảo hành | FR-03, FR-04 | UC-03, UC-04 |
| 6 | `GET` | `/api/parts/stock/low-stock` | Xem danh sách linh kiện dưới ngưỡng tối thiểu | FR-05 | UC-05 |
| 7 | `GET` | `/api/transactions` | Xem lịch sử giao dịch linh kiện (lọc theo linh kiện, khoảng ngày) | FR-06 | UC-06 |
| 8 | `GET` | `/api/transactions/by-ticket/{ticketId}` | Xem linh kiện đã xuất theo phiếu bảo hành | FR-06 | UC-06 |
| 9 | `PATCH` | `/api/parts/stock/{partStockId}/threshold` | Thiết lập ngưỡng tối thiểu cho linh kiện | FR-07 | UC-07 |

---

## 4. Chi tiết từng endpoint

---

### 4.1. `POST /api/auth/login`

> Đăng nhập hệ thống, nhận JWT token.

**Request Body:**

```json
{
  "username": "ktv.nguyenvana",
  "password": "********"
}
```

**Response `200 OK`:**

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 12,
      "username": "ktv.nguyenvana",
      "fullName": "Nguyễn Văn A",
      "role": "TECHNICIAN",
      "serviceCenterId": 3,
      "serviceCenterName": "BH Quận 10"
    }
  }
}
```

**Response `401 Unauthorized`:**

```json
{
  "success": false,
  "error": {
    "code": "AUTH_INVALID_CREDENTIALS",
    "message": "Tên đăng nhập hoặc mật khẩu không đúng"
  }
}
```

---

### 4.2. `GET /api/health`

> Kiểm tra hệ thống đang hoạt động. Không yêu cầu xác thực.

**Response `200 OK`:**

```json
{
  "success": true,
  "message": "Hệ thống đang hoạt động",
  "data": {
    "status": "UP",
    "database": "CONNECTED",
    "timestamp": "2026-10-03T09:00:00"
  }
}
```

---

### 4.3. `GET /api/parts/stock` — Xem tồn kho linh kiện

> **FR-01 / UC-01 / US-01** — Hiển thị danh sách linh kiện của trung tâm người dùng đang đăng nhập.  
> **Quyền:** Kỹ thuật viên ✅ · Quản lý trung tâm ✅

**Query Parameters:**

| Parameter | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `keyword` | string | Không | Tìm kiếm theo mã hoặc tên linh kiện (AC-1.2) |
| `page` | integer | Không | Trang hiện tại (mặc định: 0) |
| `size` | integer | Không | Số phần tử/trang (mặc định: 20) |
| `sort` | string | Không | Sắp xếp, ví dụ: `partName,asc` |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "partStockId": 101,
        "partId": "LCD-IP15",
        "partName": "Màn hình LCD iPhone 15",
        "quantity": 12,
        "minThreshold": 5,
        "isLowStock": false,
        "serviceCenterId": 3,
        "serviceCenterName": "BH Quận 10"
      },
      {
        "partStockId": 102,
        "partId": "PIN-SS-S24",
        "partName": "Pin Samsung Galaxy S24",
        "quantity": 3,
        "minThreshold": 5,
        "isLowStock": true,
        "serviceCenterId": 3,
        "serviceCenterName": "BH Quận 10"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 180,
    "totalPages": 9
  }
}
```

> **Ghi chú:** Hệ thống tự động lọc theo `service_center_id` của người dùng đang đăng nhập (QT-14). Trường `isLowStock` = `true` khi `quantity < minThreshold` (FR-05).

---

### 4.4. `POST /api/transactions/import` — Ghi nhận nhập kho

> **FR-02 / UC-02 / US-02** — Quản lý trung tâm ghi nhận nhập kho linh kiện, cập nhật tồn kho tức thời.  
> **Quyền:** Quản lý trung tâm ✅ · Kỹ thuật viên ❌  
> **Quy tắc:** QT-L5-01, QT-13, NFR-04

**Request Body:**

```json
{
  "partId": "LCD-IP15",
  "quantity": 10,
  "note": "Nhận từ NCC ABC, chứng từ GH-2026-0891"
}
```

| Trường | Kiểu | Bắt buộc | Ràng buộc |
|---|---|:---:|---|
| `partId` | string | ✅ | Mã linh kiện phải tồn tại trong danh mục (AC-2.3) |
| `quantity` | integer | ✅ | Số nguyên > 0 (QT-L5-01, AC-2.2) |
| `note` | string | Không | Ghi chú xuất xứ / chứng từ giao hàng |

**Response `201 Created`:**

```json
{
  "success": true,
  "message": "Ghi nhận nhập kho thành công cho linh kiện Màn hình LCD iPhone 15",
  "data": {
    "transaction": {
      "transactionId": 5024,
      "transactionType": "IMPORT",
      "partId": "LCD-IP15",
      "partName": "Màn hình LCD iPhone 15",
      "quantity": 10,
      "performedBy": "ql.tranthib",
      "performedByName": "Trần Thị B",
      "serviceCenterId": 3,
      "serviceCenterName": "BH Quận 10",
      "note": "Nhận từ NCC ABC, chứng từ GH-2026-0891",
      "createdAt": "2026-10-03T09:15:30"
    },
    "stockAfter": {
      "partId": "LCD-IP15",
      "quantity": 15,
      "minThreshold": 5,
      "isLowStock": false
    }
  }
}
```

**Response `400 Bad Request` — Số lượng không hợp lệ (AC-2.2):**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_QUANTITY",
    "message": "Số lượng nhập phải lớn hơn 0"
  }
}
```

**Response `400 Bad Request` — Mã linh kiện không tồn tại (AC-2.3):**

```json
{
  "success": false,
  "error": {
    "code": "PART_NOT_FOUND",
    "message": "Mã linh kiện không tồn tại trong danh mục"
  }
}
```

---

### 4.5. `POST /api/transactions/export` — Xuất linh kiện cho phiếu bảo hành

> **FR-03 + FR-04 / UC-03 + UC-04 / US-03 + US-04** — Kỹ thuật viên xuất linh kiện gắn với phiếu bảo hành. Hệ thống tự động kiểm tra tồn kho trước khi xuất (UC-04 `«include»`).  
> **Quyền:** Kỹ thuật viên ✅ · Quản lý trung tâm ❌  
> **Quy tắc:** QT-06, QT-09, QT-13, QT-14, QT-L5-02, QT-L5-03, NFR-02

**Request Body:**

```json
{
  "partId": "PIN-SS-S24",
  "quantity": 1,
  "ticketId": "BH-000456/2026"
}
```

| Trường | Kiểu | Bắt buộc | Ràng buộc |
|---|---|:---:|---|
| `partId` | string | ✅ | Mã linh kiện phải tồn tại trong danh mục |
| `quantity` | integer | ✅ | Số nguyên > 0 (QT-L5-02) |
| `ticketId` | string | ✅ | Mã phiếu bảo hành phải tồn tại, cùng trung tâm, trạng thái **Đang xử lý** hoặc **Chờ linh kiện** (QT-06, QT-L5-02) |

**Response `201 Created` (AC-3.1, AC-4.2):**

```json
{
  "success": true,
  "message": "Xuất linh kiện thành công cho phiếu BH-000456/2026",
  "data": {
    "transaction": {
      "transactionId": 5025,
      "transactionType": "EXPORT",
      "partId": "PIN-SS-S24",
      "partName": "Pin Samsung Galaxy S24",
      "quantity": 1,
      "ticketId": "BH-000456/2026",
      "performedBy": "ktv.nguyenvana",
      "performedByName": "Nguyễn Văn A",
      "serviceCenterId": 3,
      "serviceCenterName": "BH Quận 10",
      "createdAt": "2026-10-03T10:22:15"
    },
    "stockAfter": {
      "partId": "PIN-SS-S24",
      "quantity": 7,
      "minThreshold": 5,
      "isLowStock": false
    }
  }
}
```

**Response `409 Conflict` — Xuất vượt tồn kho (AC-4.1, AC-4.3):**

```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "Số lượng xuất (3) vượt quá tồn kho hiện tại (2). Giao dịch bị hủy."
  }
}
```

**Response `400 Bad Request` — Phiếu bảo hành không hợp lệ (AC-3.2):**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_TICKET_STATUS",
    "message": "Không thể xuất linh kiện cho phiếu đã đóng"
  }
}
```

**Response `403 Forbidden` — Phiếu không thuộc trung tâm (AC-3.3):**

```json
{
  "success": false,
  "error": {
    "code": "TICKET_CENTER_MISMATCH",
    "message": "Phiếu không thuộc trung tâm của bạn"
  }
}
```

> **Ghi chú về NFR-02 (Concurrency):** Endpoint này sử dụng cơ chế khóa bi quan (pessimistic lock) hoặc `SELECT ... FOR UPDATE` trên bản ghi `part_stock` để đảm bảo: với 50 request đồng thời xuất linh kiện có tồn = 10, chính xác 10 request thành công và 40 bị từ chối `409 Conflict`, tồn cuối = 0, số lần tồn âm = 0.

---

### 4.6. `GET /api/parts/stock/low-stock` — Xem cảnh báo tồn thấp

> **FR-05 / UC-05 / US-05** — Danh sách linh kiện có tồn kho dưới ngưỡng tối thiểu tại trung tâm.  
> **Quyền:** Quản lý trung tâm ✅ · Kỹ thuật viên ❌  
> **Quy tắc:** QT-09

**Query Parameters:**

| Parameter | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `page` | integer | Không | Trang hiện tại (mặc định: 0) |
| `size` | integer | Không | Số phần tử/trang (mặc định: 20) |

**Response `200 OK` (AC-5.1, AC-5.2):**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "partStockId": 102,
        "partId": "PIN-SS-S24",
        "partName": "Pin Samsung Galaxy S24",
        "quantity": 3,
        "minThreshold": 5,
        "deficit": 2,
        "serviceCenterId": 3,
        "serviceCenterName": "BH Quận 10"
      },
      {
        "partStockId": 108,
        "partId": "SAC-IP15",
        "partName": "Sạc iPhone 15 chính hãng",
        "quantity": 4,
        "minThreshold": 5,
        "deficit": 1,
        "serviceCenterId": 3,
        "serviceCenterName": "BH Quận 10"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

> **Ghi chú:** Trường `deficit` = `minThreshold - quantity` cho biết cần bổ sung thêm bao nhiêu. Danh sách tự động cập nhật sau mỗi giao dịch nhập/xuất kho và khi đổi ngưỡng (AC-5.3).

---

### 4.7. `GET /api/transactions` — Xem lịch sử giao dịch linh kiện

> **FR-06 / UC-06 / US-06** — Quản lý trung tâm xem lịch sử giao dịch linh kiện, lọc theo linh kiện hoặc khoảng ngày.  
> **Quyền:** Quản lý trung tâm ✅ · Kỹ thuật viên ❌  
> **Quy tắc:** QT-14, NFR-04

**Query Parameters:**

| Parameter | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `partId` | string | Không | Lọc theo mã linh kiện (AC-6.1) |
| `type` | string | Không | Lọc theo loại: `IMPORT` hoặc `EXPORT` |
| `dateFrom` | string | Không | Ngày bắt đầu, định dạng `yyyy-MM-dd` (AC-6.1) |
| `dateTo` | string | Không | Ngày kết thúc, định dạng `yyyy-MM-dd` (AC-6.1) |
| `page` | integer | Không | Trang hiện tại (mặc định: 0) |
| `size` | integer | Không | Số phần tử/trang (mặc định: 20) |
| `sort` | string | Không | Mặc định: `createdAt,desc` |

**Ví dụ request:**

```
GET /api/transactions?partId=LCD-IP15&dateFrom=2026-09-01&dateTo=2026-09-30&page=0&size=20
```

**Response `200 OK` (AC-6.1):**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "transactionId": 5020,
        "transactionType": "EXPORT",
        "partId": "LCD-IP15",
        "partName": "Màn hình LCD iPhone 15",
        "quantity": 1,
        "ticketId": "BH-000456/2026",
        "performedBy": "ktv.nguyenvana",
        "performedByName": "Nguyễn Văn A",
        "createdAt": "2026-09-28T14:30:00"
      },
      {
        "transactionId": 5010,
        "transactionType": "IMPORT",
        "partId": "LCD-IP15",
        "partName": "Màn hình LCD iPhone 15",
        "quantity": 20,
        "ticketId": null,
        "performedBy": "ql.tranthib",
        "performedByName": "Trần Thị B",
        "createdAt": "2026-09-15T08:45:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

**Response `200 OK` — Không có giao dịch (AC-6.2):**

```json
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

---

### 4.8. `GET /api/transactions/by-ticket/{ticketId}` — Xem linh kiện đã xuất theo phiếu

> **FR-06 / UC-06 / US-07** — Quản lý xem tất cả linh kiện đã xuất cho một phiếu bảo hành cụ thể.  
> **Quyền:** Quản lý trung tâm ✅ · Kỹ thuật viên ❌  
> **Quy tắc:** QT-14

**Path Parameter:**

| Parameter | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `ticketId` | string | ✅ | Mã phiếu bảo hành cần tra cứu |

**Ví dụ request:**

```
GET /api/transactions/by-ticket/BH-000456%2F2026
```

**Response `200 OK` (AC-7.1):**

```json
{
  "success": true,
  "data": {
    "ticketId": "BH-000456/2026",
    "totalPartsUsed": 2,
    "parts": [
      {
        "transactionId": 5020,
        "partId": "LCD-IP15",
        "partName": "Màn hình LCD iPhone 15",
        "quantity": 1,
        "performedBy": "ktv.nguyenvana",
        "performedByName": "Nguyễn Văn A",
        "createdAt": "2026-09-28T14:30:00"
      },
      {
        "transactionId": 5022,
        "partId": "PIN-IP15",
        "partName": "Pin iPhone 15",
        "quantity": 1,
        "performedBy": "ktv.nguyenvana",
        "performedByName": "Nguyễn Văn A",
        "createdAt": "2026-09-28T14:35:00"
      }
    ]
  }
}
```

**Response `200 OK` — Phiếu chưa có linh kiện (AC-7.2):**

```json
{
  "success": true,
  "data": {
    "ticketId": "BH-000999/2026",
    "totalPartsUsed": 0,
    "parts": []
  }
}
```

---

### 4.9. `PATCH /api/parts/stock/{partStockId}/threshold` — Thiết lập ngưỡng tối thiểu

> **FR-07 / UC-07 / US-08** — Quản lý trung tâm đặt ngưỡng tối thiểu cho một linh kiện tại trung tâm mình.  
> **Quyền:** Quản lý trung tâm ✅ · Kỹ thuật viên ❌

**Path Parameter:**

| Parameter | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `partStockId` | integer | ✅ | ID bản ghi tồn kho linh kiện |

**Request Body:**

```json
{
  "minThreshold": 5
}
```

| Trường | Kiểu | Bắt buộc | Ràng buộc |
|---|---|:---:|---|
| `minThreshold` | integer | ✅ | Số nguyên ≥ 0 (AC-8.2) |

**Response `200 OK` (AC-8.1):**

```json
{
  "success": true,
  "message": "Cập nhật ngưỡng tối thiểu thành công",
  "data": {
    "partStockId": 101,
    "partId": "LCD-IP15",
    "partName": "Màn hình LCD iPhone 15",
    "quantity": 4,
    "minThreshold": 5,
    "isLowStock": true,
    "serviceCenterId": 3,
    "serviceCenterName": "BH Quận 10"
  }
}
```

> **Ghi chú:** Sau khi lưu, hệ thống tự động so sánh tồn hiện tại với ngưỡng mới. Nếu `quantity < minThreshold` mới → `isLowStock = true` (cảnh báo xuất hiện). Nếu `quantity ≥ minThreshold` mới → `isLowStock = false` (cảnh báo tắt).

**Response `400 Bad Request` — Ngưỡng âm (AC-8.2):**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_THRESHOLD",
    "message": "Ngưỡng tối thiểu phải là số nguyên ≥ 0"
  }
}
```

---

## 5. Mã lỗi nghiệp vụ

Tổng hợp tất cả mã lỗi nghiệp vụ (`error.code`) mà API có thể trả về:

| Mã lỗi | HTTP | Mô tả | Quy tắc / AC liên quan |
|---|:---:|---|---|
| `AUTH_INVALID_CREDENTIALS` | 401 | Tên đăng nhập hoặc mật khẩu không đúng | NFR-03 |
| `AUTH_TOKEN_EXPIRED` | 401 | Token JWT đã hết hạn | NFR-03 |
| `ACCESS_DENIED` | 403 | Vai trò không có quyền truy cập endpoint | NFR-03, QT-14 |
| `CENTER_MISMATCH` | 403 | Truy cập dữ liệu trung tâm khác | QT-14, AC-1.3 |
| `PART_NOT_FOUND` | 400 | Mã linh kiện không tồn tại trong danh mục | AC-2.3 |
| `INVALID_QUANTITY` | 400 | Số lượng nhập/xuất ≤ 0 hoặc không phải số nguyên | QT-L5-01, QT-L5-02, AC-2.2 |
| `INSUFFICIENT_STOCK` | 409 | Số lượng xuất vượt tồn kho hiện tại | QT-09, QT-L5-03, AC-4.1, AC-4.3 |
| `INVALID_TICKET_STATUS` | 400 | Phiếu bảo hành không ở trạng thái hợp lệ | QT-06, QT-L5-02, AC-3.2 |
| `TICKET_NOT_FOUND` | 404 | Mã phiếu bảo hành không tồn tại | QT-L5-02 |
| `TICKET_CENTER_MISMATCH` | 403 | Phiếu bảo hành không thuộc trung tâm của người dùng | QT-14, AC-3.3 |
| `INVALID_THRESHOLD` | 400 | Ngưỡng tối thiểu < 0 | AC-8.2 |
| `PART_STOCK_NOT_FOUND` | 404 | Không tìm thấy bản ghi tồn kho linh kiện | — |

---

## 6. Bảng ánh xạ API – FR – UC

Bảng truy vết đảm bảo mọi yêu cầu chức năng đều có endpoint tương ứng:

| Mã FR | Tên yêu cầu | Endpoint | UC |
|:---:|---|---|:---:|
| **FR-01** | Xem tồn kho linh kiện | `GET /api/parts/stock` | UC-01 |
| **FR-02** | Ghi nhận nhập kho | `POST /api/transactions/import` | UC-02 |
| **FR-03** | Xuất linh kiện cho phiếu bảo hành | `POST /api/transactions/export` | UC-03 |
| **FR-04** | Chặn xuất vượt tồn kho | `POST /api/transactions/export` *(logic kiểm tra tích hợp)* | UC-04 |
| **FR-05** | Cảnh báo tồn kho thấp | `GET /api/parts/stock/low-stock` + trường `isLowStock` trong `GET /api/parts/stock` | UC-05 |
| **FR-06** | Xem lịch sử giao dịch linh kiện | `GET /api/transactions` + `GET /api/transactions/by-ticket/{ticketId}` | UC-06 |
| **FR-07** | Thiết lập ngưỡng tối thiểu | `PATCH /api/parts/stock/{partStockId}/threshold` | UC-07 |

---

*API Contract – Kho linh kiện thay thế (L5) – Smart CRM Mekong Mobile.*
