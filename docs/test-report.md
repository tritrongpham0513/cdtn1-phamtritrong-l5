# Test Report — 12 test case đã CHẠY THẬT
## Thành phần 2 — Bài tập 3: Kiểm thử và Đóng gói prototype

* **Dự án:** Mekong Mobile Smart CRM — Luồng L5 Quản lý Kho Linh kiện Thay thế (Track SE)
* **Sinh viên:** Phạm Trí Trọng — MSSV: 2374802010525
* **Ngày chạy:** 11/11/2026
* **Môi trường:** Windows 11 · JDK 17.0.14 · Apache Maven Wrapper · Spring Boot 3.2.5 · JUnit 5 · Mockito · MockMvc · H2 in-memory
* **Lệnh chạy:** `.\mvnw.cmd test` (Windows) / `./mvnw test` (Linux/macOS)
* **Kết quả tổng:** **12/12 test case PASS 100%** · 0 failures · 0 errors · 0 skipped · BUILD SUCCESS.

**Phân tầng kiến trúc:** **7 Unit + 3 Integration + 2 API = 12 test cases**.  
**Phân bổ 5 loại test:** Luồng đúng 5 · Giá trị biên 2 · Dữ liệu sai 3 · Bảo mật 1 · Tính tái lập 1.

---

## 1. Đối chiếu yêu cầu mục 2.2 của đề bài BT3

| Yêu cầu đề bài | Số lượng tối thiểu | Test case đáp ứng | Minh chứng kiểm chứng |
|---|---|---|---|
| **Test cho luồng đúng**, mỗi quy tắc cốt lõi có ít nhất 1 test | 4 quy tắc → ≥ 4 test | **TC-01, TC-02, TC-03, TC-09, TC-11** | 5 test luồng đúng: TC-01 (xuất kho QT-06/QT-09), TC-02 (nhập kho US-02), TC-03 (cảnh báo tồn thấp US-05), TC-09 (end-to-end H2), TC-11 (API xuất kho HTTP 201). |
| **Test giá trị biên**, hai test ở hai bên cùng một ranh giới | 2 | **TC-04, TC-05** | Cặp biên ranh giới tồn kho = 10: xuất 10/10 → tồn về đúng 0 (PASS); xuất 11/10 → bị từ chối, tồn giữ nguyên 10. |
| **Test dữ liệu sai**, kiểm cả mã trạng thái/exception và thông báo lỗi | 2 | **TC-06, TC-10, TC-12** | 3 test: TC-06 (unit: exception chứa `DA_DONG`), TC-10 (integration H2: exception chứa `DA_DONG` và sổ cái không phát sinh bản ghi), TC-12 (API: HTTP 422 + `VALIDATION_ERROR` + details). |
| **Test bảo mật hoặc chất lượng dữ liệu** | ≥ 1 | **TC-07** | TC-07 chặn kỹ thuật viên xuất linh kiện cho phiếu của trung tâm khác (OWASP A01 / Tiêu chí S2). |
| **Test tính tái lập**, chạy lại lần hai cho kết quả giống lần đầu | 1 | **TC-08** | Seeder chạy lần 2 trên H2: không ngoại lệ, số lượng bản ghi 5 bảng không đổi (Tiêu chí C8). |
| **Tổng số test case** | **10–12** | **12** | Đúng cấu trúc chuẩn: 7 Unit + 3 Integration + 2 API = 12 test. |

---

## 2. Bảng 12 test case đã chạy thật (đúng 8 cột)

