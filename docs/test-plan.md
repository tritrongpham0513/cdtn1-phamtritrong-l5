# Kế hoạch Kiểm thử Rút gọn (Test Plan)
## Luồng L5 — Quản lý Kho Linh kiện Thay thế (Track SE)
* **Hệ thống:** Mekong Mobile Smart CRM | **Sinh viên:** Phạm Trí Trọng — MSSV: 2374802010525
* **Công nghệ:** Java 17, Spring Boot 3.2.5, JUnit 5, Mockito, MockMvc, H2 in-memory
* **Vị trí lưu:** `docs/test-plan.md` — chép vào Mục 1 Báo cáo PDF BT3, độ dài ~1 trang A4

---

## 1. Phạm vi kiểm thử và phần chủ ý KHÔNG kiểm

### 1.1. In-Scope — 4 quy tắc nghiệp vụ cốt lõi
1. **Xuất kho theo phiếu bảo hành (US-03, US-04):** chỉ xuất khi phiếu `DANG_XU_LY`/`CHO_LINH_KIEN` (QT-06); kiểm tra đủ tồn và khóa bi quan chống xuất âm (QT-09, NFR-02).
2. **Nhập kho bổ sung (US-02):** tăng tồn thực tế và ghi sổ cái append-only `part_transactions`.
3. **Cảnh báo tồn tối thiểu (US-05):** bật `isLowStock = true` khi `quantity < minThreshold`.
4. **Kiểm soát quyền theo chủ sở hữu & vai trò (OWASP A01 / S2):** chặn kỹ thuật viên xuất linh kiện cho phiếu của trung tâm khác; chặn kỹ thuật viên gọi API nhập kho của Quản lý.

*NFR-04 (sổ cái bất biến) được đảm bảo bằng thiết kế append-only, không có API sửa/xóa; chưa có test tự động riêng trong phạm vi BT3 — bảo lưu cho CĐTN2.*

### 1.2. Out-of-Scope — lý do
1. **UI E2E (Selenium/Cypress):** 3 màn hình M1/M2/M3 kiểm thử thủ công; tự động hóa tập trung tầng Service và REST API backend.
2. **Hiệu năng và tải phân tán:** cần hạ tầng JMeter/k6, thuộc phạm vi CĐTN2.
3. **Điều chuyển linh kiện liên trung tâm:** tính năng *Won't Have* theo SRS BT1.

---

## 2. Loại test và số lượng mỗi tầng

Repo có **28 automated tests**: **23 unit + 2 integration + 3 API**.
Báo cáo BT3 tuyển chọn **12 test case tiêu biểu**: **7 unit + 2 integration + 3 API**.

* **23 Unit Tests:** `PartTransactionServiceTest` (7), `PartServiceTest` (5), `StockServiceTest` (4), `AuthTokenServiceTest` (5), `InventoryApplicationTests` (2).
* **2 Integration Tests (H2 thật):** `DataInitializerIntegrationTest` (seeder chạy 2 lần — C8) và `PartTransactionIntegrationTest` (export + import end-to-end).
* **3 API Slice Tests:** `TransactionControllerApiTest` — MockMvc standalone, kiểm tra HTTP 201, 422, 403.

**12 test case báo cáo theo loại:** Luồng đúng 5 · Giá trị biên 2 · Dữ liệu sai 2 · Bảo mật 2 · Tính tái lập 1.

---

## 3. Tiêu chí thành công ĐO ĐƯỢC

1. **Tỷ lệ vượt qua:** **28/28 test trong repo** và **12/12 test case báo cáo** PASS 100% qua một lệnh chạy duy nhất.
2. **Độ bao phủ quy tắc:** 100% quy tắc cốt lõi (QT-06, QT-09, US-02, US-05, A01/S2, C8) có test kiểm chứng trực tiếp.
3. **Tồn đọng lỗi:** 0 bug Critical và 0 bug Major đang mở.
4. **Không suy thoái:** sau mỗi lần sửa bug, 100% test còn lại vẫn PASS.

---

## 4. Môi trường và dữ liệu thử

* **Môi trường:** JDK 17 + Maven Wrapper. Windows: `.\mvnw.cmd test`; Linux/macOS: `./mvnw test`.
* **Thời gian:** unit test vài ms; API slice ~1–3s; integration H2 ~14–17s; tổng suite ~27s.
* **Cô lập:**
  * Unit/API: Mockito + `MockMvcBuilders.standaloneSetup()`, object mới trong `@BeforeEach`, không dùng DB.
  * Integration: `@SpringBootTest` + H2 in-memory (`src/test/resources/application.properties`), `ddl-auto=create-drop`, Flyway tắt; test seeder dùng `app.seed.enabled=true`, `app.seed.path=seeds`; test service dùng `@Transactional` để rollback.
* **Dữ liệu thử:**
  * *Seeds thật:* TT01/TT04/TT05; `MH-SS-01`, `PIN-SS-01`, `CAM-AP-01`; mã tồn 0 tại TT01: `PIN-OP-03`, `CS-SS-02`; phiếu `BH-000002/2025` (TT02, `DANG_XU_LY`), `BH-000003/2026` (TT06, `DA_DONG`), `BH-000022/2025` (TT01, `DANG_XU_LY`).
  * *Mock:* `PIN-IP15` (tồn 10, ngưỡng 5), `BH-000456/2026`, `BH-000789/2026`, token `ktv_trong`/`quanly_kho`.
  * *Integration:* `TT-INT-01`, `PIN-INT-01`, `BH-INT-0001/2026`, tồn 10.
* **Endpoint & mã HTTP:** `POST /api/v1/transactions/export`, `/import`; 201 (thành công), 422 (validation), 403 (RBAC), 400 (vi phạm chủ sở hữu), 409 (tồn kho/trạng thái phiếu).

---

> ### TRẢ LỜI CÂU HỎI VẤN ĐÁP: "LÀM SAO BIẾT ĐÃ KIỂM THỬ ĐỦ?"
> *"Thưa thầy, hệ thống có bốn nhóm quy tắc cốt lõi: xuất kho (QT-06, QT-09), nhập kho (US-02), tồn thấp (US-05), và phân quyền theo chủ sở hữu/vai trò (A01/S2). Mỗi nhóm có ít nhất một test luồng đúng; các quy tắc định lượng có cặp biên 10/11 và 5/4. Em có thêm hai test dữ liệu sai, hai test bảo mật, một test tái lập C8 ở mức integration, và ba test API (201, 422, 403). 12 test case báo cáo được chọn từ 28 test thật (PASS 100%), phân bổ 7 unit + 2 integration + 3 API. Em không kiểm thử tự động UI E2E mà kiểm thử thủ công 3 màn hình; NFR-04 đảm bảo bằng thiết kế append-only và bảo lưu test tự động cho CĐTN2 — đã ghi rõ trong Mục 1."*
