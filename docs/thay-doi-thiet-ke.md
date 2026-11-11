# Nhật ký thay đổi thiết kế

> **Dự án:** Smart CRM – Mekong Mobile  
> **Học phần:** Chuyên đề tốt nghiệp 1 · Học kỳ 1, Năm học 2026 – 2027  
> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525  
> **Chuyên ngành (Track):** Kỹ thuật Phần mềm (Software Engineering - SE)  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế  
> **Quy chuẩn trình bày:** Tuân thủ cấu trúc 3 dòng bắt buộc theo Rubric BT2 (*Thay đổi gì · Vì sao · Ảnh hưởng tới thành phần nào*)

---

## PHẦN 1: Thay đổi sau phản hồi Bài tập 1 (Buổi 8, ngày 24/10/2026)

### TD-01: Chuẩn hóa và đồng bộ tên kỹ thuật giữa Glossary, DDL, API Contract và Wireframe
- **Thay đổi gì:** Chuẩn hóa toàn bộ tên kỹ thuật bằng tiếng Anh bất biến trong CSDL và API: đổi bảng thành `part_transactions`, loại giao dịch thành `IMPORT`/`EXPORT`, và vòng đời 6 trạng thái phiếu bảo hành thành `TIEP_NHAN` -> `DA_PHAN_CONG` -> `DANG_XU_LY` -> `CHO_LINH_KIEN` -> `HOAN_TAT` -> `DA_DONG` (loại bỏ hoàn toàn các từ `'NHAP'`, `'XUAT'`, `'Mới'`).
- **Vì sao:** Thực hiện theo phản hồi trực tiếp của GVHD (TS. Nguyễn Trí Hải tại Buổi 8): Đảm bảo nguyên tắc cốt lõi của môn học — mỗi khái niệm chỉ dùng ĐÚNG MỘT TÊN kỹ thuật duy nhất trên toàn bộ vòng đời phân tích, thiết kế và cài đặt mã nguồn; tránh nhầm lẫn giữa nhãn hiển thị tiếng Việt trên UI và giá trị lưu trữ trong CSDL.
- **Ảnh hưởng tới thành phần nào:** `docs/srs.md` (Mục 1.1.4 Glossary, 4.2.5 Data Dictionary, 5.1 Business Rules), `docs/api-contract.md` (Mục 1.6, 2.3), `migrations/V1__init_schema.sql`, và nhãn Wireframe M1, M2, M3.

---

### TD-02: Chuẩn hóa liên kết Use Case UC-07 và UC-06 trên Wireframe M3 và M1
- **Thay đổi gì:** Sửa lại liên kết Use Case: quy tụ `US-06` (tra cứu theo linh kiện) và `US-07` (xem linh kiện theo phiếu) vào Use Case duy nhất là `UC-06 (Xem lịch sử giao dịch kho)` trên Màn hình M3; xác định rõ `UC-07` là `UC-07 (Thiết lập ngưỡng tối thiểu)` thực hiện qua Modal tại Màn hình M1.
- **Vì sao:** Khắc phục lỗi lệch liên kết được GVHD chỉ ra tại Buổi 8 (wireframe M3 ghi nhầm nhãn UC-07); đảm bảo tính truy vết hai chiều (Traceability) 100% nhất quán giữa Ma trận truy vết ↔ Sơ đồ Use Case ↔ Wireframe giao diện.
- **Ảnh hưởng tới thành phần nào:** `docs/srs.md` (Mục 2.1 Traceability Matrix, Mục 5.1 & 5.3 Wireframe M1 và M3).

---

### TD-03: Chuẩn hóa diễn đạt số lượng User Story tại Mục 1.3.3
- **Thay đổi gì:** Cập nhật dòng tổng kết số lượng User Story từ câu chữ cũ ("đạt chuẩn 5–7 story") thành "8 User Story (từ US-01 đến US-08), vượt mức tối thiểu đề bài, bao phủ 100% phạm vi nghiệp vụ luồng L5, tổng 26 Story Points".
- **Vì sao:** Khắc phục sự không nhất quán giữa con số thống kê và câu diễn đạt được GVHD góp ý tại Buổi 8; phản ánh chính xác quy mô hiện thực hóa của dự án.
- **Ảnh hưởng tới thành phần nào:** `docs/srs.md` (Mục 1.3.3 và Mục 3.3).

---

