# MEKONG MOBILE SMART CRM — PHÂN HỆ LUỒNG L5

> **Quản lý kho linh kiện thay thế tại 6 Trung tâm Bảo hành, kiểm soát xuất/nhập kho theo phiếu bảo hành, khóa bi quan chống xuất âm kho đồng thời, đối soát sổ cái giao dịch bất biến.**

- **Sinh viên thực hiện:** Phạm Trí Trọng
- **MSSV:** 2374802010525
- **Track chuyên ngành:** Kỹ thuật phần mềm (SE)
- **Học phần:** Chuyên đề tốt nghiệp 1 (HK1, 2026–2027) — Khoa CNTT, Đại học Văn Lang
- **Giảng viên hướng dẫn:** TS. Nguyễn Trí Hải
- **Tiến độ hiện tại:** Hoàn tất Giai đoạn 2 — **Buổi 12** (Nghiệm thu toàn bộ Module cốt lõi và Release `v0.2`)
- **Phiên bản release:** `v0.2` (Tag: `v0.2`)
- **Repository:** [https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5)

---

## 1. Tóm tắt hệ thống

Hệ thống Smart CRM — Luồng L5 giải quyết trọn vẹn bài toán quản lý kho và kiểm soát chuỗi cung ứng linh kiện thay thế tại 6 Trung tâm Bảo hành (TTBH) thuộc chuỗi Mekong Mobile trên toàn quốc. Phân hệ cung cấp giao diện trực quan cho Quản lý kho (QLK) theo dõi tồn khả dụng theo thời gian thực, thiết lập định mức an toàn tối thiểu (`min_threshold` theo QT-11, US-08), nhập kho linh kiện mới từ nhà cung cấp, và cấp phát linh kiện. Kỹ thuật viên (KTV) có thể tra cứu nhanh tồn kho tức thời trước khi nhận máy sửa chữa và lập phiếu xuất kho gắn chặt với mã phiếu bảo hành hợp lệ đang ở trạng thái xử lý (`DANG_XU_LY` theo QT-06).

Hệ thống ứng dụng kỹ thuật **Khóa bi quan (`SELECT ... FOR UPDATE` - QT-09 & NFR-02)** ở mức cô lập hàng CSDL (Row-Level Locking) kết hợp ràng buộc toàn vẹn `CHECK (quantity >= 0)` để triệt tiêu 100% rủi ro Race Condition dẫn đến xuất âm kho khi có nhiều yêu cầu đồng thời. Mọi biến động nhập/xuất đều được ghi nhận vào **Sổ cái giao dịch bất biến Append-Only (`part_transactions` theo NFR-04)** không cho phép sửa/xóa vật lý, đảm bảo phương trình đối soát toán học: $\text{Tồn thực tế} = \sum(\text{Nhập}) - \sum(\text{Xuất})$ luôn đúng tuyệt đối.

---

## 2. Công nghệ sử dụng

- **Frontend Prototype:** HTML5, CSS3 hiện đại, Vanilla JavaScript (ES6+), FontAwesome / Lucide Icons. Kiến trúc gọi API tách lớp riêng biệt ([`api.js`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/resources/static/js/api.js) và [`app.js`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/resources/static/js/app.js)), tích hợp đầy đủ xử lý tập trung 3 trạng thái giao diện: *Đang tải (Loading skeleton/spinner), Rỗng (Empty State có nút xóa bộ lọc), và Lỗi kết nối (Error State với banner cảnh báo và nút thử lại ngay)*.
- **Backend:** Java 17 LTS, Spring Boot 3.2.5, Spring Data JPA, Hibernate ORM, Flyway Migration. Kiến trúc phân tầng sạch (Clean Architecture) tách biệt nghiêm ngặt: `controller/` (REST API & DTO mapping) ↔ `service/` (100% nghiệp vụ thuần túy, hoàn toàn không phụ thuộc HTTP) ↔ `repository/` (Spring Data JPA truy xuất CSDL, hỗ trợ Khóa bi quan `PESSIMISTIC_WRITE`).
- **Cơ sở dữ liệu:** MySQL 8.0 (triển khai production/Docker) kết hợp H2 In-Memory Database (môi trường kiểm thử và chạy local độc lập). Lược đồ 5 bảng chuẩn 3NF: `service_centers`, `parts`, `tickets` (Read-only từ L2/L4), `part_stock`, `part_transactions`.
- **Kiểm thử tự động:** JUnit 5, Mockito, Spring Boot Test, H2 In-Memory. 17 kịch bản kiểm thử tự động bao phủ trọn vẹn 3 nhóm: Luồng đúng (Happy path), Giá trị biên (Boundary), Dữ liệu sai & Ràng buộc bảo mật (Invalid data & RBAC).
- **Đóng gói & Triển khai:** Docker Multi-stage build (JRE Eclipse Temurin 17 non-root user `mekonguser`), Docker Compose v2 ([`docker-compose.yml`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/docker-compose.yml) khởi chạy trọn gói Backend + Database bằng đúng 1 lệnh).

---

## 3. Khắc phục mâu thuẫn dữ liệu từ Bài tập 1

Theo góp ý đánh giá Bài tập 1 và yêu cầu kỹ thuật của Buổi 12 (ghi chép chi tiết tại [`docs/thay-doi-thiet-ke.md`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/docs/thay-doi-thiet-ke.md)), hệ thống đã chuẩn hóa và giải quyết triệt để 11 nội dung thay đổi theo cấu trúc 3 dòng bắt buộc (*Thay đổi gì · Vì sao · Ảnh hưởng tới thành phần nào*):

