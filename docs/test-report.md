# Test Report — 12 test case đã CHẠY THẬT
## Thành phần 2 — Bài tập 3: Kiểm thử và Đóng gói prototype

* **Dự án:** Mekong Mobile Smart CRM — Luồng L5 Quản lý Kho Linh kiện Thay thế (Track SE)
* **Sinh viên:** Phạm Trí Trọng — MSSV: 2374802010525
* **Ngày chạy:** 10/11/2026
* **Môi trường:** Windows 11 · JDK 17.0.14 · Apache Maven 3.9.15 · Spring Boot 3.2.5 · JUnit 5 · Mockito · MockMvc · H2 in-memory
* **Lệnh chạy:** `.\mvnw.cmd clean test` (Windows) / `./mvnw test` (Linux/macOS)
* **Kết quả tổng:** **32/32 test trong repo PASS** · **12/12 test case báo cáo PASS** · 0 failures · 0 errors · 0 skipped · BUILD SUCCESS.

**Phân bổ 12 test case báo cáo:** 7 Unit + 2 Integration + 3 API.
**Phân bổ theo loại:** Luồng đúng 5 · Giá trị biên 2 · Dữ liệu sai 2 · Bảo mật 2 · Tính tái lập 1.

---

## 1. Đối chiếu yêu cầu mục 2.2 của đề bài

| Yêu cầu | Số lượng tối thiểu | Test case đáp ứng | Minh chứng |
|---|---|---|---|
| Test cho luồng đúng, mỗi quy tắc nghiệp vụ đã hiện thực có ít nhất 1 test | 4 quy tắc → ≥ 4 test | **TC-01, TC-02, TC-03, TC-09, TC-10** | 5 test luồng đúng: TC-01 (QT-06/QT-09), TC-02 (US-02), TC-03 (US-05), TC-09 (end-to-end), TC-10 (API happy path + quyền hợp lệ của TECHNICIAN tại TT01) |
| Test giá trị biên, hai test ở hai bên cùng một ranh giới | 2 | **TC-04, TC-05** | Ranh giới tồn kho = 10: xuất 10 → tồn 0 (PASS); xuất 11 → bị từ chối |
| Test dữ liệu sai, kiểm cả mã trạng thái và nội dung thông báo lỗi | 2 | **TC-06, TC-11** | TC-06 kiểm exception + thông báo `DA_DONG`; TC-11 kiểm HTTP 422 + `VALIDATION_ERROR` + `error.details.quantity` |
| Test bảo mật hoặc chất lượng dữ liệu | ≥ 1 | **TC-07, TC-12** | TC-07 chặn truy cập chéo trung tâm (A01/S2); TC-12 chặn TECHNICIAN gọi API nhập kho (RBAC → 403) |
| Test tính tái lập: chạy lại lần hai cho kết quả giống lần đầu | 1 | **TC-08** | Seeder chạy lần 2: không exception, count 5 bảng không đổi |
| Tổng số test case | 10–12 | **12** | 7 Unit + 2 Integration + 3 API |

---

## 2. Bảng 12 test case đã chạy thật (đủ 8 cột)