### TD-04: Khôi phục tài liệu API Contract và bổ sung cam kết liêm chính học thuật trong Phụ lục AI
- **Thay đổi gì:** Khôi phục toàn diện tài liệu đặc tả `docs/api-contract.md` (9 endpoints RESTful, request/response DTOs, bảng mã lỗi, JWT spec); bổ sung khẩu lệnh cam kết nguyên văn `> "Tôi xác nhận đã đọc, hiểu và chịu trách nhiệm về toàn bộ nội dung nộp."` kèm đầy đủ họ tên, MSSV, track và ngày ký.
- **Vì sao:** Thực hiện yêu cầu bắt buộc của GVHD tại Buổi 8: Track SE bắt buộc phải có API Contract để định nghĩa giao tiếp giữa frontend và backend; Phụ lục AI phải tuân thủ nghiêm ngặt Quy định Liêm chính học thuật của Trường Đại học Văn Lang.
- **Ảnh hưởng tới thành phần nào:** Tệp mới khôi phục `docs/api-contract.md`, tệp `docs/ai-disclosure.md` (và `docs/ai-declaration.md`).

---

## PHẦN 2: Thay đổi trong quá trình thực hiện Bài tập 2 (Hiện thực hóa module cốt lõi)

### TD-BT2-01: Tích hợp công cụ Flyway Database Migration tự động từ trạng thái CSDL trống
- **Thay đổi gì:** Thay thế tệp DDL tạo bảng thủ công `db/schema.sql` bằng kịch bản Migration tự động chuẩn `migrations/V1__init_schema.sql` (và `src/main/resources/db/migration/V1__init_schema.sql`) được kiểm soát phiên bản qua thư viện Flyway trong Spring Boot, kết hợp cấu hình `spring.jpa.hibernate.ddl-auto=validate`.
- **Vì sao:** Đạt mức Tự động hóa cao nhất theo Tiêu chí S3 & C1 của Rubric BT2; khi Giảng viên clone repo về máy mới, chỉ cần chạy đúng 1 lệnh khởi động là Flyway tự động phát hiện CSDL trống và dựng toàn bộ bảng, khóa chính, khóa ngoại, chỉ mục NFR và các ràng buộc toàn vẹn `CHECK (quantity >= 0)` mà không cần người dùng thao tác SQL thủ công.
- **Ảnh hưởng tới thành phần nào:** `pom.xml`, `application.properties`, `application-dev.properties`, thư mục `migrations/`, thư mục `src/main/resources/db/migration/`.

---

### TD-BT2-02: Bổ sung 4 thao tác CRUD đầy đủ cho Thực thể Part (Danh mục linh kiện)
- **Thay đổi gì:** Bổ sung các endpoint `POST /api/v1/parts` (Tạo mới), `GET /api/v1/parts` và `GET /api/v1/parts/{partCode}` (Đọc danh sách và chi tiết), `PUT /api/v1/parts/{partCode}` (Sửa thông tin linh kiện), `DELETE /api/v1/parts/{partCode}` (Xóa mềm - Soft Delete `isActive = false`).
- **Vì sao:** Trong thiết kế BT1, `Part` được coi là Master Data dùng chung chỉ đọc trong luồng L5; tuy nhiên Rubric BT2 (Tiêu chí S1 riêng cho Track SE) quy định bắt buộc: *"≥ 2 thực thể có đủ bốn thao tác tạo – đọc – sửa – xoá (CRUD)"*. Việc bổ sung này giúp hệ thống đạt điểm tuyệt đối 10/10 cho tiêu chí S1 mà vẫn bảo toàn tính cô lập của luồng L5.
- **Ảnh hưởng tới thành phần nào:** `PartController.java`, `PartService.java`, `PartServiceImpl.java`, các DTO (`CreatePartRequestDTO`, `UpdatePartRequestDTO`, `PartResponseDTO`), `PartRepository.java`, `PartServiceTest.java`.

---

### TD-BT2-03: Hoàn thiện đủ 4 thao tác CRUD cho Thực thể PartStock (Tồn kho linh kiện trung tâm)
- **Thay đổi gì:** Bổ sung endpoint khởi tạo tồn kho `POST /api/v1/stock` và endpoint xóa bản ghi tồn kho `DELETE /api/v1/stock/{partCode}` (với điều kiện ràng buộc số lượng tồn bằng 0), kết hợp với 2 thao tác Đọc (`GET /api/v1/stock`) và Sửa ngưỡng an toàn (`PUT /api/v1/stock/threshold`) đã thiết kế từ BT1.
- **Vì sao:** Đảm bảo thực thể nghiệp vụ thứ hai (`PartStock`) đáp ứng trọn vẹn 100% tiêu chí S1 của Track SE về đủ 4 thao tác vòng đời dữ liệu Tạo – Đọc – Sửa – Xóa; hỗ trợ Quản lý kho chủ động cấu hình linh kiện mới về trung tâm và thu hồi mã tồn kho không còn sử dụng.
- **Ảnh hưởng tới thành phần nào:** `StockController.java`, `StockService.java`, `StockServiceImpl.java`, `CreateStockRequestDTO.java`, `PartStockRepository.java`, `js/api.js`, `js/app.js`, `index.html`.

