# Nhật ký sửa lỗi (Bug Log) — Giai đoạn 3

> **Dự án:** Mekong Mobile Smart CRM — Phân hệ Luồng L5 (Quản lý kho linh kiện thay thế)  
> **Học phần:** Chuyên đề Tốt nghiệp 1 · Học kỳ 1, Năm học 2026 – 2027 · Đại học Văn Lang  
> **Sinh viên thực hiện:** Phạm Trí Trọng — MSSV: 2374802010525 (Track SE)  
> **Phạm vi tài liệu:** Ghi nhận và theo dõi 03 lỗi (01 CRITICAL + 02 MAJOR) phát hiện trong quá trình kiểm thử bảo mật của giai đoạn BT3; cả 03 lỗi đã được khắc phục triệt để với commit độc lập.  
> **Kết quả xác nhận kiểm thử tự động:** `.\mvnw.cmd test` → **12/12 test PASS (100%)**, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.  
> **Môi trường thực thi lệnh kiểm thử:** Các lệnh cURL mẫu bên dưới sử dụng cổng backend cục bộ `http://localhost:8080` (khi chạy `.\mvnw.cmd spring-boot:run`). Trường hợp chạy ứng dụng qua Docker Compose, cổng tương ứng là `http://localhost:8888`.  
> **Lưu ý về shell thực thi lệnh cURL:** Các lệnh cURL mẫu bên dưới chạy trực tiếp trong Git Bash, WSL, hoặc dùng `curl.exe` trên PowerShell 7 với chuỗi JSON đặt trong dấu nháy đơn, ví dụ: `-d '{"username":"ktv_trong","password":"123456"}'` (nhằm tránh xung đột với alias `Invoke-WebRequest` trên Windows PowerShell 5 cũ).

---

## 1. Bảng tổng hợp lỗi giai đoạn BT3

| Mã lỗi | Mức độ nghiêm trọng | Thành phần ảnh hưởng | Phát hiện bởi | Commit khắc phục |
|---|---|---|---|---|
| **BUG-BT3-01** | **CRITICAL** | Auth + Transaction / Stock Controller | `SEC-01` (Kiểm thử bảo mật thủ công) | [`1b9f84a`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/1b9f84aa2fefe489aef617a04a091764f0a3198d) |
| **BUG-BT3-02** | **MAJOR** | AuthController (`/api/v1/auth/me`) | `SEC-02` (Kiểm thử bảo mật thủ công) | [`0d0dd1e`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/0d0dd1e0da49eed7b4315be759fdc6f13caa0b26) |
| **BUG-BT3-03** | **MAJOR** | GlobalExceptionHandler | `SEC-03` (Kiểm thử bảo mật thủ công) | [`dd354f8`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/dd354f88fd3b1a6c2eb0ca2600aa430ed014db1e) |

---

## 2. Bảng các ca kiểm thử bảo mật thủ công (Security Test Cases)