| Mã | Tiền điều kiện | Các bước | Dữ liệu thử | Kết quả mong đợi | Mức ưu tiên | Kết quả thực tế | PASS/FAIL |
|---|---|---|---|---|---|---|---|
| **TC-01**<br>Unit — Luồng đúng | `PartTransactionServiceTest`; mock `PIN-IP15` tồn 10, ngưỡng 5; phiếu `BH-000456/2026` trạng thái `DANG_XU_LY` thuộc TT01. | 1. Tạo `ExportTransactionRequestDTO` quantity = 2.<br>2. Gọi `exportPart(1L, request)`.<br>3. Kiểm tra response, tồn kho và sổ cái. | `ticketId=BH-000456/2026`; `partId=PIN-IP15`; `quantity=2`; `performedBy=KTV - Phạm Trí Trọng`. | Response `transactionType=EXPORT`; tồn 10 → 8; `remainingStock=8`; `partTransactionRepository.save()` gọi đúng 1 lần. | Cao | PASS — response `EXPORT`, tồn 8, `remainingStock=8`, sổ cái 1 bản ghi. | **PASS** |
| **TC-02**<br>Unit — Luồng đúng | `PartTransactionServiceTest`; mock `PIN-IP15` tồn 10, ngưỡng 5. | 1. Tạo `ImportTransactionRequestDTO` quantity = 20.<br>2. Gọi `importPart(1L, request)`.<br>3. Kiểm tra response, tồn kho và sổ cái. | `partId=PIN-IP15`; `quantity=20`; `performedBy=Quản lý kho`. | Tồn 10 → 30; response `IMPORT`; `remainingStock=30`; `isLowStock=false`; sổ cái 1 bản ghi `IMPORT`. | Cao | PASS — tồn 30, response `IMPORT`, `isLowStock=false`, sổ cái 1 bản ghi. | **PASS** |
| **TC-03**<br>Unit — Luồng đúng (US-05) | `InventoryApplicationTests`; tạo 3 `PartStock` với các cặp quantity/minThreshold. | 1. Tạo `PartStock` (4,5), (5,5), (10,5).<br>2. Gọi `isLowStock()` trên từng object. | `quantity=4/5/10`; `minThreshold=5`. | `true / false / false`; tồn 5 bằng ngưỡng không cảnh báo, tồn 4 dưới ngưỡng cảnh báo. | Cao | PASS — cả 3 giá trị đúng: `true`, `false`, `false`. | **PASS** |
| **TC-04**<br>Unit — Giá trị biên | `PartTransactionServiceTest`; mock `PIN-IP15` tồn 10; phiếu `BH-000456/2026` `DANG_XU_LY`. | 1. Export quantity = 10 (đúng bằng tồn).<br>2. Kiểm tra tồn kho và cờ cảnh báo. | `quantity=10`; `stock=10`; `minThreshold=5`. | Tồn về đúng 0, không âm; `remainingStock=0`; `isLowStock=true`. | Cao | PASS — tồn 0, `remainingStock=0`, `isLowStock=true`. | **PASS** |
| **TC-05**<br>Unit — Giá trị biên | `PartTransactionServiceTest`; mock `PIN-IP15` tồn 10; phiếu hợp lệ `DANG_XU_LY`. | 1. Export quantity = 11 (vượt tồn 1 đơn vị).<br>2. Bắt exception, kiểm tra tồn và sổ cái. | `quantity=11`; `stock=10`. | `IllegalStateException` chứa “vượt quá số tồn hiện tại”; tồn vẫn 10; không ghi sổ cái. | Cao | PASS — exception đúng thông báo, tồn vẫn 10, `save()` không được gọi. | **PASS** |
| **TC-06**<br>Unit — Dữ liệu sai | `PartTransactionServiceTest`; phiếu `BH-000789/2026` trạng thái `DA_DONG`. | 1. Export quantity = 1.<br>2. Bắt exception, kiểm tra thông báo và verify không khóa tồn. | `ticketId=BH-000789/2026`; `status=DA_DONG`; `quantity=1`. | `IllegalStateException` chứa `DA_DONG`; không gọi `findByCenterIdAndPartCodeForUpdate`. | Cao | PASS — exception chứa `DA_DONG`, không khóa tồn. | **PASS** |
| **TC-07**<br>Unit — Bảo mật (S2/A01) | `PartTransactionServiceTest`; phiếu `BH-000002/2025` thuộc TT02; caller đang thao tác tại TT01. | 1. Export quantity = 1 với `centerId=1`.<br>2. Bắt exception, kiểm tra thông báo và verify không khóa tồn. | `ticketId=BH-000002/2025`; `centerId=1`; ticket center = 2; `quantity=1`. | `IllegalArgumentException` chứa “không thuộc quyền quản lý”; không gọi khóa tồn. | Cao | PASS — chặn đúng quyền sở hữu, không khóa tồn. | **PASS** |
| **TC-08**<br>Integration — Tính tái lập (C8) | `DataInitializerIntegrationTest`; `@SpringBootTest`; H2 in-memory; `app.seed.enabled=true`; `app.seed.path=seeds`. | 1. Context startup chạy seeder lần 1.<br>2. Đếm count 5 bảng.<br>3. Gọi `dataInitializer.run()` lần 2.<br>4. Đếm lại và so sánh. | `seeds/service_centers.csv`, `parts.csv`, `tickets.csv`, `part_stock.csv`, `part_transactions.csv`. | Không exception; count của 5 bảng không đổi sau lần chạy thứ hai. | Cao | PASS — 6 centers, 180 parts, 7800 tickets, 717 stocks, 4954 transactions; lần 2 không đổi. | **PASS** |
| **TC-09**<br>Integration — Luồng đúng | `PartTransactionIntegrationTest`; `@SpringBootTest`; H2; `@Transactional`; fixture `TT-INT-01`, `PIN-INT-01`, tồn 10, phiếu `BH-INT-0001/2026` `DANG_XU_LY`. | 1. Gọi `exportPart(centerId, qty=4)`.<br>2. Kiểm tra tồn và sổ cái.<br>3. Gọi `importPart(centerId, qty=20)`.<br>4. Kiểm tra tồn và sổ cái. | Export `qty=4`; import `qty=20`; tồn ban đầu 10; ngưỡng 5. | Tồn 10 → 6 → 26; sổ cái có 1 `EXPORT` + 1 `IMPORT`; response đúng loại. | Cao | PASS — tồn 6 rồi 26, sổ cái đủ 2 bản ghi. | **PASS** |
| **TC-10**<br>API — Luồng đúng | `TransactionControllerApiTest`; MockMvc standalone; service mock trả response 201; token TECHNICIAN; header `X-Center-Id: 1`. | 1. POST `/api/v1/transactions/export`.<br>2. Kiểm tra HTTP status và envelope `ApiResponseDTO`. | `ticketId=BH-000022/2025` (TT01, `DANG_XU_LY`); `partId=MH-SS-01`; `quantity=1`; `performedBy=ktv_trong`. | HTTP 201; `success=true`; `data.transactionType=EXPORT`; `data.partId=MH-SS-01`; `data.remainingStock=9`. | Cao | PASS — HTTP 201, envelope đúng, `remainingStock=9`. | **PASS** |
| **TC-11**<br>API — Dữ liệu sai | `TransactionControllerApiTest`; MockMvc standalone; token TECHNICIAN; header `X-Center-Id: 1`. | 1. POST export với quantity = 0.<br>2. Kiểm tra HTTP status và nội dung lỗi validation. | `ticketId=BH-000001/2025`; `partId=MH-SS-01`; `quantity=0` (vi phạm `@Min(1)`). | HTTP 422; `success=false`; `error.code=VALIDATION_ERROR`; `error.details.quantity` tồn tại. | Cao | PASS — HTTP 422, `VALIDATION_ERROR`, chi tiết lỗi trường `quantity` có mặt. | **PASS** |
| **TC-12**<br>API — Bảo mật (S2/A01) | `TransactionControllerApiTest`; MockMvc standalone; token TECHNICIAN; header `X-Center-Id: 1`. | 1. POST `/api/v1/transactions/import` bằng token TECHNICIAN.<br>2. Kiểm tra HTTP status và nội dung lỗi. | `partId=PIN-SS-01`; `quantity=50`; `performedBy=ktv_trong`. | HTTP 403; `success=false`; `error.code=FORBIDDEN`; thông báo chứa “403 Forbidden”. | Cao | PASS — HTTP 403, `FORBIDDEN`, thông báo đúng. | **PASS** |

