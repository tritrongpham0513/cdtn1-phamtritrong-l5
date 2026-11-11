# Kế hoạch Kiểm thử Rút gọn (Test Plan)
## Luồng L5 — Quản lý Kho Linh kiện Thay thế (Track SE)
* **Hệ thống:** Mekong Mobile Smart CRM | **Sinh viên:** Phạm Trí Trọng — MSSV: 2374802010525
* **Công nghệ:** Java 17, Spring Boot 3.2.5, JUnit 5, Mockito, MockMvc, H2 in-memory
* **Vị trí lưu:** `docs/test-plan.md`

---

## 1. Phạm vi kiểm thử và phần chủ ý KHÔNG kiểm

### 1.1. In-Scope — Các quy tắc nghiệp vụ cốt lõi được kiểm thử
1. **Xuất kho theo phiếu bảo hành (FR-03, US-03, QT-06):** Chỉ cho phép xuất khi phiếu ở trạng thái `DANG_XU_LY` hoặc `CHO_LINH_KIEN`. Phiếu ở trạng thái khác (như `DA_DONG`) bị từ chối cả ở tầng Service lẫn CSDL H2.
2. **Khóa bi quan chống xuất âm kho (FR-04, US-04, QT-09, NFR-02 mức đơn luồng):** Kiểm tra đủ tồn kho khả dụng; phát sinh truy vấn `SELECT ... FOR UPDATE` khóa bản ghi tồn; từ chối và giữ nguyên tồn kho khi yêu cầu vượt quá số tồn.
3. **Ghi nhận nhập kho bổ sung (FR-02, US-02):** Tăng tồn kho thực tế và ghi nhận giao dịch `IMPORT` vào sổ cái `part_transactions`.
4. **Cảnh báo tồn tối thiểu (FR-05, US-05):** Kích hoạt cảnh báo `isLowStock = true` khi `quantity < minThreshold`.
5. **Kiểm soát quyền theo chủ sở hữu (OWASP A01 / Tiêu chí S2):** Chặn kỹ thuật viên xuất linh kiện cho phiếu thuộc trung tâm khác tại tầng Service.
6. **Tính tái lập dữ liệu hạt giống (Tiêu chí C8):** Kiểm tra DataInitializer chạy lại lần thứ hai không gây lỗi và không nhân đôi dữ liệu trên CSDL H2.

### 1.2. Out-of-Scope & Known Gaps — Phần chủ ý không kiểm kèm lý do kỹ thuật
1. **Giao diện người dùng E2E (Selenium/Cypress):** 3 màn hình M1/M2/M3 được kiểm thử thủ công qua trình duyệt; bộ kiểm thử tự động tập trung 100% vào tầng Service và REST API backend.
2. **Kiểm thử tải và truy cập đồng thời (NFR-01, NFR-02 mức 50 luồng đồng thời):** Cần hạ tầng phân tán JMeter/k6, thuộc phạm vi Chuyên đề tốt nghiệp 2. Bộ test hiện tại chỉ kiểm tra cơ chế khóa đơn luồng.
3. **Kiểm tra tính bất biến toán học và đối soát toàn vẹn (NFR-04):** Hiện chỉ kiểm tra số lượng bản ghi sổ cái tăng sau giao dịch đơn lẻ; chưa có kiểm thử đối soát toán học toàn bộ 10.000 giao dịch.
4. **Các tính năng tra cứu và CRUD chưa có test tự động:** FR-01 (xem tồn kho/tìm kiếm/lọc), FR-06 (lịch sử giao dịch/lọc/phân trang), FR-07 (cập nhật ngưỡng), Part CRUD và Stock CRUD. Các tính năng này đã nghiệm thu thủ công trong BT2 nhưng nằm ngoài phạm vi 12 ca kiểm thử trọng tâm của BT3.
5. **Kiểm soát quyền truy cập ở các endpoint đọc (A01):** Các API `GET /api/v1/stock`, `GET /api/v1/transactions` chưa tích hợp kiểm tra token/center; đây là hạn chế đã ghi nhận trong kiến trúc hiện tại.
6. **Điều chuyển linh kiện liên trung tâm:** Tính năng *Won't Have* theo SRS.