---

### TD-BT2-04: Hiện thực hóa Khóa bi quan (@Lock(LockModeType.PESSIMISTIC_WRITE)) chống xuất âm kho
- **Thay đổi gì:** Bổ sung phương thức truy vấn `findByCenterIdAndPartCodeForUpdate` có gắn chú thích `@Lock(LockModeType.PESSIMISTIC_WRITE)` trong `PartStockRepository`, phát sinh câu lệnh SQL `SELECT ... FOR UPDATE` khóa chặt bản ghi tồn kho tại tầng CSDL InnoDB ngay khi KTV bắt đầu tạo giao dịch xuất kho.
- **Vì sao:** Trong BT1, quy tắc QT-09 và NFR-02 chỉ dừng ở mức yêu cầu nghiệp vụ ngăn xuất âm kho nhưng chưa chỉ định giải pháp chống tranh chấp đồng thời (Concurrency Control); việc cài đặt Khóa bi quan loại trừ 100% rủi ro Race Condition khi 2 KTV cùng xuất 1 linh kiện còn tồn 1 cái tại cùng một thời điểm.
- **Ảnh hưởng tới thành phần nào:** `PartStockRepository.java`, `PartTransactionServiceImpl.java`, `PartTransactionServiceTest.java`.

---

### TD-BT2-05: Nâng cấp Data Seeder chống trùng lặp theo Khóa duy nhất (Unique Key) và đối soát tồn kho NFR-04
- **Thay đổi gì:** Thay thế cơ chế kiểm tra nạp dữ liệu bằng ngưỡng số lượng (`count >= 1000`) bằng cơ chế kiểm tra tồn tại theo Khóa duy nhất (`Set<String>` của `partCode`, `centerCode`, `ticketCode`, `transactionCode`), đồng thời tự động đối soát tồn kho thực tế $\text{quantity} = \sum(\text{nhập}) - \sum(\text{xuất})$ và hỗ trợ cờ tắt/bật `app.seed.enabled`.
- **Vì sao:** Ngăn ngừa triệt để lỗi trùng khóa chính (`Duplicate entry`) nếu quá trình nạp dữ liệu bị gián đoạn giữa chừng; đảm bảo nguyên tắc bất biến NFR-04 và giúp hệ thống vượt qua Phép thử tự kiểm C5: chạy lại lần thứ hai không lỗi và không nhân đôi dữ liệu.
- **Ảnh hưởng tới thành phần nào:** `src/main/java/com/mekongmobile/inventory/config/DataInitializer.java`, thư mục `seeds/`, `src/test/resources/application.properties`.

---

### TD-BT2-06: Hiện thực hóa tầng Xác thực Token và Ma trận Phân quyền RBAC (Role-Based Access Control)
- **Thay đổi gì:** Bổ sung dịch vụ xác thực token và phân quyền `AuthTokenService`, ngoại lệ bảo mật `ForbiddenException` (HTTP 403 Forbidden theo RFC-7807), áp dụng ma trận phân quyền chi tiết cho Kỹ thuật viên (`TECHNICIAN`) và Quản lý kho (`MANAGER`), đồng thời tích hợp logic ẩn/khóa nút nghiệp vụ trên Giao diện Frontend (`app.js`, `index.html`) và bổ sung 5 test case kiểm thử tự động trong `AuthTokenServiceTest`.
- **Vì sao:** Đáp ứng trọn vẹn yêu cầu Thành phần 3 (Track SE: "xác thực đăng nhập ở mức cơ bản") và quy tắc phân quyền nghiêm ngặt QT-14 & Mục 2.2 của `docs/api-contract.md`:
  - Kỹ thuật viên (`TECHNICIAN`): Chỉ được phép tạo phiếu xuất kho linh kiện sửa chữa; bị cấm tuyệt đối (HTTP 403) không được Nhập kho, không được Thêm/Sửa/Xóa linh kiện và Sửa định mức tồn.
  - Quản lý kho (`MANAGER`): Được phép Nhập kho, Sửa định mức tồn tối thiểu, CRUD danh mục linh kiện; bị cấm tuyệt đối (HTTP 403) không được tự ý xuất kho linh kiện thay cho KTV nhận máy.