---

## 3. Truy vết quy tắc nghiệp vụ → test case

| Quy tắc / yêu cầu | Nội dung kiểm chứng | Test case | Test method thật |
|---|---|---|---|
| **QT-06** | Chỉ xuất khi phiếu `DANG_XU_LY` hoặc `CHO_LINH_KIEN` | TC-01, TC-06 | `testExportPart_Success_NormalFlow`, `testExportPart_Error_InvalidTicketStatus` |
| **QT-09 / NFR-02** | Không cho tồn kho âm; khóa bi quan khi xuất | TC-04, TC-05, TC-09 | `testExportPart_Boundary_ExactStockQuantity`, `testExportPart_Error_OverStockAttempt`, `testExportThenImport_EndToEnd_PersistsStockAndLedger` |
| **US-02** | Nhập kho tăng tồn và ghi sổ cái append-only | TC-02, TC-09 | `testImportPart_Success_NormalFlow`, `testExportThenImport_EndToEnd_PersistsStockAndLedger` |
| **US-05** | Cảnh báo tồn thấp khi `quantity < minThreshold` | TC-03, TC-04 | `testPartStockLowStockAlert`, `testExportPart_Boundary_ExactStockQuantity` |
| **OWASP A01 / S2** | Kiểm soát quyền theo chủ sở hữu và vai trò | TC-07, TC-12 | `testExportPart_Error_CrossCenterOwnershipViolation`, `testImportPart_ForbiddenForTechnician` |
| **C8** | Chạy lại seeder/migration không lỗi và không nhân đôi dữ liệu | TC-08 | `testSeeder_RunTwice_DoesNotDuplicateData` |
| **API contract** | HTTP 201 / 422 / 403 và cấu trúc envelope | TC-10, TC-11, TC-12 | `testExportPart_ApiSuccess`, `testExportPart_ValidationError_ZeroQuantity`, `testImportPart_ForbiddenForTechnician` |