| Mã | Tiền điều kiện | Các bước | Dữ liệu thử | Kết quả mong đợi | Mức ưu tiên | Kết quả thực tế | PASS/FAIL |
|---|---|---|---|---|---|---|---|
| **TC-01**<br>Unit — Luồng đúng | `PartTransactionServiceTest`; mock `PIN-IP15` tồn 10, ngưỡng 5; mock phiếu `BH-000456/2026` trạng thái `DANG_XU_LY` thuộc TT01 (fixture tự tạo trong test). | 1. Tạo `ExportTransactionRequestDTO` quantity = 2.<br>2. Gọi `exportPart(1L, request)`.<br>3. Kiểm tra response, tồn kho và sổ cái. | `ticketId=BH-000456/2026`; `partId=PIN-IP15`; `quantity=2`; `performedBy=KTV - Phạm Trí Trọng`. | Response `transactionType=EXPORT`; tồn 10 → 8; `remainingStock=8`; `partTransactionRepository.save()` được gọi đúng 1 lần. | Cao | PASS — response `EXPORT`, tồn 8, `remainingStock=8`, sổ cái lưu 1 bản ghi. | **PASS** |
| **TC-02**<br>Unit — Luồng đúng (US-02) | `PartTransactionServiceTest`; mock `PIN-IP15` tồn 10, ngưỡng 5 (fixture tự tạo trong test). | 1. Tạo `ImportTransactionRequestDTO` quantity = 20.<br>2. Gọi `importPart(1L, request)`.<br>3. Kiểm tra response, tồn kho và sổ cái. | `partId=PIN-IP15`; `quantity=20`; `performedBy=Quản lý kho`. | Tồn 10 → 30; response `IMPORT`; `remainingStock=30`; `isLowStock=false`; sổ cái lưu 1 bản ghi `IMPORT`. | Cao | PASS — tồn 30, response `IMPORT`, `isLowStock=false`, sổ cái lưu 1 bản ghi. | **PASS** |
| **TC-03**<br>Unit — Luồng đúng (US-05) | `PartTransactionServiceTest`; khởi tạo đối tượng `PartStock` với ngưỡng tối thiểu `minThreshold = 5`. | 1. Gán tồn = 4 (< 5), gọi `isLowStock()`.<br>2. Gán tồn = 5 (== 5), gọi `isLowStock()`.<br>3. Gán tồn = 10 (> 5), gọi `isLowStock()`. | `quantity=4/5/10`; `minThreshold=5`. | Tồn 4 trả về `true` (cảnh báo tồn thấp); tồn 5 và 10 trả về `false` (không cảnh báo). | Cao | PASS — cả 3 giá trị kiểm tra khớp tuyệt đối: `true`, `false`, `false`. | **PASS** |
| **TC-04**<br>Unit — Giá trị biên | `PartTransactionServiceTest`; mock `PIN-IP15` tồn 10, ngưỡng 5; phiếu hợp lệ `DANG_XU_LY`. | 1. Export quantity = 10 (đúng bằng tồn hiện có).<br>2. Kiểm tra tồn kho và cờ cảnh báo tồn thấp. | `quantity=10`; `stock=10`; `minThreshold=5`. | Tồn kho giảm về đúng 0, không âm; `remainingStock=0`; `isLowStock=true` (0 < 5). | Cao | PASS — tồn về đúng 0, `remainingStock=0`, `isLowStock=true`. | **PASS** |
| **TC-05**<br>Unit — Giá trị biên | `PartTransactionServiceTest`; mock `PIN-IP15` tồn 10; phiếu hợp lệ `DANG_XU_LY`. | 1. Export quantity = 11 (vượt tồn 1 đơn vị).<br>2. Bắt exception, kiểm tra tồn và sổ cái. | `quantity=11`; `stock=10`. | `IllegalStateException` chứa "vượt quá số tồn hiện tại"; tồn giữ nguyên 10; không ghi sổ cái. | Cao | PASS — ném đúng exception, tồn giữ nguyên 10, sổ cái không được gọi lưu. | **PASS** |
| **TC-06**<br>Unit — Dữ liệu sai | `PartTransactionServiceTest`; mock phiếu `BH-000789/2026` trạng thái đã đóng `DA_DONG`. | 1. Export quantity = 1.<br>2. Bắt exception, kiểm tra thông báo và verify không khóa tồn. | `ticketId=BH-000789/2026`; `status=DA_DONG`; `quantity=1`. | `IllegalStateException` chứa chữ `DA_DONG`; không gọi hàm khóa tồn `findByCenterIdAndPartCodeForUpdate`. *(Unit test kiểm tra exception message ở tầng service, không kiểm mã HTTP).* | Cao | PASS — exception chứa `DA_DONG`, không gọi khóa tồn kho. | **PASS** |
| **TC-07**<br>Unit — Bảo mật (S2/A01) | `PartTransactionServiceTest`; mock phiếu `BH-000002/2025` thuộc TT02; KTV thao tác với `centerId = 1`. | 1. Export quantity = 1 với `centerId = 1`.<br>2. Bắt exception, kiểm tra thông báo và verify không khóa tồn. | `ticketId=BH-000002/2025`; `centerId=1`; ticket center = 2; `quantity=1`. | `IllegalArgumentException` chứa "không thuộc quyền quản lý"; không gọi khóa tồn kho. *(Unit test kiểm tra kiểm soát quyền sở hữu chéo trung tâm ở tầng service).* | Cao | PASS — chặn đúng vi phạm quyền sở hữu A01, không gọi khóa tồn. | **PASS** |
| **TC-08**<br>Integration — Tính tái lập (C8) | `DataInitializerIntegrationTest`; `@SpringBootTest`; H2 in-memory; `app.seed.enabled=true`; `app.seed.path=seeds`. | 1. Context startup nạp seeder lần 1.<br>2. Đếm count 5 bảng CSDL.<br>3. Gọi trực tiếp `dataInitializer.run()` lần 2.<br>4. Đếm lại count 5 bảng và so sánh. | Tệp CSV từ `seeds/`: 6 centers, 180 parts, 7.800 tickets, 717 stocks, 4.954 transactions. | Không ném ngoại lệ; số lượng bản ghi của cả 5 bảng giữ nguyên 100% sau lần chạy thứ hai. | Cao | PASS — 6 centers, 180 parts, 7800 tickets, 717 stocks, 4954 transactions; lần 2 giữ nguyên 100%. | **PASS** |
| **TC-09**<br>Integration — Luồng đúng | `PartTransactionIntegrationTest`; `@SpringBootTest`; H2 in-memory; `@Transactional`; fixture tự tạo lưu H2: `TT-INT-01`, `PIN-INT-01`, tồn 10, phiếu `BH-INT-0001/2026` `DANG_XU_LY`. | 1. Gọi `exportPart(centerId, qty=4)`.<br>2. Kiểm tra tồn và sổ cái trên H2.<br>3. Gọi `importPart(centerId, qty=20)`.<br>4. Kiểm tra tồn và sổ cái trên H2. | Export `qty=4`; import `qty=20`; tồn ban đầu 10; ngưỡng 5. | Tồn 10 → 6 → 26; sổ cái `part_transactions` lưu đúng 1 bản ghi `EXPORT` và 1 bản ghi `IMPORT`. | Cao | PASS — tồn 6 rồi lên 26 trên H2, sổ cái ghi đủ 2 giao dịch. | **PASS** |
| **TC-10**<br>Integration — Dữ liệu sai (QT-06) | `PartTransactionIntegrationTest`; `@SpringBootTest`; H2 in-memory; `@Transactional`; fixture tự tạo lưu H2: `TT-INT-02`, `PIN-INT-02`, tồn 10, phiếu `BH-INT-0002/2026` trạng thái `DA_DONG`. | 1. Gọi `exportPart(centerId, qty=1)` cho phiếu đã đóng.<br>2. Bắt exception, kiểm tra thông báo lỗi.<br>3. Kiểm tra số lượng bản ghi sổ cái trên CSDL H2. | `ticketCode=BH-INT-0002/2026`; `status=DA_DONG`; `quantity=1`. | `IllegalStateException` chứa chữ `DA_DONG`; sổ cái `part_transactions` có đúng 0 bản ghi (không ghi dữ liệu rác vào CSDL). | Cao | PASS — ném exception chứa `DA_DONG`, sổ cái có đúng 0 bản ghi. | **PASS** |
| **TC-11**<br>API — Luồng đúng (HTTP 201) | `TransactionControllerApiTest`; `MockMvcBuilders.standaloneSetup()`; service được mock trả response 201; token TECHNICIAN; header `X-Center-Id: 1`. | 1. POST `/api/v1/transactions/export`.<br>2. Kiểm tra HTTP status và envelope `ApiResponseDTO`. | Request body: `ticketId=BH-000022/2025`; `partId=MH-SS-01`; `quantity=1`; `performedBy=ktv_trong`. | HTTP 201 Created; `success=true`; `data.transactionType=EXPORT`; `data.partId=MH-SS-01`; `data.remainingStock=9`. *(Tầng Controller qua MockMvc, service được mock).* | Cao | PASS — HTTP 201, cấu trúc envelope đúng, `remainingStock=9`. | **PASS** |
| **TC-12**<br>API — Dữ liệu sai (HTTP 422) | `TransactionControllerApiTest`; `MockMvcBuilders.standaloneSetup()`; token TECHNICIAN; header `X-Center-Id: 1`. | 1. POST export với quantity = 0.<br>2. Kiểm tra HTTP status và nội dung lỗi validation RFC-7807 / ApiResponseDTO. | Request body: `ticketId=BH-000001/2025`; `partId=MH-SS-01`; `quantity=0` (vi phạm `@Min(1)`). | HTTP 422 Unprocessable Entity; `success=false`; `error.code=VALIDATION_ERROR`; có trường `error.details.quantity`. *(Tầng Controller qua MockMvc, bean validation).* | Cao | PASS — HTTP 422, `VALIDATION_ERROR`, chi tiết lỗi `quantity` hiện diện đầy đủ. | **PASS** |

