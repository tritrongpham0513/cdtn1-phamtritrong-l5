# MEKONG MOBILE SMART CRM — PHÂN HỆ LUỒNG L5

> **Quản lý kho linh kiện thay thế tại 6 Trung tâm Bảo hành, kiểm soát xuất/nhập kho theo phiếu bảo hành, khóa bi quan chống xuất âm kho đồng thời, đối soát sổ cái giao dịch bất biến.**

- **Sinh viên thực hiện:** Phạm Trí Trọng
- **MSSV:** 2374802010525
- **Track chuyên ngành:** Kỹ thuật phần mềm (SE)
- **Học phần:** Chuyên đề tốt nghiệp 1 (HK1, 2026–2027) — Khoa CNTT, Đại học Văn Lang
- **Giảng viên hướng dẫn:** TS. Nguyễn Trí Hải
- **Tiến độ hiện tại:** Hoàn tất Bài tập 3 (BT3) — Kiểm thử và Đóng gói prototype — Release **v1.0**
- **Phiên bản release:** `v1.0` (Tag: `v1.0`)
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
- **Kiểm thử tự động:** JUnit 5, Mockito, MockMvc, H2 In-Memory. 12 kịch bản kiểm thử tự động chuẩn hóa (7 Unit + 3 Integration + 2 API) bao phủ trọn vẹn mô hình Kim tự tháp theo Đề bài Bài tập 3: Unit test nghiệp vụ (Happy path, Boundary, Negative data, và Chủ sở hữu dữ liệu OWASP A01 / S2), Integration test tính tái lập Seeder (C8), xuất/nhập kho và kiểm tra phiếu đã đóng trên H2, cùng REST API slice test (HTTP 201, 422). Lệnh chạy kiểm thử một bước duy nhất: `.\mvnw.cmd test` (Windows) hoặc `./mvnw test` (Linux) đạt 12/12 PASS 100%.
- **Đóng gói & Triển khai:** Docker Multi-stage build (JRE Eclipse Temurin 17 non-root user `mekonguser`), Docker Compose v2 ([`docker-compose.yml`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/docker-compose.yml) khởi chạy trọn gói Backend + Database bằng 2 lệnh: `cp .env.example .env` và `docker compose up -d --build`).

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
    - *Thay đổi gì:* Bổ sung `AuthTokenService`, ném lỗi `ForbiddenException` (HTTP 403 Forbidden theo cấu trúc `ApiResponseDTO` thống nhất) khi sai vai trò.
    - *Vì sao:* Đáp ứng tiêu chuẩn Thành phần 3 (Track SE): KTV chỉ được xuất kho sửa chữa; Quản lý kho chỉ được nhập kho, chỉnh ngưỡng và CRUD danh mục linh kiện (QT-14).
    - *Ảnh hưởng:* `AuthTokenService.java`, `ForbiddenException.java`, `TransactionController.java`, `StockController.java`.
11. **TD-BT2-07: Đóng gói Container hóa với Dockerfile Multi-stage và Docker Compose:**
    - *Thay đổi gì:* Bổ sung `Dockerfile` đa tầng và `docker-compose.yml` tích hợp MySQL 8.0 + Backend Spring Boot có healthcheck.
    - *Vì sao:* Đáp ứng mức đánh giá cao của Thành phần 4 (chạy toàn bộ hệ thống bằng 2 lệnh `cp .env.example .env` và `docker compose up -d --build`).
    - *Ảnh hưởng:* `Dockerfile`, `docker-compose.yml`, `.dockerignore`.

---

## 4. Hướng dẫn cài đặt và chạy hệ thống (Clone-and-Run)

### 4.1. Yêu cầu môi trường & Phần mềm cần cài đặt trước (Prerequisites)

Để đảm bảo hệ thống khởi chạy suôn sẻ và người chấm có thể tái lập nhanh chóng, máy tính cần được cài đặt sẵn các công cụ môi trường dưới đây:

| STT | Phần mềm / Công cụ | Phiên bản khuyến nghị | Mục đích sử dụng | Lệnh kiểm tra phiên bản | Liên kết tải về chính thức |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **1** | **Java JDK** | `>= 17 LTS` (Khuyến nghị **Eclipse Temurin 17** hoặc **21 LTS**) | Chạy ứng dụng Spring Boot và thực thi bộ kiểm thử tự động JUnit 5 | `java -version` | [adoptium.net](https://adoptium.net/) |
| **2** | **Maven Wrapper** | Đã tích hợp sẵn trong repo (`./mvnw` / `mvnw.cmd`) | Biên dịch, quản trị dependencies và chạy ứng dụng mà **không cần cài Maven** | `./mvnw -v` | Đi kèm sẵn trong dự án |
| **3** | **Docker & Docker Compose** *(Khuyên dùng)* | Docker Engine `>= 20.10`<br>Docker Compose `>= 2.0` | Khởi chạy toàn bộ hệ thống trọn gói (Web + API + DB) bằng 2 lệnh | `docker -v`<br>`docker compose version` | [docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop/) |
| **4** | **Git** | `>= 2.30` | Sao chép (clone) mã nguồn dự án từ kho lưu trữ GitHub | `git --version` | [git-scm.com](https://git-scm.com/) |
| **5** | **MySQL Server** *(Tùy chọn)* | `>= 8.0` | Sử dụng nếu muốn chạy trực tiếp Backend với CSDL MySQL cục bộ trên máy thật | `mysql --version` | [dev.mysql.com/downloads](https://dev.mysql.com/downloads/) |

> [!IMPORTANT]
> **Lựa chọn cách chạy phù hợp với nhu cầu:**
> - **Cách 1 (Khuyên dùng cho Giảng viên chấm bài / Đánh giá nhanh):** Chạy trực tiếp bằng **Maven Wrapper** với Profile `local` sử dụng CSDL In-Memory H2. Cách này **chạy ngay lập tức trong 5 giây**, tự động nạp dữ liệu hạt giống từ `seeds/`, bật `show-sql` hiển thị rõ mệnh đề khóa `SELECT ... FOR UPDATE` mà **không cần cài đặt MySQL hay bật Docker**.
> - **Cách 2 (Khuyên dùng khi muốn kiểm tra môi trường Container hoàn chỉnh):** Chạy toàn bộ hệ thống (App Spring Boot + MySQL 8.0) bằng **2 lệnh Docker Compose (`cp .env.example .env` và `docker compose up -d --build`)**.

---

### 4.2. Tải mã nguồn dự án (Clone Repository)

Mở cửa sổ dòng lệnh (PowerShell, Command Prompt hoặc Terminal) và thực hiện clone dự án:

```bash
git clone --branch v1.0 https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5.git
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

### 4.4. Cách 2: Chạy toàn bộ hệ thống bằng Docker Compose (≤ 2 lệnh trọn gói — Thành phần 4)

Phương án đóng gói toàn bộ hệ thống (Ứng dụng Spring Boot + CSDL MySQL 8.0) vào Docker Compose theo tiêu chí cốt lõi C1. Giảng viên hoặc người chấm có thể dựng và kiểm thử trọn gói chỉ với **đúng 2 lệnh**.

#### 1. Yêu cầu môi trường (Prerequisites)
- Đã cài đặt và đang chạy **Docker Desktop** (hỗ trợ Docker Compose v2).
- Tài nguyên tối thiểu: ~2GB RAM trống, ~2GB dung lượng ổ đĩa.
- Cổng kết nối host mặc định: `8088` (ứng dụng web/API) và `3307` (MySQL ngoài container, tránh đụng cổng 3306 cục bộ). Cổng `8088` được chọn làm mặc định nhằm tránh dải cổng bị chặn bởi cơ chế WinNAT / Hyper-V trên Windows.
- JDK 17+ nếu muốn chạy bộ test trên máy host (không bắt buộc cho đường Docker).

#### 2. Hai lệnh khởi chạy hệ thống (2-Command Quickstart)

- **Trên Linux / macOS / Git Bash:**
  ```bash
  # Lệnh 1: Sao chép tệp cấu hình môi trường mẫu (mặc định đã cấu hình sẵn APP_PORT=8088)
  cp .env.example .env

  # Lệnh 2: Xây dựng image và khởi chạy toàn bộ stack (tự động đợi container đạt trạng thái Healthy)
  docker compose up -d --build --wait
  ```

- **Trên Windows Command Prompt (`cmd.exe`):**
  ```cmd
  copy .env.example .env
  docker compose up -d --build --wait
  ```

- **Trên Windows PowerShell:**
  ```powershell
  Copy-Item .env.example .env
  docker compose up -d --build --wait
  ```

> [!NOTE]
> Tham số `--wait` đảm bảo lệnh chỉ kết thúc và trả về terminal sau khi cả hai container `mekong-mysql` và `mekong-inventory-service` đều đã vượt qua healthcheck và đạt trạng thái `healthy`.

#### 3. Cơ chế hoạt động tự động của hệ thống
Hệ thống tự động thực thi chuỗi tác vụ sau khi nhận lệnh:
1. **Khởi tạo MySQL 8.0 (`mekong-mysql`):** Kéo image `mysql:8.0`, thiết lập CSDL `mekong_crm_inventory`, gán volume lưu trữ `mekong_mysql_data`, và thực hiện healthcheck `mysqladmin ping` định kỳ 5 giây cho đến khi CSDL hoàn toàn sẵn sàng (`healthy`).
2. **Xây dựng Dockerfile đa tầng (Multi-stage Build):**
   - *Stage 1 (Builder):* Dùng `eclipse-temurin:17-jdk-alpine`, chuẩn hóa xuống dòng LF cho `mvnw`, tải thư viện và biên dịch gói JAR thực thi.
   - *Stage 2 (Runtime):* Dùng `eclipse-temurin:17-jre-alpine` tối giản, tạo tài khoản người dùng bảo mật không đặc quyền `mekonguser` (`uid=100(mekonguser) gid=101(mekonggroup)`) để chạy ứng dụng (tuân thủ nguyên tắc bảo mật tối thiểu).
3. **Đồng bộ lược đồ CSDL với Flyway Migration:** Ứng dụng chờ `mysql-db` đạt trạng thái `healthy`, sau đó tự động áp dụng bản di chuyển `V1__init_schema.sql` khởi tạo chuẩn 5 bảng, chỉ mục và ràng buộc toàn vẹn `CHECK (quantity >= 0)`.
4. **Nạp dữ liệu hạt giống tự động (Data Seeder):** Tự động đọc và nạp 5 tệp CSV từ thư mục `seeds/` theo cơ chế đối soát và chống trùng lặp Unique Keys.
5. **Kiểm soát sẵn sàng (Startup Readiness):** Trong thời gian nạp dữ liệu, endpoint `/api/health` trả về HTTP 503 `{"status":"STARTING"}`. Khi hoàn tất nạp dữ liệu, trạng thái chuyển sang HTTP 200 `{"status":"UP"}`, container `mekong-inventory-service` đạt trạng thái `healthy`.

#### 4. Cách kiểm tra hệ thống đã chạy đúng (Checklist 5 bước)

Sau khi chạy 2 lệnh trên, thực hiện kiểm tra theo bảng đối chiếu sau:

```bash
# Bước 1: Kiểm tra trạng thái sức khỏe container (Cả 2 phải Up (healthy))
docker compose ps

# Bước 2: Kiểm tra endpoint sức khỏe qua HTTP (Phải trả về HTTP 200 và UP)
# (Trên Linux / macOS / Git Bash / PowerShell 7):
curl -s http://localhost:8088/api/health
# (Hoặc trên Windows PowerShell 5.1):
curl.exe -s http://localhost:8088/api/health
```

Kết quả mong đợi từ Bước 1 & Bước 2:
```text
NAME                       IMAGE                                STATUS                    PORTS
mekong-mysql               mysql:8.0                            Up (healthy)              0.0.0.0:3307->3306/tcp
mekong-inventory-service   <tên-thư-mục>-inventory-service      Up (healthy)              0.0.0.0:8088->8080/tcp
```
```json
{"service":"inventory-service","timestamp":"<thời-điểm-chạy, GMT+7>","status":"UP","module":"L5 - Kho linh kien thay the"}
```

- **Bước 3: Truy cập Giao diện Web Prototype:** Mở trình duyệt tại **`http://localhost:8088/`** để tương tác với 3 màn hình nghiệp vụ (*Quản lý kho M1, Cấp phát linh kiện M2, Lịch sử giao dịch M3*).
- **Bước 4: Kiểm tra số lượng bản ghi CSDL đã nạp đủ:**
  ```bash
  docker compose exec mysql-db mysql -u root -proot mekong_crm_inventory -e "SELECT 'service_centers' AS tbl, COUNT(*) AS total FROM service_centers UNION ALL SELECT 'parts', COUNT(*) FROM parts UNION ALL SELECT 'tickets', COUNT(*) FROM tickets UNION ALL SELECT 'part_stock', COUNT(*) FROM part_stock UNION ALL SELECT 'part_transactions', COUNT(*) FROM part_transactions;"
  ```
  Số lượng chuẩn đối soát:
  - `service_centers`: **6**
  - `parts`: **180**
  - `tickets`: **7,800**
  - `part_stock`: **717**
  - `part_transactions`: **4,954**
- **Bước 5: Chạy kiểm thử tự động trong mã nguồn:**
  ```powershell
  # Trên Windows:
  .\mvnw.cmd test
  # Trên Linux/macOS:
  ./mvnw test
  ```
  Kết quả mong đợi: **12/12 tests PASS 100%**, BUILD SUCCESS.

#### 5. Lệnh quản trị vòng đời container

- **Tạm dừng hệ thống (giữ nguyên dữ liệu CSDL):**
  ```bash
  docker compose stop
  ```
- **Khởi động lại hệ thống đã dừng:**
  ```bash
  docker compose start
  ```
- **Hạ toàn bộ hệ thống (giữ volume dữ liệu):**
  ```bash
  docker compose down
  ```
- **Xóa sạch toàn bộ hệ thống và dữ liệu để chạy lại từ trạng thái trắng hoàn toàn:**
  ```bash
  docker compose down -v
  docker compose up -d --build --wait
  ```

#### 6. Xử lý sự cố thường gặp (Troubleshooting)

- **Sự cố 1: Lỗi xung đột cổng trên Windows (WinNAT / Hyper-V Port Exclusion):**
  - *Hiện tượng:* `Error response from daemon: Ports not available: listen tcp 0.0.0.0:8088: bind: An attempt was made to access a socket in a way forbidden by its access permissions.`
  - *Nguyên nhân:* Hệ điều hành Windows với dịch vụ WinNAT / Hyper-V có thể tự động dự trữ ngẫu nhiên một số dải cổng TCP (kiểm tra bằng lệnh `netsh interface ipv4 show excludedportrange protocol=tcp`).
  - *Cách xử lý:* Mở tệp `.env`, sửa cổng dự phòng `APP_PORT=18080` (hoặc `APP_PORT=8080`), sau đó chạy lại:
    ```bash
    docker compose up -d --wait
    ```
    Hệ thống sẽ chuyển sang phục vụ tại `http://localhost:18080/`.
- **Sự cố 2: Docker daemon không phản hồi:**
  - *Hiện tượng:* `open //./pipe/docker_engine: The system cannot find the file specified` hoặc `Access is denied`.
  - *Cách xử lý:* Đảm bảo ứng dụng **Docker Desktop** đã được mở và khởi động hoàn tất trước khi chạy lệnh `docker compose`.
- **Sự cố 3: Xem nhật ký lỗi chi tiết của từng dịch vụ:**
  ```bash
  # Xem log ứng dụng Backend
  docker compose logs -f inventory-service
  # Xem log CSDL MySQL
  docker compose logs -f mysql-db
  ```

#### 7. Bằng chứng kiểm thử máy trắng độc lập (Clean-room Execution Proof)

Thực hiện kiểm thử thực tế trên thư mục độc lập tách rời khỏi môi trường phát triển:

```text
=============================================================================
CLEAN-ROOM EXECUTION LOG - MEKONG MOBILE SMART CRM L5
Thời điểm: 12/11/2026 01:48–01:49 (GMT+7)
Môi trường: Windows, Docker Desktop 28.5.1, Docker Compose v2.40.3
Nguồn chạy: git clone --branch v1.0 (tag v1.0)
Thư mục: thư mục trắng trong %TEMP%\opencode\bt3-final-check
Ghi chú: lần chạy đầu trước khi sửa healthcheck MySQL báo exit 1 vì
         mysqladmin ping pass nhầm ở temporary server (port 0);
         sau khi đổi healthcheck sang TCP + xác thực, lần chạy dưới đây
         đạt exit 0 và không container nào phải restart.
=============================================================================

1. Hai lệnh:
   $ cp .env.example .env
   $ docker compose up -d --build --wait

   Container mekong-mysql                     Healthy
   Container mekong-inventory-service         Healthy
   Thời gian build + khởi động: 39.7 giây, exit code 0
   RestartCount: app = 0, mysql = 0

2. docker compose ps:
   mekong-mysql               mysql:8.0                         Up (healthy)   0.0.0.0:3307->3306/tcp
   mekong-inventory-service   bt3-final-check-inventory-service Up (healthy)   0.0.0.0:8088->8080/tcp

3. Health check:
   $ curl.exe -s http://localhost:8088/api/health
   {"status":"UP","timestamp":"2026-11-12T01:49:26.476595857","service":"inventory-service","module":"L5 - Kho linh kien thay the"}
   HTTP_CODE:200

4. Giao diện web: HTTP_CODE:200 (http://localhost:8088/)

5. Dữ liệu mẫu (MySQL row counts):
   service_centers      6
   parts                180
   tickets              7800
   part_stock           717
   part_transactions    4954
   users                3

6. Bảng users (mật khẩu băm BCrypt):
   ktv_trong     $2a$10$   60 ký tự   TECHNICIAN   center 1
   quanly_kho    $2a$10$   60 ký tự   MANAGER      center 1
   ktv_cantho    $2a$10$   60 ký tự   TECHNICIAN   center 4
   Flyway: V1 init schema = success, V2 add users table = success

7. Đăng nhập:
   quanly_kho / 123456  -> HTTP 200
   quanly_kho / wrong   -> HTTP 409 (BUSINESS_RULE_VIOLATION)

8. Kiểm thử tự động:
   $ .\mvnw.cmd test
   Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
   [INFO] BUILD SUCCESS
   [INFO] Total time:  26.777 s

9. User trong container:
   uid=100(mekonguser) gid=101(mekonggroup) groups=101(mekonggroup)
```

---

## 5. Hướng dẫn chạy Bộ kiểm thử (Automated Tests)

*(Yêu cầu: Đã cài JDK 17+. Toàn bộ 12 kịch bản kiểm thử chạy hoàn toàn độc lập trên H2 in-memory và Mockito, không phụ thuộc vào dữ liệu máy cá nhân hay MySQL ngoài)*:

Chạy toàn bộ 12 test cases bằng **MỘT lệnh duy nhất**:

- **Trên Linux / macOS:**
  ```bash
  ./mvnw test
  ```
- **Trên Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd test
  ```

### Kết quả kiểm chứng (12/12 tests PASS 100%):

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.mekongmobile.inventory.config.DataInitializerIntegrationTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.mekongmobile.inventory.controller.TransactionControllerApiTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.mekongmobile.inventory.service.PartTransactionIntegrationTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.mekongmobile.inventory.service.PartTransactionServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

Chi tiết kế hoạch kiểm thử: [`docs/test-plan.md`](docs/test-plan.md).  
Báo cáo kết quả kiểm thử đầy đủ 8 cột: [`docs/test-report.md`](docs/test-report.md).

Phân bổ 12 test cases theo 5 loại kiểm thử chuẩn Bài tập 3 (7 Unit + 3 Integration + 2 API):

- **Loại 1: Luồng đúng (Happy Path / Normal Flow — 5 tests):**
  - `TC-01 / testExportPart_Success_NormalFlow`: KTV xuất kho thành công khi tồn đủ và phiếu bảo hành ở trạng thái hợp lệ `DANG_XU_LY` (QT-06, QT-09), tồn kho giảm chính xác, sinh giao dịch `EXPORT`.
  - `TC-02 / testImportPart_Success_NormalFlow`: Quản lý kho nhập kho thành công (US-02), tồn tăng chính xác và ghi sổ cái append-only `part_transactions`.
  - `TC-03 / testPartStock_LowStockAlert_US05`: Kiểm tra logic cảnh báo tồn thấp (US-05) tại các mức tồn 4 (< 5 -> cảnh báo), 5 (== 5 -> không cảnh báo), 10 (> 5 -> không cảnh báo).
  - `TC-09 / testExportThenImport_EndToEnd_PersistsStockAndLedger`: Integration test xuất và nhập kho end-to-end trên CSDL H2 thật, tồn 10 → 6 → 26, sổ cái lưu đúng 2 bản ghi.
  - `TC-11 / testExportPart_ApiSuccess`: API slice test xuất kho qua MockMvc, trả về HTTP 201 Created và cấu trúc envelope `ApiResponseDTO`.
- **Loại 2: Giá trị biên (Boundary Conditions — 2 tests):**
  - `TC-04 / testExportPart_Boundary_ExactStockQuantity`: Xuất số lượng đúng bằng toàn bộ tồn kho hiện tại (`quantity == 10/10`), tồn về đúng 0 mà không bị âm, tự động kích hoạt cờ cảnh báo tồn thấp `isLowStock = true` (0 < 5).
  - `TC-05 / testExportPart_Error_OverStockAttempt`: Cố tình xuất vượt tồn 1 đơn vị (`quantity == 11/10`) $\rightarrow$ Bị từ chối ngay lập tức với `IllegalStateException`, tồn giữ nguyên 10, không ghi sổ cái.
- **Loại 3: Dữ liệu sai (Invalid Data — 3 tests):**
  - `TC-06 / testExportPart_Error_InvalidTicketStatus`: Unit test từ chối xuất linh kiện cho Phiếu bảo hành đã đóng (`DA_DONG`), ném exception chứa `DA_DONG` và không khóa tồn (QT-06).
  - `TC-10 / testExportPart_InvalidTicketStatus_DA_DONG_ThrowsException`: Integration test trên CSDL H2 thật từ chối xuất cho phiếu `DA_DONG`, ném ngoại lệ rõ ràng và xác nhận sổ cái không phát sinh bản ghi giao dịch rác nào.
  - `TC-12 / testExportPart_ValidationError_ZeroQuantity`: Gửi request xuất kho với `quantity = 0` (vi phạm `@Min(1)`) $\rightarrow$ API trả về HTTP 422 Unprocessable Entity, `VALIDATION_ERROR` và chi tiết lỗi trường `quantity`.
- **Loại 4: Bảo mật & Phân quyền (Security / OWASP A01 / S2 — 1 test):**
  - `TC-07 / testExportPart_Error_CrossCenterOwnershipViolation`: KTV thuộc TT01 cố tình xuất linh kiện cho phiếu thuộc TT02 $\rightarrow$ Bị từ chối với `IllegalArgumentException` chứa "không thuộc quyền quản lý" (OWASP A01 / S2).
- **Loại 5: Tính tái lập (Reproducibility / C8 — 1 test):**
  - `TC-08 / testSeeder_RunTwice_DoesNotDuplicateData`: Seeder chạy lần 2 trên CSDL H2 thật không ném ngoại lệ và không nhân đôi dữ liệu (số lượng bản ghi 5 bảng giữ nguyên 100%).

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
  -H "Authorization: Bearer mk_token_MjpxdWFubHlfa2hvOk1BTkFHRVI6MToxNzk0NDIyMjQ4MDcy" \
  -d '{"partCode":"PIN-IP-15","initialQuantity":20,"minThreshold":5}'

# 2. Tra cứu danh sách tồn kho và chi tiết (Read - GET)
curl -H "X-Center-Id: 1" http://localhost:8080/api/v1/stock?isLowStock=false
curl -H "X-Center-Id: 1" http://localhost:8080/api/v1/stock/PIN-IP-15

# 3. Điều chỉnh ngưỡng tồn tối thiểu (Update - PUT: Yêu cầu quyền Quản lý kho theo UC-07)
curl -X PUT http://localhost:8080/api/v1/stock/threshold \
  -H "Content-Type: application/json" \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MjpxdWFubHlfa2hvOk1BTkFHRVI6MToxNzk0NDIyMjQ4MDcy" \
  -d '{"partCode":"PIN-IP-15","minThreshold":8}'

# 4. Xóa bản ghi tồn kho khi số lượng tồn bằng 0 (Delete - DELETE)
curl -X DELETE http://localhost:8080/api/v1/stock/PIN-IP-15 \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MjpxdWFubHlfa2hvOk1BTkFHRVI6MToxNzk0NDIyMjQ4MDcy"
```

#### C. Kiểm chứng 2 Quy tắc nghiệp vụ thật:

```bash
# Quy tắc 1 (QT-09 & NFR-02): Khóa bi quan chống xuất âm kho (Cố tình xuất 999 cái vượt tồn kho)
# -> Hệ thống từ chối ngay lập tức với HTTP 400 và thông báo vi phạm tồn kho
curl -X POST http://localhost:8080/api/v1/transactions/export \
  -H "Content-Type: application/json" \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MTprdHZfdHJvbmc6VEVDSE5JQ0lBTjoxOjE3OTQ0MjIyNDc5MTM=" \
  -d '{"ticketId":"BH-000022/2025","partId":"MH-AP-01","quantity":999,"performedBy":"KTV - Phạm Trí Trọng"}'

# Quy tắc 2 (QT-06): Từ chối xuất cho Phiếu bảo hành đã đóng hoặc không tồn tại
# -> Hệ thống từ chối với HTTP 400 kèm thông báo trạng thái phiếu không hợp lệ
curl -X POST http://localhost:8080/api/v1/transactions/export \
  -H "Content-Type: application/json" \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MTprdHZfdHJvbmc6VEVDSE5JQ0lBTjoxOjE3OTQ0MjIyNDc5MTM=" \
  -d '{"ticketId":"BH-999999/2026","partId":"MH-AP-01","quantity":1,"performedBy":"KTV - Phạm Trí Trọng"}'

# Quy tắc bổ sung (QT-14 & RBAC): Kỹ thuật viên cố tình thực hiện thao tác Nhập kho
# -> Hệ thống từ chối với HTTP 403 Forbidden
curl -X POST http://localhost:8080/api/v1/transactions/import \
  -H "Content-Type: application/json" \
  -H "X-Center-Id: 1" \
  -H "Authorization: Bearer mk_token_MTprdHZfdHJvbmc6VEVDSE5JQ0lBTjoxOjE3OTQ0MjIyNDc5MTM=" \
  -d '{"partId":"MH-AP-01","quantity":10,"performedBy":"ktv_trong"}'
```

#### D. Kiểm chứng "Tệp Service hoàn toàn không có dấu vết HTTP" (Tiêu chí S2):

Mở các tệp trong gói [`com.mekongmobile.inventory.service`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/service/impl/) để kiểm chứng:
- **0** import `jakarta.servlet.*` hay `org.springframework.http.*`.
- **0** sự xuất hiện của `HttpServletRequest`, `HttpServletResponse`, hoặc `ResponseEntity`.
- **0** mã trạng thái HTTP (`200`, `400`, `403`, `404`, `500`).
- Mọi lỗi nghiệp vụ đều ném ngoại lệ miền thuần túy (`BusinessRuleException`, `ResourceNotFoundException`, `ForbiddenException`). Toàn bộ việc ánh xạ sang HTTP status codes và định dạng phong bì `ApiResponseDTO` thống nhất được cách ly hoàn toàn tại [`GlobalExceptionHandler.java`](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/config/GlobalExceptionHandler.java) trong tầng Controller.

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
│   ├── api-docs/                     # Đặc tả REST API 17 endpoint × 7 mục (Thành phần 6)
│   ├── deployment-guide.md           # Hướng dẫn triển khai tiếng Anh chuẩn 5 mục (Thành phần 5)
│   ├── checklist-bao-mat.md          # Checklist bảo mật 7 mục theo OWASP Top 10 (Thành phần 5)
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
│   └── test/                         # Bộ kiểm thử tự động 12 test cases (JUnit 5 + Mockito + MockMvc + H2)
├── .dockerignore                     # Loại trừ file thừa khi đóng gói Docker
├── .env.example                      # Biến môi trường mẫu đầy đủ
├── .gitignore                        # Loại trừ .env thật, thư mục target/ và file tạm
├── BT2_2374802010525_PhamTriTrong.md # Báo cáo nghiệm thu kỹ thuật toàn diện 7 mục
├── BT2_2374802010525_PhamTriTrong.docx # Bản Word Báo cáo nghiệm thu kỹ thuật
├── Dockerfile                        # Multi-stage Dockerfile cho Spring Boot (Non-root user)
├── docker-compose.yml                # Khởi chạy toàn bộ hệ thống (App + MySQL) bằng 2 lệnh
├── pom.xml                           # Tệp quản lý và khóa phiên bản thư viện Maven
└── README.md                         # Hướng dẫn cài đặt và vận hành hệ thống
```

---

## 9. Nghiệm thu Bài tập 3 — Release v1.0

- [x] **Tiêu chí C1 (Gói đóng gói chạy được bằng ≤ 2 lệnh — Thành phần 4):** Khởi chạy trọn vẹn toàn bộ hệ thống bằng 2 lệnh `cp .env.example .env` (hoặc `copy` trên Windows) và `docker compose up -d --build`. Kiểm thử độc lập clean-room xác nhận 100% container `healthy`, nạp đủ 5 bảng CSDL và phản hồi HTTP 200 `UP`.
- [x] **Tiêu chí C2 (Phân tầng sạch sẽ):** Kiến trúc phân tầng nghiêm ngặt `controller/` $\rightarrow$ `service/` $\rightarrow$ `repository/`, không phụ thuộc ngược chiều.
- [x] **Tiêu chí C3 (Nghiệp vụ thật):** Hiện thực đầy đủ các quy tắc cốt lõi:
  - Khóa bi quan chống xuất âm kho đồng thời (`SELECT ... FOR UPDATE` theo QT-09 & NFR-02).
  - Kiểm soát xuất kho gắn chặt với vòng đời phiếu bảo hành (`DANG_XU_LY` theo QT-06).
  - Phân quyền theo vai trò RBAC (KTV xuất kho, QLK nhập kho theo QT-14).
  - Sổ cái bất biến Append-only (`part_transactions` theo NFR-04).
- [x] **Tiêu chí C4 (Kiểm thử tự động — Thành phần 1 & 2):** Đúng **12/12 test cases PASS 100%** bằng một lệnh `.\mvnw.cmd test` (hoặc `./mvnw test`), phân tầng chuẩn 7 Unit + 3 Integration + 2 API, bao phủ đủ 5 loại kiểm thử BT3.
- [x] **Tiêu chí C5 & C8 (Tính Idempotent & Tái lập Seeder):** Seeder chạy nhiều lần không trùng khóa, không nhân đôi dữ liệu nhờ cơ chế Unique Keys; kiểm chứng tự động qua `TC-08 / testSeeder_RunTwice_DoesNotDuplicateData`.
- [x] **Tiêu chí C6 (Quản lý cấu hình):** Khóa phiên bản phụ thuộc trong `pom.xml`, khai báo biến môi trường chuẩn trong `.env.example`, `.env` được bảo vệ bởi `.gitignore` và `.dockerignore`.
- [x] **Tiêu chí C7 (Xử lý lỗi):** Dữ liệu không hợp lệ được từ chối an toàn với cấu trúc lỗi phong bì `ApiResponseDTO` thống nhất qua `GlobalExceptionHandler`, không làm sập ứng dụng.
- [x] **Thành phần 1 (Test Plan rút gọn):** Hoàn thành [`docs/test-plan.md`](docs/test-plan.md) chuẩn hóa phạm vi 12 test case, tiêu chí chấp nhận và ma trận phân bổ.
- [x] **Thành phần 2 (Test Report):** Hoàn thành [`docs/test-report.md`](docs/test-report.md) báo cáo trung thực kết quả thực tế 12/12 PASS, bảng kết quả 8 cột và phân tích rủi ro.
- [x] **Thành phần 3 (Bug Log):** Hoàn thành [`docs/bug-log.md`](docs/bug-log.md) ghi nhận 3 lỗi bảo mật có thật (SEC-01, SEC-02, SEC-03) kèm commit khắc phục và kịch bản tái lập.
- [x] **Thành phần 4 (Gói đóng gói prototype):** Dockerfile đa tầng non-root (`mekonguser`), Docker Compose v2, healthcheck UP, tài liệu hóa hướng dẫn 2 lệnh và xử lý sự cố.
- [x] **Thành phần 5 (Deployment Guide):** Hoàn thành [`docs/deployment-guide.md`](docs/deployment-guide.md) viết hoàn toàn bằng tiếng Anh, chuẩn 5 mục bắt buộc kèm Troubleshooting và mục ghi các bước deploy lên Cloud (+1,0 điểm cộng).
- [x] **Thành phần 5 (Checklist bảo mật):** Hoàn thành [`docs/checklist-bao-mat.md`](docs/checklist-bao-mat.md) rà soát trung thực 7 mục OWASP Top 10 kèm minh chứng thật.
