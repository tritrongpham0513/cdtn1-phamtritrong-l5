# Kế hoạch Kiểm thử Rút gọn (Test Plan)
## Luồng L5 — Quản lý Kho Linh kiện Thay thế (Track SE)
* **Hệ thống:** Mekong Mobile Smart CRM | **Sinh viên:** Phạm Trí Trọng — MSSV: 2374802010525
* **Công nghệ:** Java 17, Spring Boot 3.2.5, JUnit 5, Mockito, MockMvc, H2 in-memory | **Tệp:** `docs/test-plan.md`

## 1. Phạm vi kiểm thử và phần chủ ý KHÔNG kiểm

**1.1. In-Scope — 6 quy tắc cốt lõi**
1. **Xuất kho theo phiếu (FR-03, US-03, QT-06):** chỉ xuất khi phiếu `DANG_XU_LY`/`CHO_LINH_KIEN`; phiếu `DA_DONG` bị từ chối ở Service và H2.
2. **Khóa bi quan chống xuất âm (FR-04, US-04, QT-09):** kiểm tra đủ tồn, phát sinh `SELECT ... FOR UPDATE`, từ chối khi vượt số lượng.
3. **Nhập kho (FR-02, US-02):** tăng tồn thực tế và ghi giao dịch `IMPORT` vào `part_transactions`.
4. **Cảnh báo tồn thấp (FR-05, US-05):** bật `isLowStock` khi `quantity < minThreshold`.
5. **Kiểm soát quyền theo chủ sở hữu (OWASP A01 / S2):** chặn KTV xuất phiếu của trung tâm khác.
6. **Tính tái lập dữ liệu (C8):** chạy lại `DataInitializer` không lỗi, không nhân đôi dữ liệu.

**1.2. Out-of-Scope:** UI E2E M1/M2/M3 kiểm thử thủ công; tải/đồng thời NFR-01/02 thuộc CĐTN2; đối soát toàn vẹn NFR-04 mới kiểm tra số bản ghi sổ cái tăng; FR-01/06/07, Part CRUD, Stock CRUD chưa có test tự động (đã nghiệm thu thủ công ở BT2); `GET /stock`, `GET /transactions` chưa kiểm tra token/center; điều chuyển liên trung tâm là *Won't Have*.

## 2. Loại test và số lượng mỗi tầng

Đúng **12 automated test cases** theo Kim tự tháp: **7 Unit** (`PartTransactionServiceTest`, TC-01…TC-07, Mockito) + **3 Integration** (`DataInitializerIntegrationTest` TC-08 tái lập C8; `PartTransactionIntegrationTest` TC-09 export+import end-to-end, TC-10 từ chối phiếu `DA_DONG`) + **2 API Slice** (`TransactionControllerApiTest` TC-11 HTTP 201, TC-12 HTTP 422).

**Kỹ thuật dữ liệu:** phân vùng tương đương (trạng thái phiếu, `quantity > 0` vs `= 0`, cùng TT01 vs khác TT02) và giá trị biên (xuất 10/10 về 0 vs 11/10 bị từ chối; tồn 4 < 5 bật cảnh báo vs tồn 5 == 5 tắt).

**Phân bổ 5 loại:** Luồng đúng (5): TC-01, TC-02, TC-03, TC-09, TC-11. Biên (2): TC-04, TC-05. Dữ liệu sai (3): TC-06, TC-10, TC-12. Bảo mật/S2 (1): TC-07. Tái lập/C8 (1): TC-08.

## 3. Tiêu chí thành công ĐO ĐƯỢC

1. **Tỷ lệ vượt qua:** 12/12 PASS 100% bằng một lệnh `.\mvnw.cmd test` / `./mvnw test`.
2. **Độ bao phủ:** 100% quy tắc trong phạm vi (QT-06, QT-09, US-02, US-05, A01, C8) có test trực tiếp.
3. **Tồn đọng lỗi:** 0 Critical, 0 Major đang mở; bug đã sửa ghi tại `docs/bug-log.md`.
4. **Không hồi quy:** 100% test vẫn PASS sau mỗi lần sửa bug/refactor.

## 4. Môi trường và dữ liệu thử

* **Môi trường:** JDK 17 LTS + Maven Wrapper; `.\mvnw.cmd test` (Windows) / `./mvnw test` (Linux/macOS).
* **CSDL:** H2 in-memory `jdbc:h2:mem:testdb`, `ddl-auto=create-drop`, tắt Flyway/seeder cho Unit/API; TC-08 bật `app.seed.enabled=true`.
* **Cô lập:** Unit/API dùng Mockito và `MockMvcBuilders.standaloneSetup()`; Integration dùng `@SpringBootTest` + `@Transactional` rollback, TC-08 dùng `@DirtiesContext`.
* **Dữ liệu:** Seeds thật cho TC-08 (6 centers, 180 parts, 7.800 tickets, 717 stocks, 4.954 transactions); fixture tự tạo cho các TC còn lại.
* **Endpoint:** `POST /api/v1/transactions/export` — 201 Created và 422 Unprocessable Entity.