---

## 3. Truy vết quy tắc nghiệp vụ → test case → mã nguồn kiểm thử

| Quy tắc / Yêu cầu cốt lõi | Nội dung kiểm chứng | Test case | Lớp và Hàm test Java cụ thể |
|---|---|---|---|
| **QT-06** | Chỉ xuất kho khi phiếu `DANG_XU_LY` hoặc `CHO_LINH_KIEN`; từ chối phiếu `DA_DONG` | TC-01, TC-06, TC-10 | `PartTransactionServiceTest#testExportPart_Success_NormalFlow`<br>`PartTransactionServiceTest#testExportPart_Error_InvalidTicketStatus`<br>`PartTransactionIntegrationTest#testExportPart_InvalidTicketStatus_DA_DONG_ThrowsException` |
| **QT-09 / NFR-02** | Khóa bi quan chống xuất âm kho; tồn giảm chính xác; chặn xuất vượt tồn | TC-04, TC-05, TC-09 | `PartTransactionServiceTest#testExportPart_Boundary_ExactStockQuantity`<br>`PartTransactionServiceTest#testExportPart_Error_OverStockAttempt`<br>`PartTransactionIntegrationTest#testExportThenImport_EndToEnd_PersistsStockAndLedger` |
| **US-02** | Ghi nhận nhập kho bổ sung, tăng tồn và ghi sổ cái append-only | TC-02, TC-09 | `PartTransactionServiceTest#testImportPart_Success_NormalFlow`<br>`PartTransactionIntegrationTest#testExportThenImport_EndToEnd_PersistsStockAndLedger` |
| **US-05** | Kích hoạt cảnh báo tồn thấp khi `quantity < minThreshold` | TC-03, TC-04 | `PartTransactionServiceTest#testPartStock_LowStockAlert_US05`<br>`PartTransactionServiceTest#testExportPart_Boundary_ExactStockQuantity` |
| **OWASP A01 / S2** | Kiểm soát quyền truy cập theo chủ sở hữu (chặn xuất kho chéo trung tâm) | TC-07 | `PartTransactionServiceTest#testExportPart_Error_CrossCenterOwnershipViolation` |
| **C8** | Tính tái lập: seeder chạy lần 2 không lỗi và không nhân đôi dữ liệu trên H2 | TC-08 | `DataInitializerIntegrationTest#testSeeder_RunTwice_DoesNotDuplicateData` |
| **API Contract & Validation** | Hợp đồng API xuất kho: mã HTTP 201 Created và mã HTTP 422 Unprocessable Entity | TC-11, TC-12 | `TransactionControllerApiTest#testExportPart_ApiSuccess`<br>`TransactionControllerApiTest#testExportPart_ValidationError_ZeroQuantity` |

