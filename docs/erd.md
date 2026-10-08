# Thành phần 4: Mô hình Dữ liệu theo Chuyên ngành (Track SE) – Luồng L5

> **Học phần:** Chuyên đề tốt nghiệp 1 – Học kỳ 1, Năm học 2026 – 2027  
> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525 – Track SE  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế (Hệ thống Smart CRM Mekong Mobile)  
> **File sơ đồ gốc:** [`docs/erd.drawio`](erd.drawio) (Vẽ bằng draw.io, ký pháp chân chim Crow's Foot)  
> **File mã nguồn DDL:** [`docs/schema.sql`](schema.sql) (Cài đặt trên hệ quản trị CSDL quan hệ MySQL 8.x / InnoDB)

---

## 1. Tổng quan Mô hình Dữ liệu Luồng L5

Mô hình dữ liệu của module **L5 – Kho linh kiện thay thế** được thiết kế chuẩn hóa ở **Dạng chuẩn 3 (3NF)**, bao gồm đúng **5 bảng cốt lõi** phục vụ toàn bộ chu trình quản lý tồn kho, giao dịch nhập kho, xuất kho theo phiếu bảo hành, cảnh báo tồn thấp và tra cứu lịch sử:

1. **`service_centers`**: Danh mục các trung tâm bảo hành của Mekong Mobile (phân vùng dữ liệu theo **QT-14**).
2. **`parts`**: Danh mục thông tin chuẩn hóa của các loại linh kiện thay thế (**FR-01**, **US-01**).
3. **`part_stock`**: Quản lý số lượng tồn kho và ngưỡng cảnh báo tối thiểu của từng linh kiện tại từng trung tâm (**QT-09**, **NFR-02**, **US-05**, **US-08**).
4. **`tickets`**: Phiếu bảo hành/sửa chữa thiết bị (dữ liệu chia sẻ từ luồng L2 theo quy tắc **RB-03**, **QT-06**).
5. **`part_transactions`**: Bảng nhật ký ghi vết bất biến (Append-Only Log) lưu trữ lịch sử nhập/xuất kho (**QT-13**, **NFR-04**, **US-06**, **US-07**).

---

## 2. Sơ đồ Thực thể Mối quan hệ (ERD – Crow's Foot Notation)

Sơ đồ tuân thủ quy ước chân chim (*crow's foot*): `||` (đúng một), `o|` (không hoặc một), `|<` (một hoặc nhiều), `o<` (không hoặc nhiều).

```text
+-----------------------+                    +------------------------------------+
|    service_centers    |                    |             part_stock             |
+-----------------------+                    +------------------------------------+
| PK center_id          | ||--------------o< | PK stock_id                        |
| UQ center_code        |                    | FK center_id  NOT NULL             |
|    center_name        |                    | FK part_id    NOT NULL             |
|    address            |                    |    quantity   NOT NULL (CHECK >= 0)|
|    phone              |         +-------o< |    min_threshold NOT NULL          |
|    is_active          |         |          |    updated_at                      |
|    created_at         |         |          +------------------------------------+
+-----------------------+         |                             |
       ||                         |                             |
       ||                         |                             |
       |  ||                      |                             |
       |                          |                             |
       o<                         |                             |
+-----------------------+         |          +------------------------------------+
|        tickets        |         |          |               parts                |
+-----------------------+         |          +------------------------------------+
| PK ticket_id          |         |          | PK part_id                         |
| UQ ticket_code        |         |          | UQ part_code                       |
| FK center_id  NOT NULL|         |          |    part_name                       |
|    status             |         |          |    category                        |
|    customer_name      |         |          |    unit                            |
|    device_model       |         |          |    unit_price (CHECK >= 0)         |
|    created_at         |         |          |    is_active                       |
+-----------------------+         |          |    created_at                      |
       |                          |          +------------------------------------+
       |  o|                      |                             ||
       |                          |                             ||
       o<                         |                             |  ||
+------------------------------------+                          |
|         part_transactions          |                          |
+------------------------------------+                          |
| PK transaction_id                  |                          |
| UQ transaction_code                |                          |
| FK center_id   NOT NULL            | <------------------------+
| FK part_id     NOT NULL            |
| FK ticket_id   NULL được           |
|    transaction_type ('NHAP'/'XUAT')|
|    quantity    NOT NULL (CHECK > 0)|
|    performed_by NOT NULL           |
|    note                            |
|    created_at  (Bất biến, NFR-04)  |
+------------------------------------+
```

### Chú thích các Quan hệ (Cardinality):
* **`service_centers` $\rightarrow$ `part_stock` (1 : N, `||--o<`):** Một trung tâm quản lý số tồn của nhiều linh kiện; mỗi dòng tồn kho thuộc đúng một trung tâm.
* **`parts` $\rightarrow$ `part_stock` (1 : N, `||--o<`):** Một linh kiện có thể tồn tại ở nhiều trung tâm bảo hành; mỗi dòng tồn kho ứng với đúng một linh kiện.
* **`service_centers` $\rightarrow$ `tickets` (1 : N, `||--o<`):** Một trung tâm tiếp nhận xử lý nhiều phiếu bảo hành.
* **`service_centers` $\rightarrow$ `part_transactions` (1 : N, `||--o<`):** Một trung tâm phát sinh nhiều giao dịch nhập/xuất linh kiện.
* **`parts` $\rightarrow$ `part_transactions` (1 : N, `||--o<`):** Một linh kiện phát sinh nhiều lần nhập/xuất kho theo thời gian.
* **`tickets` $\rightarrow$ `part_transactions` (0..1 : N, `o|--o<`):** Một phiếu bảo hành có thể được xuất một hoặc nhiều linh kiện sửa chữa; mỗi giao dịch xuất kho gắn với đúng một phiếu bảo hành (với giao dịch nhập kho, `ticket_id` để `NULL`).

---

## 3. Đặc tả Chi tiết các Bảng Dữ liệu (Data Dictionary)

### 3.1. Bảng `service_centers` (Trung tâm bảo hành)
*Phục vụ: QT-14 (phân vùng tồn kho theo trung tâm), NFR-03 (cô lập dữ liệu giữa các chi nhánh).*

| Tên cột | Kiểu dữ liệu | Khóa | Nullable | Mặc định | Ràng buộc / Ý nghĩa nghiệp vụ |
|---|---|:---:|:---:|:---:|---|
| `center_id` | `BIGINT` | **PK** | Không | Auto Inc | Khóa chính nhân tạo tự tăng (Surrogate Key) |
| `center_code` | `VARCHAR(20)` | **UQ** | Không | Không | Mã trung tâm (khóa tự nhiên: TT-CT1, TT-Q10) |
| `center_name` | `VARCHAR(120)` | | Không | Không | Tên trung tâm bảo hành Mekong Mobile |
| `address` | `VARCHAR(255)` | | Không | Không | Địa chỉ thực tế của trung tâm |
| `phone` | `VARCHAR(20)` | | Không | Không | Số điện thoại liên hệ |
| `is_active` | `BOOLEAN` | | Không | `TRUE` | Trạng thái hoạt động của trung tâm |
| `created_at` | `DATETIME` | | Không | `CURRENT_TIMESTAMP` | Thời điểm tạo bản ghi trên hệ thống |

---

### 3.2. Bảng `parts` (Danh mục linh kiện thay thế)
*Phục vụ: FR-01, FR-02, US-01, AC-2.3 (kiểm tra linh kiện tồn tại trong danh mục).*

| Tên cột | Kiểu dữ liệu | Khóa | Nullable | Mặc định | Ràng buộc / Ý nghĩa nghiệp vụ |
|---|---|:---:|:---:|:---:|---|
| `part_id` | `BIGINT` | **PK** | Không | Auto Inc | Khóa chính nhân tạo tự tăng |
| `part_code` | `VARCHAR(30)` | **UQ** | Không | Không | Mã định danh linh kiện (PIN-IP15, LCD-IP15...) (**QT-01**) |
| `part_name` | `VARCHAR(150)` | | Không | Không | Tên diễn giải linh kiện |
| `category` | `VARCHAR(50)` | | Không | Không | Phân loại: PIN, MAN_HINH, CAMERA, MAINBOARD... |
| `unit` | `VARCHAR(20)` | | Không | `'Cái'` | Đơn vị đo lường linh kiện |
| `unit_price` | `DECIMAL(12,2)`| | Không | `0.00` | `CHECK (unit_price >= 0)` – Đơn giá linh kiện |
| `is_active` | `BOOLEAN` | | Không | `TRUE` | Trạng thái còn sử dụng hay ngừng nhập |
| `created_at` | `DATETIME` | | Không | `CURRENT_TIMESTAMP` | Thời điểm khai báo linh kiện |

---

### 3.3. Bảng `part_stock` (Tồn kho linh kiện theo trung tâm)
*Phục vụ: FR-01, FR-03, FR-04, FR-07, US-01, US-04, US-05, US-08, QT-09, NFR-02.*

| Tên cột | Kiểu dữ liệu | Khóa | Nullable | Mặc định | Ràng buộc / Ý nghĩa nghiệp vụ |
|---|---|:---:|:---:|:---:|---|
| `stock_id` | `BIGINT` | **PK** | Không | Auto Inc | Khóa chính nhân tạo tự tăng |
| `center_id` | `BIGINT` | **FK** | Không | Không | Tham chiếu `service_centers(center_id)` |
| `part_id` | `BIGINT` | **FK** | Không | Không | Tham chiếu `parts(part_id)` |
| `quantity` | `INT` | | Không | `0` | **`CHECK (quantity >= 0)`**: **CHẶN ÂM KHO** ở tầng CSDL (**QT-09**, **NFR-02**) |
| `min_threshold`| `INT` | | Không | `5` | `CHECK (min_threshold >= 0)`: Ngưỡng cảnh báo tồn thấp (**US-05**, **US-08**) |
| `updated_at` | `DATETIME` | | Không | `CURRENT_TIMESTAMP` | Tự động cập nhật khi có giao dịch nhập/xuất |

> **Ràng buộc duy nhất:** `UNIQUE (center_id, part_id)` đảm bảo mỗi linh kiện tại một trung tâm chỉ có duy nhất 1 bản ghi số tồn kho.

---

### 3.4. Bảng `tickets` (Phiếu bảo hành – Dữ liệu chia sẻ từ Luồng L2)
*Phục vụ: FR-02, US-03, RB-03, QT-06 (kiểm tra phiếu hợp lệ khi xuất kho).*

| Tên cột | Kiểu dữ liệu | Khóa | Nullable | Mặc định | Ràng buộc / Ý nghĩa nghiệp vụ |
|---|---|:---:|:---:|:---:|---|
| `ticket_id` | `BIGINT` | **PK** | Không | Auto Inc | Khóa chính nhân tạo tự tăng |
| `ticket_code` | `VARCHAR(30)` | **UQ** | Không | Không | Mã số phiếu bảo hành (BH-000456/2026) |
| `center_id` | `BIGINT` | **FK** | Không | Không | Tham chiếu `service_centers(center_id)` (kiểm tra cùng trung tâm **QT-14**) |
| `status` | `VARCHAR(30)` | | Không | `'TIEP_NHAN'` | `CHECK (status IN ('TIEP_NHAN', 'DANG_XU_LY', 'CHO_LINH_KIEN', 'HOAN_TAT', 'DA_DONG'))` |
| `customer_name`| `VARCHAR(120)` | | Không | Không | Họ tên khách hàng tiếp nhận |
| `device_model` | `VARCHAR(100)` | | Không | Không | Dòng máy tiếp nhận sửa chữa |
| `created_at` | `DATETIME` | | Không | `CURRENT_TIMESTAMP` | Thời điểm lập phiếu |

---

### 3.5. Bảng `part_transactions` (Lịch sử giao dịch nhập/xuất kho – Append-Only Log)
*Phục vụ: FR-02, FR-05, FR-06, US-02, US-03, US-06, US-07, QT-13, NFR-04.*

| Tên cột | Kiểu dữ liệu | Khóa | Nullable | Mặc định | Ràng buộc / Ý nghĩa nghiệp vụ |
|---|---|:---:|:---:|:---:|---|
| `transaction_id`| `BIGINT` | **PK** | Không | Auto Inc | Khóa chính nhân tạo tự tăng |
| `transaction_code`| `VARCHAR(30)`| **UQ** | Không | Không | Mã giao dịch tự sinh (GD-NK-..., GD-XK-...) |
| `center_id` | `BIGINT` | **FK** | Không | Không | Tham chiếu `service_centers(center_id)` |
| `part_id` | `BIGINT` | **FK** | Không | Không | Tham chiếu `parts(part_id)` |
| `ticket_id` | `BIGINT` | **FK** | Có | `NULL` | Tham chiếu `tickets(ticket_id)` (Bắt buộc khi type = 'XUAT', NULL khi 'NHAP') |
| `transaction_type`| `VARCHAR(10)`| | Không | Không | `CHECK (transaction_type IN ('NHAP', 'XUAT'))` (**QT-L5-01**, **QT-L5-02**) |
| `quantity` | `INT` | | Không | Không | **`CHECK (quantity > 0)`**: Số lượng giao dịch phải nguyên dương |
| `performed_by`| `VARCHAR(100)`| | Không | Không | Người thực hiện giao dịch (KTV/Quản lý kho) (**NFR-04**) |
| `note` | `VARCHAR(255)` | | Có | `NULL` | Diễn giải lý do hoặc chứng từ kèm theo |
| `created_at` | `DATETIME` | | Không | `CURRENT_TIMESTAMP` | **Thời điểm giao dịch bất biến, không xóa sửa** (**NFR-04**) |

---

## 4. Chứng minh Dạng chuẩn hóa (3NF Normalization)

Mô hình dữ liệu được thiết kế tuân thủ nghiêm ngặt 3 dạng chuẩn đầu tiên:

### 4.1. Dạng chuẩn 1 (1NF – First Normal Form)
* **Quy tắc:** Mỗi thuộc tính chỉ chứa giá trị nguyên tử (atomic value), không có nhóm lặp hoặc danh sách lồng ghép.
* **Chứng minh:**
  * Không lưu danh sách linh kiện trong một chuỗi text (ví dụ không lưu `"PIN-IP15: 2, LCD-IP15: 1"` trong bảng `tickets`).
  * Mọi thuộc tính số lượng (`quantity`), giá cả (`unit_price`), thời gian (`created_at`) đều mang một giá trị đơn duy nhất.

### 4.2. Dạng chuẩn 2 (2NF – Second Normal Form)
* **Quy tắc:** Đã đạt 1NF và mọi thuộc tính không khóa phải phụ thuộc hàm đầy đủ vào khóa chính (Full functional dependency), không phụ thuộc vào một phần của khóa ghép.
* **Chứng minh:**
  * Bảng `part_stock` sử dụng khóa chính nhân tạo đơn `stock_id`. Mọi thuộc tính như `quantity`, `min_threshold` phụ thuộc hoàn toàn vào cặp `(center_id, part_id)` đại diện bởi `stock_id`.
  * Bảng `part_transactions` sử dụng khóa chính đơn `transaction_id`. Mọi thuộc tính giao dịch phụ thuộc vào chính giao dịch đó mà không bị phụ thuộc bộ phận.

### 4.3. Dạng chuẩn 3 (3NF – Third Normal Form)
* **Quy tắc:** Đã đạt 2NF và không có thuộc tính không khóa nào phụ thuộc bắc cầu (Transitive dependency) vào khóa chính qua một thuộc tính không khóa khác.
* **Chứng minh:**
  * Trong bảng `part_stock`, không lưu `part_name`, `category` hay `center_name`. Khi cần lấy tên linh kiện, hệ thống nối qua `part_id`; đổi tên linh kiện chỉ cập nhật đúng 1 dòng ở bảng `parts`.
  * Trong bảng `part_transactions`, không lưu thông tin khách hàng hay thiết bị của phiếu bảo hành. Thông tin này được liên kết thông qua khóa ngoại `ticket_id`.
  * Không lưu trữ thuộc tính tính toán dư thừa: trạng thái cảnh báo tồn thấp không lưu thành cột `is_low_stock` trong bảng mà được suy diễn trực tiếp từ biểu thức logic `quantity < min_threshold` ở tầng Service.

---

## 5. Chiến lược Đánh Chỉ mục (INDEX) gắn với Yêu cầu Phi chức năng

Nhằm thỏa mãn các tiêu chí hiệu năng khắt khe trong SRS, hệ thống thiết kế 5 chỉ mục vật lý:

| Tên Index | Bảng áp dụng | Cột đánh Index | Phục vụ Yêu cầu (NFR / US) | Mục đích tối ưu & Đánh đổi |
|---|---|---|:---:|---|
| `idx_stock_center_part` | `part_stock` | `(center_id, part_id)` | **NFR-01**, **NFR-02** | **Tối ưu:** Tăng tốc truy vấn số tồn kho tức thì (< 500ms) và tối ưu lệnh khóa bi quan `SELECT ... FOR UPDATE` khi xuất kho.<br>**Đánh đổi:** Tăng dung lượng lưu trữ B-Tree index. |
| `idx_stock_alert` | `part_stock` | `(center_id, quantity, min_threshold)` | **US-05**, **FR-04** | **Tối ưu:** Quét nhanh danh sách linh kiện chạm ngưỡng cảnh báo tại trung tâm bảo hành mà không phải scan toàn bảng.<br>**Đánh đổi:** Chi phí cập nhật index mỗi khi số tồn thay đổi. |
| `idx_tx_center_created` | `part_transactions` | `(center_id, created_at)` | **NFR-01**, **US-06** | **Tối ưu:** Phục vụ màn hình Lịch sử giao dịch: lọc theo trung tâm và sắp xếp giảm dần theo thời gian (`ORDER BY created_at DESC LIMIT 20`). |
| `idx_tx_ticket` | `part_transactions` | `(ticket_id)` | **US-07**, **FR-06** | **Tối ưu:** Cho phép KTV mở tab "Linh kiện đã dùng" của phiếu bảo hành và load danh sách linh kiện ngay lập tức. |
| `idx_tx_part_created` | `part_transactions` | `(part_id, created_at)` | **US-06**, **FR-05** | **Tối ưu:** Hỗ trợ quản lý trung tâm lọc lịch sử biến động số tồn của 1 mã linh kiện cụ thể theo khoảng ngày. |

---

## 6. Giải quyết Triệt để 5 Lỗi ERD Phổ biến (Theo hướng dẫn Buổi 5)

| STT | Lỗi thường gặp | Giải pháp thiết kế trong Luồng L5 |
|:---:|---|---|
| **1** | **Bảng cô lập** (không quan hệ với phần còn lại) | **Không có bảng cô lập.** Cả 5 bảng đều có quan hệ khóa ngoại hai chiều chặt chẽ: `service_centers` và `parts` đóng vai trò danh mục trung tâm; `part_stock` và `part_transactions` là hai bảng giao dịch - tồn kho lõi; `tickets` liên kết trực tiếp qua giao dịch xuất kho. |
| **2** | **Thiếu bảng lịch sử** (ghi đè trạng thái) | Bảng `part_transactions` được thiết kế theo mô hình **Append-Only Log** bất biến (**NFR-04**). Mọi biến động tăng/giảm tồn kho đều sinh một dòng giao dịch mới kèm người thực hiện (`performed_by`) và thời điểm (`created_at`), không bao giờ sửa hoặc xóa vật lý. |
| **3** | **Lưu giá trị tính toán dư thừa** | Không lưu cột cờ `is_low_stock` hay `total_exported`. Trạng thái cảnh báo tồn thấp được tính động từ `quantity < min_threshold`. |
| **4** | **Không có Index ở các cột truy vấn nhiều** | Thiết lập 5 Index trên các khóa ngoại và cột lọc ngày giờ, trực tiếp giải quyết yêu cầu phản hồi $\le 500\text{ ms}$ cho 10.000 bản ghi của **NFR-01**. |
| **5** | **Dùng khóa tự nhiên làm Khóa chính** | Mọi bảng đều dùng Khóa nhân tạo tự tăng `BIGINT` (`center_id`, `part_id`, `stock_id`, `ticket_id`, `transaction_id`) làm PK. Các mã định danh nghiệp vụ (`part_code`, `ticket_code`, `center_code`) được đặt ràng buộc `UNIQUE`. |

---

## 7. Bảng Truy vết Ngược: Thực thể ↔ Yêu cầu Chức năng (Traceability)

Mọi bảng trong mô hình dữ liệu đều trả lời được câu hỏi: *"Bảng này phục vụ yêu cầu chức năng nào?"*

| Tên Bảng | Functional Requirements (FR) | User Stories (US) | Quy tắc nghiệp vụ (Business Rules) |
|---|---|---|---|
| `service_centers` | FR-01, FR-02, FR-05, FR-07 | US-01, US-02, US-03, US-06 | **QT-14** (phân vùng dữ liệu kho theo trung tâm bảo hành), **NFR-03** |
| `parts` | FR-01, FR-02, FR-05 | US-01, US-02, US-06 | **QT-01**, **AC-2.3** (xác thực mã linh kiện hợp lệ trong danh mục) |
| `part_stock` | FR-01, FR-03, FR-04, FR-07 | US-01, US-04, US-05, US-08 | **QT-09**, **QT-L5-03**, **NFR-02** (chặn xuất âm kho, kiểm tra số tồn) |
| `tickets` | FR-02, FR-06 | US-03, US-07 | **RB-03**, **QT-06** (xuất linh kiện phải gắn với phiếu BH hợp lệ cùng trung tâm) |
| `part_transactions` | FR-02, FR-05, FR-06 | US-02, US-03, US-06, US-07 | **QT-13**, **NFR-04** (ghi log giao dịch bất biến append-only) |

---

## 8. Trích xuất SQL DDL Skeleton (`docs/schema.sql`)

```sql
-- 1. BẢNG TRUNG TÂM BẢO HÀNH
CREATE TABLE service_centers (
    center_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    center_code   VARCHAR(20)  NOT NULL UNIQUE,
    center_name   VARCHAR(120) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG DANH MỤC LINH KIỆN
CREATE TABLE parts (
    part_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_code     VARCHAR(30)   NOT NULL UNIQUE,
    part_name     VARCHAR(150)  NOT NULL,
    category      VARCHAR(50)   NOT NULL,
    unit          VARCHAR(20)   NOT NULL DEFAULT 'Cái',
    unit_price    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_parts_price CHECK (unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG TỒN KHO LINH KIỆN
CREATE TABLE part_stock (
    stock_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    center_id     BIGINT NOT NULL,
    part_id       BIGINT NOT NULL,
    quantity      INT    NOT NULL DEFAULT 0,
    min_threshold INT    NOT NULL DEFAULT 5,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_center FOREIGN KEY (center_id) REFERENCES service_centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT fk_stock_part   FOREIGN KEY (part_id)   REFERENCES parts(part_id) ON DELETE RESTRICT,
    CONSTRAINT uk_stock_center_part UNIQUE (center_id, part_id),
    CONSTRAINT chk_stock_qty_positive CHECK (quantity >= 0),
    CONSTRAINT chk_stock_threshold_positive CHECK (min_threshold >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BẢNG PHIẾU BẢO HÀNH (DỮ LIỆU ĐỌC TỪ LUỒNG L2)
CREATE TABLE tickets (
    ticket_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_code   VARCHAR(30)  NOT NULL UNIQUE,
    center_id     BIGINT       NOT NULL,
    status        VARCHAR(30)  NOT NULL DEFAULT 'TIEP_NHAN',
    customer_name VARCHAR(120) NOT NULL,
    device_model  VARCHAR(100) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tickets_center FOREIGN KEY (center_id) REFERENCES service_centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT chk_tickets_status CHECK (status IN ('TIEP_NHAN', 'DANG_XU_LY', 'CHO_LINH_KIEN', 'HOAN_TAT', 'DA_DONG'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG LỊCH SỬ GIAO DỊCH KHO (APPEND-ONLY LOG)
CREATE TABLE part_transactions (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_code VARCHAR(30)  NOT NULL UNIQUE,
    center_id        BIGINT       NOT NULL,
    part_id          BIGINT       NOT NULL,
    ticket_id        BIGINT       NULL,
    transaction_type VARCHAR(10)  NOT NULL,
    quantity         INT          NOT NULL,
    performed_by     VARCHAR(100) NOT NULL,
    note             VARCHAR(255) NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_center FOREIGN KEY (center_id) REFERENCES service_centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_part   FOREIGN KEY (part_id)   REFERENCES parts(part_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE RESTRICT,
    CONSTRAINT chk_tx_type  CHECK (transaction_type IN ('NHAP', 'XUAT')),
    CONSTRAINT chk_tx_qty   CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- INDEX PHỤC VỤ HIỆU NĂNG VÀ TRA CỨU
CREATE INDEX idx_stock_center_part ON part_stock(center_id, part_id);
CREATE INDEX idx_stock_alert       ON part_stock(center_id, quantity, min_threshold);
CREATE INDEX idx_tx_center_created ON part_transactions(center_id, created_at);
CREATE INDEX idx_tx_ticket         ON part_transactions(ticket_id);
CREATE INDEX idx_tx_part_created   ON part_transactions(part_id, created_at);
```

---

## 9. Bảng Tự kiểm tra Tiêu chí Chấm điểm BT1 (Checklist Track SE)

| Tiêu chí kỹ thuật tối thiểu (Buổi 6) | Kết quả kiểm tra | Bằng chứng trong hồ sơ |
|---|:---:|---|
| **ERD có 4–6 bảng** | **ĐẠT (100%)** | Đúng 5 bảng (`service_centers`, `parts`, `part_stock`, `tickets`, `part_transactions`). |
| **Mỗi bảng có khóa chính được chỉ rõ** | **ĐẠT (100%)** | 100% bảng có PK nhân tạo tự tăng `BIGINT AUTO_INCREMENT`. |
| **Có $\ge 2$ quan hệ khóa ngoại** | **ĐẠT (100%)** | Có tổng cộng **6 quan hệ khóa ngoại** giữa 5 bảng. |
| **Không có bảng cô lập** | **ĐẠT (100%)** | Mọi bảng đều nối kết chặt chẽ vào chu trình nghiệp vụ quản lý kho. |
| **Có SQL DDL skeleton với đầy đủ ràng buộc** | **ĐẠT (100%)** | File [`docs/schema.sql`](schema.sql) có đầy đủ kiểu dữ liệu, `NOT NULL`, `UNIQUE`, `CHECK`, `FOREIGN KEY`. |
| **Có Index gắn với NFR hiệu năng** | **ĐẠT (100%)** | 5 Index chuyên biệt phục vụ tra cứu < 500ms (**NFR-01**) và chặn xuất âm (**NFR-02**). |
| **Khớp 100% với Sơ đồ Kiến trúc & SRS** | **ĐẠT (100%)** | Tên 5 bảng trùng khớp tuyệt đối với Lớp Lưu trữ trong [`docs/architecture.drawio`](architecture.drawio) và bảng thuật ngữ trong [`docs/srs.md`](srs.md). |