### Giai đoạn 1: Thay đổi sau phản hồi Bài tập 1 (Buổi 8)

1. **TD-01: Chuẩn hóa và đồng bộ tên kỹ thuật giữa Glossary, DDL, API Contract và Wireframe:**
   - *Thay đổi gì:* Đổi tên bảng thành `part_transactions`, loại giao dịch thành `IMPORT`/`EXPORT`, và vòng đời 6 trạng thái phiếu bảo hành thành `TIEP_NHAN` → `DA_PHAN_CONG` → `DANG_XU_LY` → `CHO_LINH_KIEN` → `HOAN_TAT` → `DA_DONG` (loại bỏ hoàn toàn các từ tiếng Việt `'NHAP'`, `'XUAT'`, `'Mới'`).
   - *Vì sao:* Đảm bảo nguyên tắc cốt lõi: mỗi khái niệm chỉ dùng đúng một tên kỹ thuật bất biến trên toàn bộ tài liệu và mã nguồn.
   - *Ảnh hưởng:* `docs/srs.md`, `docs/api-contract.md`, `migrations/V1__init_schema.sql`, và nhãn Wireframe M1, M2, M3.
2. **TD-02: Chuẩn hóa liên kết Use Case UC-07 và UC-06 trên Wireframe M3 và M1:**
   - *Thay đổi gì:* Quy tụ US-06 và US-07 vào `UC-06 (Xem lịch sử giao dịch kho)` trên M3; xác định rõ `UC-07` là `UC-07 (Thiết lập ngưỡng tối thiểu)` thực hiện qua Modal tại M1.
   - *Vì sao:* Khắc phục lỗi lệch liên kết được GVHD chỉ ra tại Buổi 8; bảo đảm tính truy vết hai chiều (Traceability) 100%.
   - *Ảnh hưởng:* `docs/srs.md` (Mục 2.1 Ma trận truy vết, Mục 5.1 & 5.3 Wireframe).
3. **TD-03: Chuẩn hóa diễn đạt số lượng User Story tại Mục 1.3.3:**
   - *Thay đổi gì:* Cập nhật thành: "8 User Story (từ US-01 đến US-08), vượt mức tối thiểu đề bài, bao phủ 100% phạm vi nghiệp vụ luồng L5, tổng 26 Story Points".
   - *Vì sao:* Đảm bảo tính nhất quán tuyệt đối giữa bảng thống kê và văn bản diễn đạt theo góp ý của GVHD.
   - *Ảnh hưởng:* `docs/srs.md` (Mục 1.3.3 và Mục 3.3).
4. **TD-04: Khôi phục tài liệu API Contract và bổ sung cam kết liêm chính học thuật trong Phụ lục AI:**
   - *Thay đổi gì:* Khôi phục toàn diện `docs/api-contract.md` (9 endpoints RESTful, DTOs, bảng mã lỗi, JWT spec); bổ sung khẩu lệnh cam kết nguyên văn: `> "Tôi xác nhận đã đọc, hiểu và chịu trách nhiệm về toàn bộ nội dung nộp."` kèm đầy đủ họ tên, MSSV, track và ngày ký.
   - *Vì sao:* Đáp ứng bắt buộc của Track SE về tài liệu giao tiếp API và tuân thủ Quy định Liêm chính học thuật ĐH Văn Lang.
   - *Ảnh hưởng:* `docs/api-contract.md`, `docs/ai-disclosure.md`.

### Giai đoạn 2: Thay đổi trong quá trình thực hiện Bài tập 2 (Buổi 12)

5. **TD-BT2-01: Tích hợp công cụ Flyway Database Migration tự động từ CSDL trống:**
   - *Thay đổi gì:* Quản lý lược đồ CSDL qua Flyway với `V1__init_schema.sql`, tự động tạo 5 bảng, khóa ngoại, chỉ mục NFR và ràng buộc `CHECK (quantity >= 0)`.
   - *Vì sao:* Đạt mức Tự động hóa cao nhất theo Tiêu chí S3 & C1; người chấm clone về chỉ cần chạy 1 lệnh là CSDL tự khởi tạo.
   - *Ảnh hưởng:* `pom.xml`, `application.properties`, thư mục `migrations/`, thư mục `db/migration/`.
6. **TD-BT2-02: Bổ sung 4 thao tác CRUD đầy đủ cho Thực thể `Part` (Danh mục linh kiện):**
   - *Thay đổi gì:* Thêm các endpoint `POST /api/v1/parts` (Tạo mới), `GET /api/v1/parts` (Đọc danh sách/chi tiết), `PUT /api/v1/parts/{partCode}` (Sửa), `DELETE /api/v1/parts/{partCode}` (Xóa mềm - Soft delete `isActive = false`).
   - *Vì sao:* Đáp ứng trọn vẹn tiêu chí S1 của Track SE: *$\ge 2$ thực thể có đủ 4 thao tác tạo – đọc – sửa – xóa*.
   - *Ảnh hưởng:* `PartController.java`, `PartService.java`, `PartServiceImpl.java`, `PartRepository.java`, `PartServiceTest.java`.