- **Ảnh hưởng tới thành phần nào:** `AuthTokenService.java`, `ForbiddenException.java`, `GlobalExceptionHandler.java`, `TransactionController.java`, `StockController.java`, `PartController.java`, `AuthTokenServiceTest.java`, `js/api.js`, `js/app.js`, `index.html`.

---

### TD-BT2-07: Đóng gói Container hóa với Dockerfile Multi-stage và Docker Compose (Thành phần 4 - Mức đánh giá cao)
- **Thay đổi gì:** Bổ sung tệp `Dockerfile` đa tầng (Multi-stage build trên nền Eclipse Temurin 17 JRE tối giản, chạy non-root user `mekonguser`) và tệp `docker-compose.yml` tích hợp sẵn container CSDL MySQL 8.0 và dịch vụ Spring Boot có cơ chế kiểm tra sức khỏe `service_healthy`.
- **Vì sao:** Đáp ứng mức đánh giá cao của Thành phần 4 (cho phép dựng toàn bộ hệ thống gồm CSDL và Ứng dụng chỉ bằng đúng một lệnh `docker compose up -d`), cộng điểm trực tiếp vào Tiêu chí 2 của Rubric, đồng thời là bước chuẩn bị trực tiếp cho việc triển khai ở Bài tập 3.
- **Ảnh hưởng tới thành phần nào:** `Dockerfile`, `docker-compose.yml`, `.dockerignore`, `README.md`.

---

## PHẦN 3: Thay đổi trong quá trình thực hiện Bài tập 3 (Kiểm thử và Đóng gói prototype)

### TD-BT3-01: Ràng buộc `X-Center-Id` với trung tâm ghi trong token (BUG-BT3-01)
- **Thay đổi gì:** Bổ sung `centerId` vào token (`userId:username:role:centerId:issuedAt`) và thêm `AuthTokenService.requireRoleAndCenter()`; áp dụng cho `POST /transactions/import`, `POST /transactions/export`, `POST/PUT/DELETE /stock`.
- **Vì sao:** Trước khi sửa, client tự gửi `X-Center-Id` và service tin tưởng giá trị này, nên KTV TT01 có thể xuất linh kiện cho phiếu TT04 và Quản lý kho có thể thao tác kho trung tâm khác (lỗ hổng OWASP A01). Header lệch trung tâm trong token nay bị từ chối bằng HTTP 403.
- **Ảnh hưởng tới thành phần nào:** `AuthTokenService.java`, `AuthController.java`, `TransactionController.java`, `StockController.java`, `AuthTokenServiceTest.java`, `TransactionControllerApiTest.java`, `docs/bug-log.md`.

---

### TD-BT3-02: `/auth/me` trả đúng danh tính phiên đăng nhập (BUG-BT3-02)
- **Thay đổi gì:** `AuthController` inject `AuthTokenService`, bóc tách `username` từ token và tra đúng `UserProfile` thay vì hard-code `MOCK_USERS.get("ktv_trong")`; token thiếu/không hợp lệ trả HTTP 403.
- **Vì sao:** Trước khi sửa, mọi phiên đăng nhập đều nhận về cùng một danh tính `ktv_trong` (TECHNICIAN), khiến giao diện hiển thị sai người dùng và cấp sai quyền.
- **Ảnh hưởng tới thành phần nào:** `AuthController.java`, `AuthControllerTest.java`, `docs/bug-log.md`.

---

### TD-BT3-03: Không lộ chi tiết nội bộ trong phản hồi 500 (BUG-BT3-03)
- **Thay đổi gì:** `GlobalExceptionHandler.handleGenericException()` ghi log đầy đủ ở server và trả thông báo trung tính `"Đã xảy ra lỗi hệ thống. Vui lòng liên hệ quản trị viên."` với mã `INTERNAL_SERVER_ERROR`.
- **Vì sao:** Trước khi sửa, body lỗi chứa nguyên `ex.getMessage()`, làm lộ tên bảng, kiểu dữ liệu và chuỗi kết nối CSDL (tiêu chí S4).
- **Ảnh hưởng tới thành phần nào:** `GlobalExceptionHandler.java`, `GlobalExceptionHandlerTest.java`, `docs/bug-log.md`.

---