---

## 2. Loại test và số lượng mỗi tầng

Hệ thống triển khai đúng **12 automated test cases** theo mô hình Kim tự tháp kiểm thử:
* **7 Unit Tests (Service Logic):** Lớp `PartTransactionServiceTest` (TC-01 đến TC-07) — dùng Mockito cô lập phụ thuộc CSDL, chạy trong mili-giây.
* **3 Integration Tests (CSDL H2 thật):**
  * `DataInitializerIntegrationTest#testSeeder_RunTwice_DoesNotDuplicateData` (TC-08) — kiểm thử tính tái lập C8 trên H2.
  * `PartTransactionIntegrationTest#testExportThenImport_EndToEnd_PersistsStockAndLedger` (TC-09) — kiểm thử luồng xuất và nhập kho end-to-end lưu trữ H2.
  * `PartTransactionIntegrationTest#testExportPart_InvalidTicketStatus_DA_DONG_ThrowsException` (TC-10) — kiểm thử từ chối xuất cho phiếu `DA_DONG` trên H2 và xác nhận sổ cái không phát sinh bản ghi rác.
* **2 API Slice Tests (MockMvc Controller):** Lớp `TransactionControllerApiTest` (TC-11 và TC-12) — kiểm tra trọn vẹn mã HTTP 201 Created và HTTP 422 Unprocessable Entity kèm cấu trúc lỗi `ApiResponseDTO`.

**Kỹ thuật thiết kế dữ liệu thử nghiệm:**
* *Phân vùng tương đương (Equivalence Partitioning):* Chia miền giá trị thành các phân vùng: trạng thái phiếu (`DANG_XU_LY` hợp lệ vs `DA_DONG` không hợp lệ); số lượng (`quantity > 0` hợp lệ vs `quantity = 0` không hợp lệ); quyền sở hữu trung tâm (cùng TT01 vs khác TT02).
* *Phân tích giá trị biên (Boundary Value Analysis):* Thử nghiệm tại ranh giới tồn kho (xuất đúng 10/10 tồn về 0 vs xuất vượt 11/10 bị từ chối) và ranh giới cảnh báo tồn thấp (tồn 4 < 5 bật cảnh báo vs tồn 5 == 5 tắt cảnh báo).

**Phân bổ 12 test case theo 5 loại đề bài BT3 yêu cầu:**
* **Luồng đúng (5):** TC-01 (xuất kho QT-06/QT-09), TC-02 (nhập kho US-02), TC-03 (cảnh báo tồn thấp US-05), TC-09 (end-to-end H2), TC-11 (API xuất kho HTTP 201).
* **Giá trị biên (2):** TC-04 (xuất 10/10 -> tồn về 0, PASS) và TC-05 (xuất 11/10 vượt tồn 1 đơn vị -> từ chối).
* **Dữ liệu sai (3):** TC-06 (unit: phiếu `DA_DONG` -> từ chối), TC-10 (integration: phiếu `DA_DONG` trên H2 -> từ chối, sổ cái không đổi), TC-12 (API: quantity = 0 -> HTTP 422 `VALIDATION_ERROR`).
* **Bảo mật / S2 (1):** TC-07 (chặn KTV xuất phiếu trung tâm khác theo OWASP A01).
* **Tính tái lập / C8 (1):** TC-08 (seeder chạy lần 2 trên H2 không lỗi, số bản ghi 5 bảng giữ nguyên).

---

## 3. Tiêu chí thành công ĐO ĐƯỢC

1. **Tỷ lệ vượt qua:** **12/12 test case PASS 100%** qua một lệnh chạy duy nhất (`.\mvnw.cmd test` / `./mvnw test`).
2. **Độ bao phủ quy tắc cốt lõi:** 100% quy tắc nghiệp vụ trong phạm vi đã chọn (QT-06, QT-09, US-02, US-05, A01, C8) có test kiểm chứng trực tiếp.
3. **Tồn đọng lỗi:** 0 bug Critical và 0 bug Major đang mở (các bug phát hiện trong kiểm thử đã sửa và ghi vào `docs/bug-log.md`).
4. **Tính ổn định (No Regression):** 100% test case vẫn PASS sau mỗi lần sửa bug hoặc refactor mã nguồn.