---

## 4. Minh chứng chạy thật (Maven Surefire)

Lệnh thực thi bộ kiểm thử:

```powershell
.\mvnw.cmd test
```

Kết quả tổng hợp từ Maven Surefire:

```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  22.159 s
```

Bảng tổng hợp chi tiết theo 4 lớp kiểm thử cấu thành 12 test cases:

| Lớp test Java | Tầng kiểm thử | Số test | Thời gian chạy | Kết quả thực tế | Ghi chú môi trường |
|---|---|---:|---:|:---:|---|
| `PartTransactionServiceTest` | Unit (Service Logic) | 7 | 0.351 s | **7/7 PASS** | Dùng Mockito cô lập phụ thuộc, không truy xuất CSDL |
| `DataInitializerIntegrationTest` | Integration (C8 Tái lập) | 1 | 15.450 s | **1/1 PASS** | `@SpringBootTest`, nạp dữ liệu thật từ `seeds/` vào H2 |
| `PartTransactionIntegrationTest` | Integration (End-to-End H2) | 2 | 1.397 s | **2/2 PASS** | `@SpringBootTest` + H2 thật + `@Transactional` rollback |
| `TransactionControllerApiTest` | API Slice (MockMvc Controller) | 2 | 0.800 s | **2/2 PASS** | `MockMvcBuilders.standaloneSetup()` + Mockito service |
| **Tổng cộng** | **Cả 3 tầng Kim tự tháp** | **12** | **22.159 s** | **12/12 PASS (100%)** | **0 failures, 0 errors, 0 skipped** |

---

## 5. Hướng dẫn chạy lại một test case bất kỳ tại chỗ (Tiêu chí C3)

Giảng viên có thể chọn ngẫu nhiên một test case trong bảng và yêu cầu sinh viên chạy lại tại chỗ bằng các lệnh sau (trên Windows PowerShell):