### TD-BT3-04: Chuẩn hóa gói đóng gói Docker Compose cho phép thử máy trắng (Thành phần 4)
- **Thay đổi gì:** Parameter hóa toàn bộ biến trong `docker-compose.yml` theo `.env` với giá trị mặc định an toàn; `.env.example` liệt kê đủ biến (`APP_PORT`, `MYSQL_HOST_PORT`, `MYSQL_*`, `SPRING_PROFILES_ACTIVE`, `SEED_*`); `Dockerfile` viết lại rõ hai tầng và kiểm tra CRLF cho `mvnw`; README ghi đúng 2 lệnh `cp .env.example .env` và `docker compose up -d --build`.
- **Vì sao:** Phép thử máy trắng yêu cầu clone từ tag `v1.0` vào thư mục trống và dựng hệ thống bằng không quá hai lệnh, không cần hỏi thêm. Cấu hình cũ hard-code mật khẩu và thiếu biến cổng trong `.env.example`, dễ gây lệch giữa tài liệu và lệnh chạy thực tế.
- **Ảnh hưởng tới thành phần nào:** `docker-compose.yml`, `Dockerfile`, `.env.example`, `README.md`, `docs/images/`.

---

### TD-BT3-05: Healthcheck chỉ báo `healthy` sau khi Flyway và Seeder hoàn tất
- **Thay đổi gì:** Thêm `StartupReadiness` lắng nghe `ApplicationReadyEvent`; `GET /api/health` trả HTTP 503 `STARTING` trong lúc nạp dữ liệu và chỉ trả HTTP 200 `UP` khi seeder đã xong. `docker-compose.yml` dùng endpoint này cho healthcheck.
- **Vì sao:** Trước khi sửa, Tomcat nhận request trước khi `CommandLineRunner` nạp xong 7.800 phiếu và 4.954 giao dịch, nên container có thể được báo `healthy` khi dữ liệu mẫu còn đang nạp. Việc này làm phép thử "hệ thống lên được và có dữ liệu mẫu" không ổn định trên máy chậm.
- **Ảnh hưởng tới thành phần nào:** `StartupReadiness.java`, `HealthController.java`, `docker-compose.yml`, `README.md`.

---

### TD-BT3-06: Mật khẩu lưu dạng băm BCrypt trong bảng `users` (tiêu chí S3)
- **Thay đổi gì:** Thêm entity `User`, `UserRepository`, bean `BCryptPasswordEncoder` trong `SecurityConfig`, migration `V2__add_users_table.sql` (bảng `users`, 3 tài khoản demo với `password_hash` BCrypt cost 10) và `seedUsers()` idempotent trong `DataInitializer`. `AuthController` bỏ hằng `MOCK_USERS` plaintext, đọc tài khoản từ CSDL và đối chiếu bằng `passwordEncoder.matches()`.
- **Vì sao:** Trước khi sửa, mật khẩu `123456` nằm dạng plaintext ngay trong mã nguồn và CSDL không có bảng `users`, nên không đạt tiêu chí S3 (OWASP A04/A07). Sau khi sửa, kể cả đọc trực tiếp bảng `users` cũng không suy ra được mật khẩu gốc.
- **Ảnh hưởng tới thành phần nào:** `pom.xml`, `entity/User.java`, `repository/UserRepository.java`, `config/SecurityConfig.java`, `controller/AuthController.java`, `config/DataInitializer.java`, `migrations/V2__add_users_table.sql`, `docs/checklist-bao-mat.md`, `docs/api-docs/README.md`, `docs/deployment-guide.md`.

---

### TD-BT3-07: Healthcheck MySQL kiểm tra qua TCP để tránh báo `healthy` sớm (phép thử máy trắng C1)
- **Thay đổi gì:** Đổi healthcheck của service `mysql-db` từ `mysqladmin ping -h localhost` sang `mysql -h 127.0.0.1 -u$MYSQL_USER -p$MYSQL_PASSWORD -e 'SELECT 1' $MYSQL_DATABASE`; bổ sung `spring.flyway.connect-retries=10` và `connect-retries-interval=3s` trong `application-dev.properties`.
- **Vì sao:** Khi dựng lại từ thư mục trắng, `mysqladmin ping -h localhost` trả mã 0 ngay cả lúc MySQL mới chạy *temporary server* (port 0, chỉ mở socket) trong giai đoạn khởi tạo. Docker Compose vì thế đánh dấu MySQL `healthy` và khởi động ứng dụng, nhưng ngay sau đó temporary server dừng để chuyển sang server chính thức; ứng dụng kết nối bị `Connection refused` và container phải restart. Lần chạy đầu trả `UP_EXIT=1` dù cuối cùng hệ thống vẫn lên — không đạt yêu cầu "2 lệnh, không cần hỏi".
- **Ảnh hưởng tới thành phần nào:** `docker-compose.yml`, `src/main/resources/application-dev.properties`, `README.md`, `docs/ai-disclosure.md`.