7. **TD-BT2-03: Hoàn thiện đủ 4 thao tác CRUD cho Thực thể `PartStock` (Tồn kho trung tâm):**
   - *Thay đổi gì:* Thêm `POST /api/v1/stock` (Khởi tạo tồn), `GET /api/v1/stock` (Đọc tồn), `PUT /api/v1/stock/threshold` (Sửa ngưỡng an toàn), `DELETE /api/v1/stock/{partCode}` (Xóa tồn khi số lượng = 0).
   - *Vì sao:* Đảm bảo thực thể nghiệp vụ thứ hai đáp ứng 100% tiêu chí S1 của Track SE.
   - *Ảnh hưởng:* `StockController.java`, `StockService.java`, `StockServiceImpl.java`, `PartStockRepository.java`, `api.js`, `app.js`.
8. **TD-BT2-04: Hiện thực hóa Khóa bi quan (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) chống xuất âm kho:**
   - *Thay đổi gì:* Phát sinh câu lệnh SQL `SELECT ... FOR UPDATE` khóa bản ghi tồn kho ngay khi bắt đầu giao dịch xuất kho.
   - *Vì sao:* Triệt tiêu 100% rủi ro Race Condition khi nhiều KTV cùng xuất 1 linh kiện tại cùng một thời điểm (QT-09 & NFR-02).
   - *Ảnh hưởng:* `PartStockRepository.java`, `PartTransactionServiceImpl.java`, `PartTransactionServiceTest.java`.
9. **TD-BT2-05: Nâng cấp Data Seeder chống trùng lặp theo Khóa duy nhất (Unique Key) và đối soát tồn kho NFR-04:**
   - *Thay đổi gì:* Sử dụng `Set<String>` Unique Keys thay vì đếm số dòng, đồng thời tự động đối soát $\text{quantity} = \sum(\text{nhập}) - \sum(\text{xuất})$.
   - *Vì sao:* Vượt qua phép thử tự kiểm C5: chạy lại lần thứ hai không bị lỗi trùng khóa và không nhân đôi dữ liệu.
   - *Ảnh hưởng:* `DataInitializer.java`, thư mục `seeds/`.
10. **TD-BT2-06: Hiện thực hóa tầng Xác thực Token và Ma trận Phân quyền RBAC:**
    - *Thay đổi gì:* Bổ sung `AuthTokenService`, ném lỗi `ForbiddenException` (HTTP 403 Forbidden theo RFC-7807) khi sai vai trò.
    - *Vì sao:* Đáp ứng tiêu chuẩn Thành phần 3 (Track SE): KTV chỉ được xuất kho sửa chữa; Quản lý kho chỉ được nhập kho, chỉnh ngưỡng và CRUD danh mục linh kiện (QT-14).
    - *Ảnh hưởng:* `AuthTokenService.java`, `ForbiddenException.java`, `TransactionController.java`, `StockController.java`.
11. **TD-BT2-07: Đóng gói Container hóa với Dockerfile Multi-stage và Docker Compose:**
    - *Thay đổi gì:* Bổ sung `Dockerfile` đa tầng và `docker-compose.yml` tích hợp MySQL 8.0 + Backend Spring Boot có healthcheck.
    - *Vì sao:* Đáp ứng mức đánh giá cao của Thành phần 4 (chạy toàn bộ hệ thống bằng đúng 1 lệnh `docker compose up -d`).
    - *Ảnh hưởng:* `Dockerfile`, `docker-compose.yml`, `.dockerignore`.

---

## 4. Hướng dẫn cài đặt và chạy hệ thống (Clone-and-Run)

### 4.1. Yêu cầu môi trường & Phần mềm cần cài đặt trước (Prerequisites)

Để đảm bảo hệ thống khởi chạy suôn sẻ và người chấm có thể tái lập nhanh chóng, máy tính cần được cài đặt sẵn các công cụ môi trường dưới đây:

| STT | Phần mềm / Công cụ | Phiên bản khuyến nghị | Mục đích sử dụng | Lệnh kiểm tra phiên bản | Liên kết tải về chính thức |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **1** | **Java JDK** | `>= 17 LTS` (Khuyến nghị **Eclipse Temurin 17** hoặc **21 LTS**) | Chạy ứng dụng Spring Boot và thực thi bộ kiểm thử tự động JUnit 5 | `java -version` | [adoptium.net](https://adoptium.net/) |
| **2** | **Maven Wrapper** | Đã tích hợp sẵn trong repo (`./mvnw` / `mvnw.cmd`) | Biên dịch, quản trị dependencies và chạy ứng dụng mà **không cần cài Maven** | `./mvnw -v` | Đi kèm sẵn trong dự án |
| **3** | **Docker & Docker Compose** *(Khuyên dùng)* | Docker Engine `>= 20.10`<br>Docker Compose `>= 2.0` | Khởi chạy toàn bộ hệ thống trọn gói (Web + API + DB) bằng đúng 1 lệnh duy nhất | `docker -v`<br>`docker compose version` | [docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop/) |
| **4** | **Git** | `>= 2.30` | Sao chép (clone) mã nguồn dự án từ kho lưu trữ GitHub | `git --version` | [git-scm.com](https://git-scm.com/) |
| **5** | **MySQL Server** *(Tùy chọn)* | `>= 8.0` | Sử dụng nếu muốn chạy trực tiếp Backend với CSDL MySQL cục bộ trên máy thật | `mysql --version` | [dev.mysql.com/downloads](https://dev.mysql.com/downloads/) |

> [!IMPORTANT]
> **Lựa chọn cách chạy phù hợp với nhu cầu:**
> - **Cách 1 (Khuyên dùng cho Giảng viên chấm bài / Đánh giá nhanh):** Chạy trực tiếp bằng **Maven Wrapper** với Profile `local` sử dụng CSDL In-Memory H2. Cách này **chạy ngay lập tức trong 5 giây**, tự động nạp dữ liệu hạt giống từ `seeds/`, bật `show-sql` hiển thị rõ mệnh đề khóa `SELECT ... FOR UPDATE` mà **không cần cài đặt MySQL hay bật Docker**.
> - **Cách 2 (Khuyên dùng khi muốn kiểm tra môi trường Container hoàn chỉnh):** Chạy toàn bộ hệ thống (App Spring Boot + MySQL 8.0) chỉ với **1 lệnh Docker Compose duy nhất (`docker compose up -d`)**.

---

### 4.2. Tải mã nguồn dự án (Clone Repository)

Mở cửa sổ dòng lệnh (PowerShell, Command Prompt hoặc Terminal) và thực hiện clone dự án:

```bash
git clone https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5.git
cd cdtn1-phamtritrong-l5
```

---

### 4.3. Cách 1: Chạy trực tiếp bằng Maven Wrapper (Local / H2 In-Memory hoặc MySQL) (Khuyến nghị)

#### Phương án 1A: Chạy chế độ `local` dùng H2 In-Memory (Nhanh nhất — Không cần MySQL)

Hệ thống đã cấu hình sẵn profile `local` ([`application-local.properties`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/resources/application-local.properties)) dùng CSDL H2 in-memory, tự động nạp dữ liệu mẫu và in rõ câu lệnh Hibernate SQL:

- **Trên Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
  ```
- **Trên Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
  ```

#### Phương án 1B: Chạy chế độ `dev` kết nối MySQL cục bộ

Nếu muốn chạy trực tiếp với MySQL trên máy thật:

1. **Chuẩn bị biến môi trường:**
   ```bash
   cp .env.example .env
   ```
   *(Cập nhật mật khẩu MySQL của bạn trong tệp `.env` nếu khác giá trị mặc định).*
2. **Tạo CSDL trống trên MySQL:**
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS mekong_crm_inventory CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   ```
3. **Khởi động ứng dụng (Flyway sẽ tự động Migrate schema và Seeder tự nạp dữ liệu):**
   ```bash
   ./mvnw spring-boot:run
   ```

#### Địa chỉ truy cập ứng dụng:
- **Giao diện Web Prototype (3 màn hình):** **`http://localhost:8080/`**
- **Kiểm tra trạng thái dịch vụ (Health Check):** **`http://localhost:8080/api/health`**
- **Bảng điều khiển H2 Console (khi chạy profile local):** **`http://localhost:8080/h2-console`**  
  *(JDBC URL: `jdbc:h2:mem:mekong_crm_inventory` · User: `sa` · Password: để trống)*

---

### 4.4. Cách 2: Chạy toàn bộ hệ thống bằng Docker Compose (≤ 2 lệnh trọn gói)

Nếu máy tính đã cài đặt và đang bật **Docker Desktop**, bạn có thể khởi chạy toàn bộ hệ thống (Ứng dụng Spring Boot + CSDL MySQL 8.0) chỉ với **1 lệnh duy nhất**:

```bash
docker compose up -d --build
```
*(Hoặc `docker compose up -d` nếu không cần build lại image)*

Hệ thống tự động thực hiện các tác vụ:
1. Kéo image MySQL 8.0, cấu hình tài khoản và đợi kiểm tra sức khỏe `mysqladmin ping` thành công.
2. Build image ứng dụng Spring Boot từ Dockerfile đa tầng (chạy user bảo mật `mekonguser`).
3. Khởi chạy Backend, thực thi Flyway migration tạo đủ 5 bảng và nạp tự động dữ liệu hạt giống từ `seeds/`.
4. Mở cổng `8888` sẵn sàng phục vụ tại **`http://localhost:8888/`** *(cổng host mặc định `8888` giúp tránh xung đột với các dịch vụ khác hoặc cổng 8080 bị khóa trên Windows; nếu muốn đổi về cổng `8080`, chỉ cần chạy `APP_PORT=8080 docker compose up -d`)*.
   - **Giao diện Web Prototype (Docker):** **`http://localhost:8888/`**
   - **Kiểm tra trạng thái dịch vụ (Health Check):** **`http://localhost:8888/api/health`**

- **Dừng toàn bộ hệ thống khi hoàn tất nghiệm thu:**
  ```bash
  docker compose down
  ```

---

## 5. Hướng dẫn chạy Bộ kiểm thử (Automated Tests)

*(Yêu cầu: Đã cài JDK 17+. Toàn bộ 17 kịch bản kiểm thử chạy hoàn toàn độc lập trên CSDL H2 in-memory, không phụ thuộc vào dữ liệu máy cá nhân hay MySQL ngoài)*:

Chạy toàn bộ 17 test cases bằng **MỘT lệnh duy nhất**:

- **Trên Linux / macOS:**
  ```bash
  ./mvnw test
  ```
- **Trên Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd test
  ```

### Kết quả kiểm chứng (17/17 tests PASS 100%):

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.mekongmobile.inventory.InventoryApplicationTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.134 s
[INFO] Running com.mekongmobile.inventory.service.AuthTokenServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.065 s
[INFO] Running com.mekongmobile.inventory.service.PartServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.300 s
[INFO] Running com.mekongmobile.inventory.service.PartTransactionServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.570 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

Bao quát đầy đủ 3 loại kiểm thử theo yêu cầu Rubric BT2:

- **Loại 1: Luồng đúng (Happy Path / Normal Flow):**
  - `testExportPart_Success_NormalFlow`: KTV xuất kho thành công khi tồn đủ và phiếu bảo hành ở trạng thái hợp lệ `DANG_XU_LY`, tồn kho giảm chính xác, sinh giao dịch `EXPORT`.
  - `testManagerCanImport`: Quản lý kho thực hiện giao dịch Nhập kho linh kiện mới từ NCC thành công.
  - `testTechnicianCanExport`: Kỹ thuật viên thực hiện xuất kho linh kiện sửa chữa thành công.
  - `createPart_Success` (CRUD Entity 1): Thêm mới linh kiện vào danh mục chuẩn hóa thành công.
  - `getPartByCode_Success` & `updatePart_Success`: Đọc chi tiết và cập nhật thông tin linh kiện thành công.
- **Loại 2: Giá trị biên (Boundary Conditions):**
  - `testExportPart_Boundary_ExactStockQuantity`: Xuất số lượng đúng bằng toàn bộ tồn kho hiện tại (`quantity == stock -> stock = 0`), tồn về đúng 0 mà không bị âm, tự động kích hoạt cờ cảnh báo tồn thấp `isLowStock = true`.
  - `contextLoads` & Spring Boot context: Kiểm tra nạp cấu hình hệ thống tại ngưỡng biên.
- **Loại 3: Dữ liệu sai & Ràng buộc bảo mật (Invalid Data & RBAC):**
  - `testExportPart_Error_OverStockAttempt`: Cố tình xuất vượt tồn kho (`quantity > stock`) $\rightarrow$ Bị từ chối ngay lập tức, ném ngoại lệ rõ ràng, số tồn kho giữ nguyên (QT-09 & NFR-02).
  - `testExportPart_Error_InvalidTicketStatus`: Từ chối xuất linh kiện cho Phiếu bảo hành đã đóng (`DA_DONG`) (QT-06).
  - `testImportPart_Error_InvalidQuantityNegative`: Chặn nhập kho với số lượng âm hoặc bằng 0 ($\le 0$) (Tiêu chí C7).
  - `createPart_DuplicateCode_ThrowsException`: Từ chối tạo linh kiện khi mã đã tồn tại.
  - `testTechnicianCannotImport`: Kỹ thuật viên cố tình Nhập kho $\rightarrow$ Bị chặn với lỗi `ForbiddenException` (HTTP 403 Forbidden theo QT-14).
  - `testManagerCannotExport`: Quản lý kho cố tình Xuất kho thay KTV $\rightarrow$ Bị chặn với lỗi `ForbiddenException` (HTTP 403 Forbidden theo QT-14).
  - `testMissingTokenThrowsForbidden`: Không truyền token $\rightarrow$ Bị từ chối HTTP 403 yêu cầu đăng nhập.

---

### 5.1. Kịch bản cURL kiểm thử nhanh các Endpoint CRUD & Quy tắc nghiệp vụ (Dành cho Giảng viên chấm)

Sau khi khởi động backend, Giảng viên có thể mở terminal và thực thi các câu lệnh `curl` sau để kiểm chứng trực tiếp:

#### A. Thực thể 1: Part (Danh mục linh kiện) — Đủ 4 thao tác CRUD:
```bash
# 1. Tạo mới linh kiện (Create - POST)
curl -X POST http://localhost:8080/api/v1/parts \
  -H "Content-Type: application/json" \
  -d '{"partCode":"PIN-IP-15","partName":"Pin iPhone 15 Pro Max chính hãng","category":"PIN","unit":"Cái","unitPrice":1250000.00}'

# 2. Đọc danh sách và chi tiết linh kiện (Read - GET)
curl http://localhost:8080/api/v1/parts?keyword=iPhone
curl http://localhost:8080/api/v1/parts/PIN-IP-15

# 3. Cập nhật thông tin linh kiện (Update - PUT)
curl -X PUT http://localhost:8080/api/v1/parts/PIN-IP-15 \
  -H "Content-Type: application/json" \
  -d '{"partName":"Pin iPhone 15 Pro Max Zin New 100%","category":"PIN","unit":"Cái","unitPrice":1350000.00,"isActive":true}'

# 4. Xóa linh kiện (Delete - DELETE: Soft delete chuyển isActive = false)
curl -X DELETE http://localhost:8080/api/v1/parts/PIN-IP-15
```

#### B. Thực thể 2: PartStock (Tồn kho trung tâm) — Đủ 4 thao tác CRUD:
```bash
# 1. Khởi tạo bản ghi tồn kho tại TTBH Quận 10 (Create - POST: Yêu cầu quyền Quản lý kho)
curl -X POST http://localhost:8080/api/v1/stock \
  -H "Content-Type: application/json" \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MTAyOnF1YW5seV9raG86TUFOQUdFUg==" \
  -d '{"partCode":"PIN-IP-15","initialQuantity":20,"minThreshold":5}'

# 2. Tra cứu danh sách tồn kho và chi tiết (Read - GET)
curl -H "X-Center-Id: 1" http://localhost:8080/api/v1/stock?isLowStock=false
curl -H "X-Center-Id: 1" http://localhost:8080/api/v1/stock/PIN-IP-15

# 3. Điều chỉnh ngưỡng tồn tối thiểu (Update - PUT: Yêu cầu quyền Quản lý kho theo UC-07)
curl -X PUT http://localhost:8080/api/v1/stock/threshold \
  -H "Content-Type: application/json" \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MTAyOnF1YW5seV9raG86TUFOQUdFUg==" \
  -d '{"partCode":"PIN-IP-15","minThreshold":8}'

# 4. Xóa bản ghi tồn kho khi số lượng tồn bằng 0 (Delete - DELETE)
curl -X DELETE http://localhost:8080/api/v1/stock/PIN-IP-15 \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MTAyOnF1YW5seV9raG86TUFOQUdFUg=="
```

#### C. Kiểm chứng 2 Quy tắc nghiệp vụ thật:

```bash
# Quy tắc 1 (QT-09 & NFR-02): Khóa bi quan chống xuất âm kho (Cố tình xuất 999 cái vượt tồn kho)
# -> Hệ thống từ chối ngay lập tức với HTTP 400 và thông báo vi phạm tồn kho
curl -X POST http://localhost:8080/api/v1/transactions/export \
  -H "Content-Type: application/json" \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MTAxOmt0dl90cm9uZzpURUNITklDSUFO" \
  -d '{"ticketId":"BH-000022/2025","partId":"MH-AP-01","quantity":999,"performedBy":"KTV - Phạm Trí Trọng"}'

# Quy tắc 2 (QT-06): Từ chối xuất cho Phiếu bảo hành đã đóng hoặc không tồn tại
# -> Hệ thống từ chối với HTTP 400 kèm thông báo trạng thái phiếu không hợp lệ
curl -X POST http://localhost:8080/api/v1/transactions/export \
  -H "Content-Type: application/json" \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MTAxOmt0dl90cm9uZzpURUNITklDSUFO" \
  -d '{"ticketId":"BH-999999/2026","partId":"MH-AP-01","quantity":1,"performedBy":"KTV - Phạm Trí Trọng"}'

# Quy tắc bổ sung (QT-14 & RBAC): Kỹ thuật viên cố tình thực hiện thao tác Nhập kho
# -> Hệ thống từ chối với HTTP 403 Forbidden
curl -X POST http://localhost:8080/api/v1/transactions/import \
  -H "Content-Type: application/json" \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MTAxOmt0dl90cm9uZzpURUNITklDSUFO" \
  -d '{"partId":"MH-AP-01","quantity":10,"performedBy":"ktv_trong"}'
```

#### D. Kiểm chứng "Tệp Service hoàn toàn không có dấu vết HTTP" (Tiêu chí S2):

Mở các tệp trong gói [`com.mekongmobile.inventory.service`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/service/impl/) để kiểm chứng:
- **0** import `jakarta.servlet.*` hay `org.springframework.http.*`.
- **0** sự xuất hiện của `HttpServletRequest`, `HttpServletResponse`, hoặc `ResponseEntity`.
- **0** mã trạng thái HTTP (`200`, `400`, `403`, `404`, `500`).
- Mọi lỗi nghiệp vụ đều ném ngoại lệ miền thuần túy (`BusinessRuleException`, `ResourceNotFoundException`, `ForbiddenException`). Toàn bộ việc ánh xạ sang HTTP status codes chuẩn RFC-7807 được cách ly hoàn toàn tại [`GlobalExceptionHandler.java`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/config/GlobalExceptionHandler.java) trong tầng Controller.

---

## 6. Minh chứng 3 màn hình nghiệp vụ & Xử lý 3 trạng thái (Buổi 10)

Ứng dụng Web Prototype cung cấp đầy đủ 3 màn hình nghiệp vụ chính theo đúng Wireframe luồng L5:

1. **Màn hình 1 (M1) — Danh mục & Tồn kho linh kiện tại TTBH (`#stock`):**
   - 4 thẻ KPI thống kê số liệu trực quan: *Tổng linh kiện danh mục*, *Linh kiện đủ hàng*, *Cảnh báo tồn thấp*, *Hết hàng khả dụng*.
   - Thanh công cụ tìm kiếm theo từ khóa linh kiện và checkbox lọc nhanh linh kiện dưới định mức an toàn (`isLowStock = true`).
   - Cụm nút thao tác nghiệp vụ: `➕ Thêm linh kiện`, `📥 Nhập kho mới`, `✏ Sửa`, `⚙ Ngưỡng`, `🗑 Xóa`.
2. **Màn hình 2 (M2) — Lập phiếu xuất linh kiện sửa chữa (`#export`):**
   - Biểu mẫu xuất kho chuẩn hóa: nhập mã phiếu bảo hành (`ticketCode`), chọn linh kiện thay thế, số lượng và lý do xuất máy.
   - Tự động kiểm tra điều kiện trạng thái phiếu bảo hành đang xử lý (`DANG_XU_LY` theo QT-06).
   - Kiểm tra tồn khả dụng tức thời, ngăn chặn triệt để thao tác xuất vượt quá số lượng hiện có trong kho.
3. **Màn hình 3 (M3) — Sổ cái lịch sử giao dịch kho Append-Only (`#transactions`):**
   - Bảng nhật ký biến động kho bất biến (NFR-04), hoàn toàn không có nút sửa hay xóa vật lý.
   - Phân biệt trực quan: Nhập kho (`IMPORT`, huy hiệu xanh lá dấu `+`) và Xuất kho (`EXPORT`, huy hiệu xám dấu `-`).
   - Bộ lọc đa năng: mã linh kiện, loại giao dịch, khoảng ngày giao dịch (`fromDate` - `toDate`) và phân trang mượt mà.

### Xử lý trọn vẹn 3 trạng thái tại tất cả màn hình:

- **Đang tải (Loading State):** Hiển thị Spinner xoay hoạt họa và hiệu ứng làm mờ mượt mà khi đang nạp dữ liệu từ REST API.
- **Rỗng (Empty State):** Phân biệt rõ ràng giữa "Trung tâm chưa có linh kiện nào" và "Không tìm thấy kết quả phù hợp với bộ lọc", kèm nút bấm nổi bật **"Xóa bộ lọc"** để người dùng nhanh chóng quay lại trạng thái ban đầu.
- **Lỗi kết nối (Error State):** Khi backend tắt hoặc mất kết nối mạng, giao diện hiển thị hộp thông báo lỗi màu đỏ kèm nút **"Thử lại ngay"** để nạp lại dữ liệu mà không cần F5 toàn trang.

---

## 7. Tài khoản chạy thử nghiệm (Demo Accounts)

Người dùng có thể đăng nhập hoặc dùng nút **"Đổi vai trò"** nhanh trên thanh tiêu đề (Navbar):

| Tên tài khoản | Mật khẩu | Vai trò (Role) | Trung tâm làm việc | Quyền hạn nghiệp vụ trong hệ thống |
| :--- | :---: | :---: | :--- | :--- |
| **`ktv_trong`** | `123456` | `TECHNICIAN`<br>(Kỹ thuật viên) | TT01 — TTBH Quận 10 (TP.HCM) | Tra cứu tồn kho, lập phiếu xuất kho linh kiện sửa chữa theo phiếu bảo hành, xem sổ cái giao dịch. Bị cấm Nhập kho, Sửa ngưỡng, Thêm/Xóa linh kiện (HTTP 403). |
| **`quanly_kho`** | `123456` | `MANAGER`<br>(Quản lý kho) | TT01 — TTBH Quận 10 (TP.HCM) | Nhập kho từ NCC, điều chỉnh định mức an toàn tối thiểu (UC-07), thêm/sửa/xóa danh mục linh kiện. Bị cấm Xuất kho sửa chữa thay KTV (HTTP 403). |
| **`ktv_cantho`** | `123456` | `TECHNICIAN`<br>(Kỹ thuật viên) | TT04 — TTBH Ninh Kiều (Cần Thơ) | Tra cứu tồn kho và xuất kho linh kiện tại chi nhánh Cần Thơ. |

---

## 8. Cấu trúc thư mục

```text
cdtn1-phamtritrong-l5/
├── docs/                             # Toàn bộ tài liệu đặc tả & thiết kế
│   ├── srs.md                        # SRS chuẩn ISO/IEC/IEEE 29148 (8 User Story, 26 Story Points)
│   ├── thay-doi-thiet-ke.md          # Nhật ký thay đổi thiết kế chuẩn 3 dòng (11 thay đổi hoàn thiện)
│   ├── ai-disclosure.md              # Bảng khai báo AI 4 cột chuẩn ĐH Văn Lang
│   ├── architecture-thuc-te.md       # Báo cáo kiến trúc 4 tầng thực tế
│   ├── kientruc.drawio               # Sơ đồ kiến trúc thực tế dạng thanh chuyển tầng
│   ├── api-contract.md               # Đặc tả hợp đồng API REST/JSON
│   └── images/bt2/                   # 8 ảnh chụp màn hình minh chứng nghiệm thu kỹ thuật
├── migrations/                       # Kịch bản Flyway Database Migration
│   └── V1__init_schema.sql           # DDL tạo 5 bảng chuẩn 3NF, CHECK constraints & chỉ mục NFR
├── seeds/                            # Dữ liệu mẫu CSV chuẩn của Case study Mekong Mobile
│   ├── service_centers.csv           # 6 Trung tâm bảo hành trên toàn quốc
│   ├── parts.csv                     # Danh mục linh kiện chuẩn hóa
│   ├── tickets.csv                   # Phiếu bảo hành liên kết từ luồng tiếp nhận
│   ├── part_stock.csv                # Tồn kho ban đầu tại các trung tâm
│   └── part_transactions.csv         # Lịch sử giao dịch sổ cái
├── src/
│   ├── main/
│   │   ├── java/com/mekongmobile/inventory/
│   │   │   ├── controller/           # Tầng 1: REST Controllers (Stock, Part, Transaction, Auth)
│   │   │   ├── service/              # Tầng 2: Nghiệp vụ cốt lõi (100% Non-HTTP, QT-09, QT-06, RBAC)
│   │   │   │   └── impl/             # Cài đặt nghiệp vụ, Khóa bi quan, đối soát NFR-04
│   │   │   ├── repository/           # Tầng 3: Spring Data JPA (hỗ trợ PESSIMISTIC_WRITE)
│   │   │   ├── entity/               # Thực thể CSDL JPA (Part, PartStock, PartTransaction, Ticket...)
│   │   │   ├── dto/                  # Request/Response DTOs
│   │   │   ├── exception/            # Ngoại lệ miền nghiệp vụ (BusinessRule, Forbidden)
│   │   │   └── config/               # Cấu hình Flyway, Cors, GlobalExceptionHandler, DataInitializer
│   │   └── resources/
│   │       ├── db/migration/         # Bản sao migration cho Flyway classpath
│   │       ├── static/               # Giao diện Frontend Prototype (HTML5, CSS, api.js, app.js)
│   │       ├── application.properties # Cấu hình chung của ứng dụng
│   │       ├── application-dev.properties # Cấu hình môi trường MySQL
│   │       └── application-local.properties # Cấu hình môi trường H2 In-Memory chạy ngay
│   └── test/                         # Bộ kiểm thử tự động 17 test cases (JUnit 5 + Mockito + H2)
├── .dockerignore                     # Loại trừ file thừa khi đóng gói Docker
├── .env.example                      # Biến môi trường mẫu đầy đủ
├── .gitignore                        # Loại trừ .env thật, thư mục target/ và file tạm
├── BT2_2374802010525_PhamTriTrong.md # Báo cáo nghiệm thu kỹ thuật toàn diện 7 mục
├── BT2_2374802010525_PhamTriTrong.docx # Bản Word Báo cáo nghiệm thu kỹ thuật
├── Dockerfile                        # Multi-stage Dockerfile cho Spring Boot (Non-root user)
├── docker-compose.yml                # Khởi chạy toàn bộ hệ thống (App + MySQL) bằng đúng 1 lệnh
├── pom.xml                           # Tệp quản lý và khóa phiên bản thư viện Maven
└── README.md                         # Hướng dẫn cài đặt và vận hành hệ thống
```

---

## 9. Checklist tiến độ (Nghiệm thu Buổi 12 — Release v0.2)

- [x] **Tiêu chí C1 (Cốt lõi):** Hệ thống clone về chạy được theo đúng README bằng lệnh `./mvnw spring-boot:run` hoặc `docker compose up -d`, không cần bất kỳ can thiệp thủ công nào.
- [x] **Tiêu chí C2 (Cốt lõi):** Phân tầng sạch sẽ, đúng trách nhiệm: `controller/` $\rightarrow$ `service/` $\rightarrow$ `repository/`, không gọi chéo sai chiều.
- [x] **Tiêu chí C3 (Nghiệp vụ thật):** Hiện thực đầy đủ $\ge 2$ quy tắc nghiệp vụ thật:
  - Khóa bi quan chống xuất âm kho đồng thời (`SELECT ... FOR UPDATE` theo QT-09 & NFR-02).
  - Kiểm soát xuất linh kiện gắn chặt với vòng đời phiếu bảo hành (`DANG_XU_LY` theo QT-06).
  - Phân quyền nghiêm ngặt theo vai trò RBAC (KTV xuất kho, QLK nhập kho theo QT-14).
- [x] **Tiêu chí C4 (Kiểm thử tự động):** 17/17 test cases PASS 100% bằng đúng một lệnh `./mvnw test`, bao phủ trọn vẹn 3 nhóm: Luồng đúng, Giá trị biên, Dữ liệu sai.
- [x] **Tiêu chí C5 (Tính Idempotent):** Chạy lại lần thứ hai không bị lỗi trùng khóa và không nhân đôi dữ liệu nhờ cơ chế Unique Keys trong `DataInitializer.java`.
- [x] **Tiêu chí C6 (Quản lý cấu hình):** Khóa phiên bản phụ thuộc trong `pom.xml`, khai báo đầy đủ biến môi trường tại `.env.example`, `.env` thật được `.gitignore` bảo vệ tuyệt đối.
- [x] **Tiêu chí C7 (Xử lý lỗi):** Dữ liệu vào không hợp lệ được từ chối an toàn với thông báo lỗi chuẩn RFC-7807 qua `GlobalExceptionHandler`, không làm sập ứng dụng.
- [x] **Tiêu chí S1 (CRUD Track SE):** Đầy đủ 4 thao tác Tạo – Đọc – Sửa – Xóa cho 2 thực thể cốt lõi (`Part` và `PartStock`).
- [x] **Tiêu chí S2 (Service Clean Track SE):** 100% các tệp trong tầng `service/` không chứa dấu vết HTTP (`req`, `res`, `ResponseEntity`, mã HTTP).
- [x] **Tiêu chí S3 (Migration & Seeder Track SE):** Tự động Migrate từ CSDL trống bằng Flyway (`V1__init_schema.sql`) và Seeder tự nạp 5 file CSV từ `seeds/`.
- [x] **Tiêu chí S4 (Frontend Prototype Track SE):** Giao diện 3 màn hình nghiệp vụ kết nối REST API thật, xử lý tập trung đủ 3 trạng thái (*Loading, Empty có nút xóa lọc, Error có nút thử lại*).
- [x] **Thành phần 4 Mức đánh giá cao (Bonus):** Đóng gói Dockerfile đa tầng và `docker-compose.yml` khởi chạy toàn bộ hệ thống bằng đúng 1 lệnh.