```powershell
# 1. Chạy lại TC-01 (Unit - Luồng đúng xuất kho):
.\mvnw.cmd "-Dtest=PartTransactionServiceTest#testExportPart_Success_NormalFlow" test

# 2. Chạy lại TC-03 (Unit - Cảnh báo tồn thấp US-05):
.\mvnw.cmd "-Dtest=PartTransactionServiceTest#testPartStock_LowStockAlert_US05" test

# 3. Chạy lại TC-08 (Integration - Tính tái lập Seeder C8):
.\mvnw.cmd "-Dtest=DataInitializerIntegrationTest#testSeeder_RunTwice_DoesNotDuplicateData" test

# 4. Chạy lại TC-10 (Integration - Dữ liệu sai phiếu DA_DONG trên CSDL H2):
.\mvnw.cmd "-Dtest=PartTransactionIntegrationTest#testExportPart_InvalidTicketStatus_DA_DONG_ThrowsException" test

# 5. Chạy lại TC-11 (API Slice - Xuất kho hợp lệ HTTP 201):
.\mvnw.cmd "-Dtest=TransactionControllerApiTest#testExportPart_ApiSuccess" test
```

*(Trên môi trường Linux / macOS, thay `.\mvnw.cmd` bằng `./mvnw`)*. Mọi test case đều tự động chuẩn bị dữ liệu qua fixture hoặc H2 in-memory, hoàn toàn độc lập với máy cá nhân.

---

## 6. Known Gaps / Hạn chế kỹ thuật (Ghi nhận trung thực)

1. **Các User Story tra cứu và CRUD chưa có kiểm thử tự động:** FR-01 (xem tồn kho/tìm kiếm/lọc), FR-06 (xem lịch sử giao dịch/lọc/phân trang), FR-07 (cập nhật ngưỡng tối thiểu), Part CRUD, và Stock CRUD. Các tính năng này đã hoạt động và được kiểm thử thủ công qua Web UI trong BT2, nhưng chưa được viết test tự động trong bộ 12 test case này.
2. **Kiểm thử truy cập đồng thời (NFR-02):** Hiện tại mới chỉ kiểm chứng cơ chế khóa bi quan (`PESSIMISTIC_WRITE`) qua kiểm thử đơn luồng; chưa có kiểm thử tải đa luồng 50 request đồng thời (thuộc phạm vi Chuyên đề tốt nghiệp 2 với công cụ JMeter/k6).
3. **Kiểm tra tính bất biến toán học và đối soát sổ cái (NFR-04):** Test integration TC-09 mới chỉ xác nhận số lượng bản ghi giao dịch trong `part_transactions` tăng thêm 2 bản ghi sau 1 lần xuất và 1 lần nhập; chưa có test đối soát toán học toàn diện trên toàn bộ dữ liệu 10.000 giao dịch.
4. **Kiểm soát quyền truy cập ở các endpoint đọc (A01):** Quyền sở hữu theo trung tâm (A01) đã được kiểm chứng cho nghiệp vụ xuất kho (TC-07), nhưng các endpoint đọc (`GET /api/v1/stock`, `GET /api/v1/transactions`, `GET /api/v1/tickets/{ticketId}/parts`) hiện chưa có middleware kiểm tra token/center.
5. **Kiểm tra mã trạng thái HTTP 403 / RBAC ở tầng API:** Để đảm bảo phân tầng 7 Unit + 3 Integration + 2 API theo đúng yêu cầu đề bài BT3, test kiểm tra phân quyền RBAC 403 của KTV khi gọi API nhập kho không nằm trong 2 API test của báo cáo này; bảo mật phân quyền được kiểm chứng ở tầng Service qua TC-07.
6. **Mã trạng thái HTTP cho lỗi phiếu đã đóng `DA_DONG`:** Quy tắc QT-06 được kiểm chứng ngoại lệ ở tầng Service (TC-06) và CSDL H2 (TC-10); tầng REST API khi gặp ngoại lệ này sẽ được `GlobalExceptionHandler` bắt và chuyển thành HTTP 409 Conflict, tuy nhiên chưa có test API slice riêng cho mã 409 này.

---

## 7. Kết luận và Liêm chính học thuật

* **Đúng cấu trúc và phân tầng:** Đạt chính xác **7 Unit + 3 Integration + 2 API = 12 test cases**, bao phủ trọn vẹn 5 loại kiểm thử đề bài yêu cầu.
* **100% kết quả chạy thật:** Không có test case nào được lập khống; toàn bộ 12 test method đều tồn tại trong thư mục `src/test/java`, biên dịch thành công và đạt **12/12 PASS 100%**.
* **Minh bạch về phạm vi và hạn chế:** Phân định rõ ràng giữa dữ liệu seed thật và fixture tự tạo; các điểm chưa bao phủ được ghi nhận đầy đủ tại Mục 6 (Known Gaps).