---

## 4. Môi trường và dữ liệu thử

* **Môi trường thực thi:** JDK 17 LTS + Apache Maven Wrapper. Lệnh chạy: `.\mvnw.cmd test` (Windows) / `./mvnw test` (Linux/macOS).
* **Cơ sở dữ liệu kiểm thử:** H2 in-memory độc lập (`jdbc:h2:mem:testdb`), `ddl-auto=create-drop`, `spring.flyway.enabled=false`, `app.seed.enabled=false` cho Unit và API; riêng test seeder TC-08 bật `app.seed.enabled=true`, `app.seed.path=seeds`.
* **Cơ chế cô lập và dọn dẹp:**
  * *Unit & API:* Dùng Mockito và `MockMvcBuilders.standaloneSetup()`, khởi tạo đối tượng mới độc lập trong `@BeforeEach`, không lưu CSDL nên không cần dọn dẹp.
  * *Integration:* Chạy `@SpringBootTest` kết hợp `@Transactional` để tự động rollback sạch dữ liệu sau mỗi test; test seeder dùng `@DirtiesContext` giải phóng context.
* **Dữ liệu thử nghiệm:**
  * *Dữ liệu hạt giống thật (Seeds):* Dùng cho test seeder TC-08 từ thư mục `seeds/` (6 centers, 180 parts, 7.800 tickets, 717 stocks, 4.954 transactions).
  * *Dữ liệu giả lập (Fixtures tự tạo trong test):* TC-01 đến TC-07 dùng mock entity tự dựng trong test code; TC-09 và TC-10 dùng fixture tự lưu vào H2 (`TT-INT-01`, `PIN-INT-01`, `BH-INT-0001/2026`, `BH-INT-0002/2026`).
* **Endpoint & mã HTTP đã kiểm thử:** `POST /api/v1/transactions/export` kiểm thử mã 201 (Created) và mã 422 (Unprocessable Entity).

---

> ### TRẢ LỜI CÂU HỎI VẤN ĐÁP: "LÀM SAO EM BIẾT LÀ EM ĐÃ KIỂM THỬ ĐỦ?"
> *"Thưa thầy, hệ thống có sáu quy tắc nghiệp vụ cốt lõi trong phạm vi kiểm thử tự động của Luồng L5: xuất kho (QT-06, QT-09), nhập kho (US-02), cảnh báo tồn thấp (US-05), bảo mật phân quyền theo chủ sở hữu (OWASP A01 / S2), và tính tái lập dữ liệu (C8). Em biết em đã kiểm thử ĐỦ vì:*
> *1. Mỗi quy tắc cốt lõi đều có ít nhất một test luồng đúng; các quy tắc định lượng có cặp giá trị biên ở hai bên ranh giới (xuất 10/10 tồn về 0, xuất 11/10 bị từ chối; tồn 4 < 5 cảnh báo, tồn 5 == 5 tắt cảnh báo; quantity = 1 hợp lệ, quantity = 0 bị từ chối).*
> *2. Có ba test dữ liệu sai kiểm tra cả thông báo lỗi ở tầng service/CSDL H2 (phiếu đã đóng DA_DONG) và mã trạng thái HTTP 422 kèm trường chi tiết lỗi ở tầng API.*
> *3. Có một test bảo mật kiểm soát quyền truy cập theo chủ sở hữu (A01: chặn KTV xuất phiếu trung tâm khác) và một test tính tái lập C8 kiểm chứng seeder chạy lại không nhân đôi dữ liệu.*
> *Bộ kiểm thử gồm đúng 12 test case phân bổ theo Kim tự tháp: 7 Unit + 3 Integration + 2 API, đạt 12/12 PASS 100%. Các phần chưa có test tự động như UI E2E (đã kiểm thủ công), hiệu năng tải phân tán (thuộc CĐTN2), và các API đọc chưa kiểm tra auth em đã minh bạch ghi rõ vào Mục 1 (Out-of-Scope và Known Gaps) của Test Plan."*