---

## 4. Minh chứng chạy thật

Lệnh đã chạy:

```powershell
.\mvnw.cmd clean test
```

Kết quả tổng từ Maven Surefire:

```text
[INFO] Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Chi tiết từng lớp test trong lần chạy ngày 10/11/2026:

| Lớp test | Số test | Thời gian | Kết quả |
|---|---:|---:|---|
| `DataInitializerIntegrationTest` | 1 | 17,640 s | PASS |
| `GlobalExceptionHandlerTest` | 1 | 0,047 s | PASS |
| `AuthControllerTest` | 1 | 0,037 s | PASS |
| `TransactionControllerApiTest` | 3 | 1,747 s | PASS |
| `InventoryApplicationTests` | 2 | 0,008 s | PASS |
| `AuthTokenServiceTest` | 7 | 0,050 s | PASS |
| `PartServiceTest` | 5 | 0,258 s | PASS |
| `PartTransactionIntegrationTest` | 1 | 2,450 s | PASS |
| `PartTransactionServiceTest` | 7 | 0,475 s | PASS |
| `StockServiceTest` | 4 | 0,035 s | PASS |
| **Tổng** | **32** | **~22,7 s test + build ≈ 32 s** | **PASS 100%** |

Ngoài 12 test case báo cáo, repo còn 20 test khác — trong đó có 3 test hồi quy cho Bug Log BT3 (`AuthTokenServiceTest#testCenterMismatchThrowsForbidden`, `AuthControllerTest#testGetCurrentUser_ReturnsIdentityFromToken`, `GlobalExceptionHandlerTest#testGenericException_DoesNotLeakInternalDetails`).

---

## 5. Cách chạy lại một test case bất kỳ

Giảng viên có thể chọn ngẫu nhiên một test case trong báo cáo và chạy lại bằng một trong các lệnh sau (Windows PowerShell):

```powershell
# Chạy lại TC-01 (unit)
.\mvnw.cmd "-Dtest=PartTransactionServiceTest#testExportPart_Success_NormalFlow" test

# Chạy lại TC-08 (integration C8)
.\mvnw.cmd "-Dtest=DataInitializerIntegrationTest" test

# Chạy lại TC-10 (API)
.\mvnw.cmd "-Dtest=TransactionControllerApiTest#testExportPart_ApiSuccess" test
```

Linux/macOS thay `.\mvnw.cmd` bằng `./mvnw`. Mọi test dùng H2 in-memory hoặc Mockito, không phụ thuộc MySQL hay dữ liệu trên máy cá nhân.

---

## 6. Ghi chú

* **Dữ liệu thử:** TC-08 nạp seeds thật trong `seeds/`; TC-10 dùng bộ giá trị đối chiếu từ seeds nhưng service được mock; các case còn lại dùng fixture tổng hợp trong test. Không test nào phụ thuộc dữ liệu máy cá nhân.
* **Cô lập:** unit/API dùng Mockito + `MockMvcBuilders.standaloneSetup()`, object mới trong `@BeforeEach`; integration dùng `@SpringBootTest` + H2, `ddl-auto=create-drop`, test service bọc `@Transactional` để rollback.
* **NFR-04:** sổ cái append-only được đảm bảo bằng thiết kế (không có API sửa/xóa); chưa có test tự động riêng trong phạm vi BT3 — bảo lưu cho CĐTN2.
* **Trung thực kết quả:** toàn bộ PASS/FAIL trong báo cáo lấy từ log Maven Surefire thật; không có test case nào được ghi PASS mà chưa chạy.

---

## 7. Kết luận

* **12/12 test case báo cáo PASS**, tuyển chọn từ **32/32 test thật** của repo; chạy lại bằng một lệnh duy nhất.
* **100% quy tắc nghiệp vụ cốt lõi** có test kiểm chứng trực tiếp: QT-06, QT-09/NFR-02, US-02, US-05, OWASP A01/S2 và C8.
* **Không còn bug Critical hoặc Major đang mở**; các bug đã sửa được ghi trong `docs/bug-log.md` kèm commit xác nhận.
