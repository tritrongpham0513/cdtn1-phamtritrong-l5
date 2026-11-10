# Nhật ký sửa lỗi (Bug Log) — Giai đoạn 3

> **Dự án:** Mekong Mobile Smart CRM — Phân hệ Luồng L5 (Kho linh kiện thay thế)
> **Học phần:** Chuyên đề Tốt nghiệp 1 · Học kỳ 1, Năm học 2026 – 2027 · Đại học Văn Lang
> **Sinh viên:** Phạm Trí Trọng — MSSV: 2374802010525 (Track SE)
> **Phạm vi:** 03 bug phát hiện trong quá trình kiểm thử BT3, đã sửa và có test hồi quy.
> **Kết quả xác nhận:** `.\mvnw.cmd clean test` → **32/32 test PASS**, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.

## Bảng tổng hợp

| Mã | Mức độ | Thành phần | Test case phát hiện / hồi quy | Commit |
|---|---|---|---|---|
| **BUG-BT3-01** | **CRITICAL** | Auth + Transaction/Stock Controller | `AuthTokenServiceTest#testCenterMismatchThrowsForbidden` | [`1b9f84a`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/1b9f84aa2fefe489aef617a04a091764f0a3198d) |
| **BUG-BT3-02** | **MAJOR** | AuthController (`/auth/me`) | `AuthControllerTest#testGetCurrentUser_ReturnsIdentityFromToken` | [`0d0dd1e`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/0d0dd1e0da49eed7b4315be759fdc6f13caa0b26) |
| **BUG-BT3-03** | **MAJOR** | GlobalExceptionHandler | `GlobalExceptionHandlerTest#testGenericException_DoesNotLeakInternalDetails` | [`dd354f8`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/dd354f88fd3b1a6c2eb0ca2600aa430ed014db1e) |

---

## BUG-BT3-01 — Giả mạo `X-Center-Id` để thao tác kho của trung tâm khác

- **Mã và mức độ:** `BUG-BT3-01` — **CRITICAL** — Component: Auth / Transaction / Stock Controller.
- **Phát hiện bởi test case:** `AuthTokenServiceTest#testCenterMismatchThrowsForbidden` (test hồi quy A01), liên quan trực tiếp TC-07 và TC-12 trong Test Report.
- **Triệu chứng:** Kỹ thuật viên thuộc TT01 có thể xuất linh kiện cho phiếu của TT04 bằng cách gửi header `X-Center-Id: 4`. Trước khi sửa, API trả **HTTP 201 Created** thay vì 403; cùng lỗi cho phép Quản lý kho nhập kho hoặc tạo/điều chỉnh/xóa tồn kho tại trung tâm không thuộc quyền.
- **Cách tái lập:** Đăng nhập `ktv_trong` (token chứa TT01), gửi `POST /api/v1/transactions/export` với header `X-Center-Id: 4`, body `{"ticketId":"BH-000051/2025","partId":"MH-SS-02","quantity":1,"performedBy":"ktv_trong"}`. `BH-000051/2025` là phiếu TT04 trạng thái `DANG_XU_LY`, `MH-SS-02` có tồn 22 tại TT04. Trước khi sửa: 201; sau khi sửa: **403 FORBIDDEN**.
- **Nguyên nhân:** `TransactionController` và `StockController` lấy thẳng `X-Center-Id` do client gửi làm phạm vi trung tâm, trong khi token chỉ chứa `userId/username/role` và không có `centerId`. Vì vậy service kiểm tra chủ sở hữu bằng cách so sánh `centerId` của header với `centerId` của phiếu — cả hai đều do client kiểm soát, nên không thể phát hiện giả mạo.
- **Cách sửa:** Bổ sung `centerId` vào token (`userId:username:role:centerId:issuedAt`); thêm `AuthTokenService.requireRoleAndCenter()` so khớp `X-Center-Id` với `centerId` trong token và ném `ForbiddenException` (403) khi lệch; áp dụng cho `POST /transactions/import`, `POST /transactions/export`, `POST/PUT/DELETE /stock`. Token cũ không có `centerId` bị từ chối ở các endpoint ghi kho.
- **Link commit và xác nhận:** Commit [`1b9f84a`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/1b9f84aa2fefe489aef617a04a091764f0a3198d) — `fix(auth): rang buoc X-Center-Id voi trung tam trong token (A01)`. Chạy lại `.\mvnw.cmd clean test`: **32/32 PASS**; test hồi quy `testCenterMismatchThrowsForbidden` PASS.

---

## BUG-BT3-02 — `/api/v1/auth/me` trả sai danh tính người dùng