> *Ghi chú:* Để duy trì phân tầng kiểm thử tự động đúng chuẩn 7 Unit + 3 Integration + 2 API = 12 test cases theo yêu cầu đề bài BT3 (như đã ghi nhận tại Mục 6 — Known Gaps #5 của Test Report), 03 ca kiểm thử bảo mật dưới đây được thực hiện theo phương thức kiểm thử bảo mật thủ công bằng lệnh cURL, độc lập với bộ 12 ca kiểm thử tự động của dự án.  
>  
> **Cách tái hiện trạng thái "trước khi sửa":** Dùng lệnh `git worktree add --detach ../before-01 1b9f84a^` (tương tự `0d0dd1e^` cho SEC-02 và `dd354f8^` cho SEC-03), khởi chạy ứng dụng từ worktree đó rồi gửi lệnh cURL. Trên nhánh `HEAD` hiện tại, kết quả nhận được luôn là trạng thái "sau khi sửa". Nhờ vậy, toàn bộ bằng chứng trước/sau sửa lỗi đều có tính tái lập hoàn toàn (tuân thủ tiêu chuẩn C4).

| ID | Mục tiêu kiểm thử | Lệnh tái lập (cURL) | Kết quả trước khi sửa | Kết quả sau khi sửa | Minh chứng xác nhận |
|---|---|---|---|---|---|
| **SEC-01** | Kiểm tra kiểm soát truy cập theo trung tâm (A01 / S2): Chặn kỹ thuật viên giả mạo `X-Center-Id` để xuất kho linh kiện trung tâm khác | 1. Đăng nhập KTV TT01:<br>`curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"ktv_trong","password":"123456"}'`<br>*(Lấy token trả về)*<br>2. Xuất kho cho phiếu TT04:<br>`curl -s -X POST http://localhost:8080/api/v1/transactions/export -H "Authorization: Bearer <token>" -H "X-Center-Id: 4" -H "Content-Type: application/json" -d '{"ticketId":"BH-000051/2025","partId":"MH-SS-02","quantity":1,"performedBy":"ktv_trong"}'` | **HTTP 201 Created**:<br>Hệ thống cho phép KTV TT01 xuất kho linh kiện của TT04 trái thẩm quyền. | **HTTP 403 FORBIDDEN**:<br>`"Từ chối truy cập (403 Forbidden): X-Center-Id=4 không khớp với trung tâm 1 của tài khoản 'ktv_trong'."` | Commit [`1b9f84a`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/1b9f84aa2fefe489aef617a04a091764f0a3198d);<br>Bộ 12 automated test PASS 100%. |
| **SEC-02** | Kiểm tra xác thực danh tính phiên làm việc: Endpoint `/api/v1/auth/me` phải trả về đúng danh tính từ token đăng nhập thay vì gán cứng | 1. Đăng nhập Quản lý kho TT01:<br>`curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"quanly_kho","password":"123456"}'`<br>*(Lấy token trả về)*<br>2. Kiểm tra thông tin phiên:<br>`curl -s -X GET http://localhost:8080/api/v1/auth/me -H "Authorization: Bearer <token>"` | **Trả sai danh tính người dùng**:<br>Body JSON trả về `username=ktv_trong`, `role=TECHNICIAN`, `fullName="Phạm Trí Trọng"`. | **Trả chính xác danh tính người dùng**:<br>Body JSON trả về `username=quanly_kho`, `role=MANAGER`, `centerId=1`. Nếu thiếu token trả HTTP 403. | Commit [`0d0dd1e`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/0d0dd1e0da49eed7b4315be759fdc6f13caa0b26);<br>Bộ 12 automated test PASS 100%. |
| **SEC-03** | Kiểm tra bảo mật thông tin nội bộ (S4): Ngăn ngừa rò rỉ chi tiết cấu trúc hệ thống, tên bảng CSDL hoặc chuỗi kết nối trong mã lỗi 500 | Gửi request có header sai định dạng kiểu dữ liệu (chuỗi thay vì số Long):<br>`curl -s -X GET http://localhost:8080/api/v1/stock -H "X-Center-Id: abc"` | **HTTP 500 làm lộ thông tin nội bộ**:<br>Body JSON trả về nguyên văn thông điệp exception kỹ thuật (chứa kiểu dữ liệu, có nguy cơ lộ tên bảng `part_stock`, chuỗi kết nối `jdbc:mysql` hoặc `localhost`). | **HTTP 500 trung tính, an toàn**:<br>Body JSON chuẩn hóa:<br>`{"success":false,"error":{"code":"INTERNAL_SERVER_ERROR","message":"Đã xảy ra lỗi hệ thống. Vui lòng liên hệ quản trị viên."},"timestamp":"2026-11-11T21:22:11.123"}`.<br>*(Không chứa `data`, `meta` hay `details` do `JsonInclude.NON_NULL`)*. Log chi tiết lưu tại server. | Commit [`dd354f8`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/dd354f88fd3b1a6c2eb0ca2600aa430ed014db1e);<br>Bộ 12 automated test PASS 100%. |

---

## 3. Chi tiết các lỗi đã khắc phục

### BUG-BT3-01 — Giả mạo `X-Center-Id` để thao tác kho của trung tâm khác

- **Mã và mức độ:** `BUG-BT3-01` — **CRITICAL** — Thành phần: Auth / Transaction Controller / Stock Controller.
- **Phát hiện bởi test case:** `SEC-01` (Kiểm thử bảo mật thủ công, ngoài 12 automated test cases; kiểm tra kiểm soát quyền sở hữu theo trung tâm OWASP A01 / S2).
- **Triệu chứng:** Kỹ thuật viên thuộc Trung tâm 1 (`ktv_trong`) có thể xuất linh kiện cho phiếu sửa chữa của Trung tâm 4 bằng cách đổi header thành `X-Center-Id: 4`. Hệ thống trả về **HTTP 201 Created** thay vì 403 Forbidden. Lỗ hổng tương tự cho phép Quản lý kho nhập kho, tạo mới, điều chỉnh ngưỡng tồn hoặc xóa bản ghi tồn kho tại bất kỳ trung tâm nào khác không thuộc thẩm quyền quản lý.
- **Cách tái lập:** Đăng nhập `ktv_trong / 123456` lấy token (tài khoản thuộc TT01); gửi request `POST /api/v1/transactions/export` với header `Authorization: Bearer <token>`, header `X-Center-Id: 4`, body `{"ticketId":"BH-000051/2025","partId":"MH-SS-02","quantity":1,"performedBy":"ktv_trong"}` (`BH-000051/2025` là phiếu bảo hành thuộc TT04 trạng thái `DANG_XU_LY`, linh kiện `MH-SS-02` có tồn 22 cái tại TT04). Trước khi sửa: API trả về **HTTP 201 Created** và trừ kho TT04 thành công. Sau khi sửa: API trả về **HTTP 403 FORBIDDEN** với thông báo `"Từ chối truy cập (403 Forbidden): X-Center-Id=4 không khớp với trung tâm 1 của tài khoản 'ktv_trong'."`.
- **Nguyên nhân:** `TransactionController` và `StockController` lấy trực tiếp giá trị `X-Center-Id` do client gửi làm phạm vi trung tâm thao tác. Trong khi đó, định dạng token cũ chỉ chứa `userId:username:role:issuedAt` mà không đóng gói `centerId`. Tầng Service khi kiểm tra quyền sở hữu chỉ so sánh `centerId` của header với `centerId` của phiếu bảo hành — cả hai giá trị đều do client kiểm soát trong request — nên phép đối chiếu luôn khớp, không thể phát hiện hành vi giả mạo phạm vi trung tâm (Lỗ hổng Broken Object Level Authorization - BOLA / OWASP A01).
- **Cách sửa:** Bổ sung `centerId` vào token đăng nhập (`userId:username:role:centerId:issuedAt`); thêm phương thức `AuthTokenService.requireRoleAndCenter()` để trích xuất `centerId` từ token và bắt buộc so khớp với `X-Center-Id` yêu cầu, ném `ForbiddenException` (HTTP 403) nếu không trùng khớp; đồng thời áp dụng chốt chặn này cho toàn bộ các endpoint ghi kho: `POST /api/v1/transactions/import`, `POST /api/v1/transactions/export`, `POST /api/v1/stock`, `PUT /api/v1/stock/threshold`, `DELETE /api/v1/stock/{partCode}`. Token cũ không có `centerId` bị từ chối truy cập ở các thao tác ghi.
- **Link commit và xác nhận:** Commit [`1b9f84a`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/1b9f84aa2fefe489aef617a04a091764f0a3198d) — `fix(auth): rang buoc X-Center-Id voi trung tam trong token (A01)`. Tái hiện lại với lệnh `SEC-01` nhận đúng HTTP 403 FORBIDDEN; chạy lệnh `.\mvnw.cmd test` đạt **12/12 test PASS (100%)**.

---

### BUG-BT3-02 — `/api/v1/auth/me` trả sai danh tính người dùng

- **Mã và mức độ:** `BUG-BT3-02` — **MAJOR** — Thành phần: AuthController (`/api/v1/auth/me`).
- **Phát hiện bởi test case:** `SEC-02` (Kiểm thử bảo mật thủ công, ngoài 12 automated test cases; kiểm tra xác thực danh tính phiên làm việc).
- **Triệu chứng:** Khi người dùng đăng nhập bằng tài khoản Quản lý kho `quanly_kho` (vai trò `MANAGER`, `centerId=1`), sau đó gọi `GET /api/v1/auth/me` với Bearer token hợp lệ, API vẫn trả về thông tin `username=ktv_trong`, `role=TECHNICIAN`, `fullName="Phạm Trí Trọng"`. Giao diện Web UI client dựa vào kết quả này sẽ hiển thị sai người đăng nhập và hiển thị sai menu quyền hạn của người dùng.
- **Cách tái lập:** Đăng nhập `quanly_kho / 123456`, trích xuất chuỗi token từ response; gửi request `GET /api/v1/auth/me` với header `Authorization: Bearer <token>`. Trước khi sửa: response trả về thông tin `username=ktv_trong`, `role=TECHNICIAN`. Sau khi sửa: response trả về chính xác danh tính người đăng nhập gồm `username=quanly_kho`, `role=MANAGER`, `centerId=1`. Nếu không truyền token hoặc token không hợp lệ, hệ thống trả về **HTTP 403 FORBIDDEN**.
- **Nguyên nhân:** Phương thức `getCurrentUser()` trong `AuthController` ban đầu được gán cứng (hard-code) `MOCK_USERS.get("ktv_trong")` như một giải pháp tạm thời trong giai đoạn dựng prototype nhanh và chưa được thay thế bằng logic trích xuất danh tính thực tế. Lớp `AuthTokenService` không được inject vào `AuthController`, khiến thông tin người dùng trong header `Authorization` bị bỏ qua hoàn toàn.
- **Cách sửa:** Inject `AuthTokenService` vào `AuthController` thông qua `@RequiredArgsConstructor`; gọi `authTokenService.extractUser(authHeader)` để trích xuất `principal` từ token; tra cứu đúng hồ sơ `UserProfile` tương ứng trong `MOCK_USERS` theo username thực tế; trả về đối tượng `LoginResponseDTO` chứa đầy đủ `userId`, `username`, `fullName`, `role`, `centerId`, `centerName`. Trường hợp token bị thiếu hoặc không hợp lệ sẽ ném `ForbiddenException` (**HTTP 403 FORBIDDEN**) thay vì âm thầm trả về tài khoản mặc định.
- **Link commit và xác nhận:** Commit [`0d0dd1e`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/0d0dd1e0da49eed7b4315be759fdc6f13caa0b26) — `fix(auth): /auth/me tra dung danh tinh tu token thay vi hard-code`. Tái hiện lại với lệnh `SEC-02` nhận đúng thông tin `quanly_kho` (MANAGER); chạy lệnh `.\mvnw.cmd test` đạt **12/12 test PASS (100%)**.

---

### BUG-BT3-03 — Phản hồi lỗi HTTP 500 làm lộ chi tiết nội bộ hệ thống

- **Mã và mức độ:** `BUG-BT3-03` — **MAJOR** — Thành phần: GlobalExceptionHandler / Security (S4 — Ngăn ngừa rò rỉ thông tin hệ thống).
- **Phát hiện bởi test case:** `SEC-03` (Kiểm thử bảo mật thủ công, ngoài 12 automated test cases; kiểm tra chuẩn hóa phản hồi lỗi và an toàn thông tin hệ thống).
- **Triệu chứng:** Khi phát sinh ngoại lệ không mong muốn chưa được xử lý riêng (ví dụ: client truyền sai kiểu dữ liệu tham số trên header gây ra `MethodArgumentTypeMismatchException`, hoặc lỗi truy vấn CSDL bất ngờ), phản hồi HTTP 500 gửi về client chứa nguyên văn chuỗi thông báo lỗi kỹ thuật của framework hoặc cơ sở dữ liệu (`ex.getMessage()`). Nội dung này có thể để lộ kiểu dữ liệu Java nội bộ, tên bảng CSDL (`part_stock`), hoặc chuỗi kết nối (`jdbc:mysql://localhost:3306/inventory`), vi phạm tiêu chuẩn bảo mật chống rò rỉ thông tin nội bộ (S4).
- **Cách tái lập:** Gửi request tra cứu tồn kho `GET /api/v1/stock` với header cố tình truyền sai định dạng kiểu dữ liệu: `X-Center-Id: abc` (chuỗi ký tự thay vì số nguyên). Trước khi sửa: API trả về **HTTP 500** với trường `error.message` chứa chi tiết ngoại lệ nội bộ của thư viện Spring. Sau khi sửa: API trả về **HTTP 500** với body JSON chuẩn hóa: `{"success":false,"error":{"code":"INTERNAL_SERVER_ERROR","message":"Đã xảy ra lỗi hệ thống. Vui lòng liên hệ quản trị viên."},"timestamp":"2026-11-11T21:22:11.123"}` (các trường `data`, `meta`, và `error.details` bị lược bỏ do cấu hình `NON_NULL`), tuyệt đối không chứa tên bảng CSDL, chuỗi kết nối hay chi tiết stack trace.
- **Nguyên nhân:** Phương thức xử lý ngoại lệ chung `handleGenericException(Exception ex)` trong `GlobalExceptionHandler` đã nối trực tiếp giá trị `ex.getMessage()` vào trường message của `ApiResponseDTO.fail(...)` trả về cho client. Đồng thời, ngoại lệ chưa được ghi log đầy đủ ở phía máy chủ. Đây là lỗi thiết kế rò rỉ thông tin an ninh hệ thống (Information Disclosure - S4) theo các chuẩn bảo mật ứng dụng web.
- **Cách sửa:** Ghi nhận toàn bộ thông tin ngoại lệ kèm stack trace tại log máy chủ bằng lệnh `log.error("Lỗi hệ thống chưa được xử lý", ex);` để phục vụ công tác điều tra sự cố của quản trị viên; chuẩn hóa thông điệp trả về client thành chuỗi trung tính cố định `"Đã xảy ra lỗi hệ thống. Vui lòng liên hệ quản trị viên."` với mã lỗi `INTERNAL_SERVER_ERROR`; đảm bảo trường `error.details` là `null` (bị lược bỏ khỏi JSON do cấu hình `@JsonInclude(JsonInclude.Include.NON_NULL)`), loại bỏ hoàn toàn việc để lộ chi tiết nội bộ của hạ tầng và CSDL ra ngoài môi trường mạng.
- **Link commit và xác nhận:** Commit [`dd354f8`](https://github.com/tritrongpham0513/cdtn1-phamtritrong-l5/commit/dd354f88fd3b1a6c2eb0ca2600aa430ed014db1e) — `fix(error): khong lo chi tiet noi bo trong phan hoi 500 (S4)`. Tái hiện lại với lệnh `SEC-03` nhận thông báo HTTP 500 an toàn, trung tính; chạy lệnh `.\mvnw.cmd test` đạt **12/12 test PASS (100%)**.

---

## 4. Ghi chú lịch sử và Liêm chính học thuật

* **Bối cảnh phát hiện lỗi BT3:** Cả 03 lỗi trên được phát hiện trong giai đoạn kiểm thử Bài tập 3 (BT3) khi thực hiện rà soát an toàn bảo mật và kiểm soát truy cập theo các tiêu chuẩn OWASP A01 / S2 (kiểm soát phân quyền theo chủ sở hữu) và S4 (ngăn ngừa rò rỉ thông tin nội bộ).
* **Phân định rõ ràng với bộ kiểm thử tự động:** Để đảm bảo tính chính xác và liêm chính học thuật đối với bộ 12 test case tự động (7 Unit + 3 Integration + 2 API) đã được báo cáo trong `docs/test-plan.md` và `docs/test-report.md`, các ca kiểm thử bảo mật `SEC-01`, `SEC-02`, `SEC-03` được phân loại rõ ràng là **kiểm thử bảo mật thủ công bằng lệnh cURL ngoài bộ 12 automated test cases**, hoàn toàn nhất quán với nội dung đã ghi nhận tại Mục 6 (Known Gaps #5) của Test Report.
* **Lịch sử sửa lỗi các giai đoạn trước (BT2):** Các lỗi kỹ thuật phát hiện và khắc phục trong giai đoạn Bài tập 2 được lưu giữ đầy đủ trong lịch sử commit của kho mã nguồn Git:
  * Commit `3fae257`: Chuẩn hóa dữ liệu seeder CSV và cơ chế đối soát tồn kho sổ cái (NFR-04).
  * Commit `52f8aaf`: Chuẩn hóa định dạng ký tự xuống dòng (LF / CRLF) cho kịch bản triển khai Docker.
  * Commit `551b5a1`: Tối ưu hóa chỉ mục (indexing) bảng CSDL MySQL và cấu hình cổng kết nối dịch vụ.
* **Đảm bảo cấu trúc tài liệu:** Toàn bộ 03 lỗi trong báo cáo này đều tuân thủ nghiêm ngặt định dạng 7 dòng chi tiết theo hướng dẫn đề bài BT3:
  1. *Mã và mức độ nghiêm trọng*
  2. *Phát hiện bởi test case*
  3. *Triệu chứng*
  4. *Cách tái lập*
  5. *Nguyên nhân gốc rễ (Root Cause)*
  6. *Cách sửa*
  7. *Link commit và xác nhận kiểm thử*