- **Mã và mức độ:** `BUG-BT3-02` — **MAJOR** — Component: AuthController.
- **Phát hiện bởi test case:** `AuthControllerTest#testGetCurrentUser_ReturnsIdentityFromToken`.
- **Triệu chứng:** Với token của tài khoản `quanly_kho` (MANAGER), gọi `GET /api/v1/auth/me` vẫn trả về `username=ktv_trong`, `role=TECHNICIAN`, `centerId=1`. Giao diện có thể hiển thị sai người đăng nhập và cấp sai quyền trên màn hình.
- **Cách tái lập:** Đăng nhập `quanly_kho / 123456`, lấy token trả về; gọi `GET /api/v1/auth/me` với header `Authorization: Bearer <token>`. Trước khi sửa: response có `username=ktv_trong`, `role=TECHNICIAN`; sau khi sửa: `username=quanly_kho`, `role=MANAGER`, `centerId=1`.
- **Nguyên nhân:** Phương thức `getCurrentUser()` trong `AuthController` hard-code `MOCK_USERS.get("ktv_trong")` — một đoạn shortcut phục vụ demo đã không được thay bằng việc bóc tách token; thông tin principal từ `AuthTokenService` bị bỏ qua hoàn toàn.
- **Cách sửa:** Inject `AuthTokenService` vào `AuthController`; gọi `extractUser(authHeader)`, lấy `username` thực từ token, tra đúng `UserProfile` trong `MOCK_USERS` và trả về đúng `userId/username/fullName/role/centerId`. Token thiếu hoặc không hợp lệ trả **403 FORBIDDEN** thay vì âm thầm dùng tài khoản mặc định.
- **Link commit và xác nhận:** Commit [`0d0dd1e`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/0d0dd1e0da49eed7b4315be759fdc6f13caa0b26) — `fix(auth): /auth/me tra dung danh tinh tu token thay vi hard-code`. Chạy lại `.\mvnw.cmd clean test`: **32/32 PASS**; test hồi quy `testGetCurrentUser_ReturnsIdentityFromToken` PASS.

---

## BUG-BT3-03 — Phản hồi 500 làm lộ chi tiết nội bộ hệ thống

- **Mã và mức độ:** `BUG-BT3-03` — **MAJOR** — Component: GlobalExceptionHandler / Security (S4).
- **Phát hiện bởi test case:** `GlobalExceptionHandlerTest#testGenericException_DoesNotLeakInternalDetails`.
- **Triệu chứng:** Khi xảy ra exception không được xử lý riêng, body JSON trả về chứa nguyên `ex.getMessage()`, ví dụ `Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'` hoặc `Table 'part_stock' doesn't exist; jdbc:mysql://localhost:3306/inventory`. Thông tin này lộ tên bảng, kiểu dữ liệu, chuỗi kết nối và cấu trúc nội bộ.
- **Cách tái lập:** Gửi `GET /api/v1/stock` với header `X-Center-Id: abc` (không phải số) để tạo `MethodArgumentTypeMismatchException`, hoặc gây lỗi DB bất ngờ. Trước khi sửa: HTTP 500 với `error.message` chứa chi tiết exception; sau khi sửa: HTTP 500 với thông báo trung tính, không có tên bảng/chuỗi kết nối/stack trace.
- **Nguyên nhân:** Handler tổng quát `handleGenericException()` nối trực tiếp `ex.getMessage()` vào `ApiResponseDTO.fail(...)` trả cho client, đồng thời không ghi log đầy đủ ở server. Đây là lỗi rò rỉ thông tin (S4), không phải lỗi hiển thị.
- **Cách sửa:** Ghi log đầy đủ exception ở server bằng `log.error("Lỗi hệ thống chưa được xử lý", ex)`; trả client thông báo cố định `"Đã xảy ra lỗi hệ thống. Vui lòng liên hệ quản trị viên."` với mã `INTERNAL_SERVER_ERROR`, không kèm chi tiết nội bộ. Bổ sung test hồi quy kiểm tra body không chứa `part_stock`, `jdbc:mysql`, `localhost`.
- **Link commit và xác nhận:** Commit [`dd354f8`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/dd354f88fd3b1a6c2eb0ca2600aa430ed014db1e) — `fix(error): khong lo chi tiet noi bo trong phan hoi 500 (S4)`. Chạy lại `.\mvnw.cmd clean test`: **32/32 PASS**; test hồi quy `testGenericException_DoesNotLeakInternalDetails` PASS.

---

## Ghi chú lịch sử

* Ba bug trên là bug của giai đoạn BT3, được phát hiện khi rà soát bảo mật và thiết kế test hồi quy cho các tiêu chí A01/S2 và S4.
* Các bug BT2 đã sửa trước đó được lưu trong lịch sử commit: `3fae257` (seeder CSV), `52f8aaf` (Docker line endings), `551b5a1` (MySQL index/cổng).
* Mỗi mục bug đều có đủ 7 dòng theo yêu cầu mục 2.3: mã và mức độ · test case · triệu chứng · tái lập · nguyên nhân · cách sửa · link commit và xác nhận.
