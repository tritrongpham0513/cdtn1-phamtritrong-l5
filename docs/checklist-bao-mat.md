# Checklist Bảo mật 7 mục — OWASP Top 10:2025 (BT3)

- **Ngày chạy kiểm chứng:** 12/11/2026
- **Commit kiểm chứng:** `c689ec1` (commit mã nguồn); tag `v1.0` trỏ commit đóng gói tài liệu cuối cùng
- **Môi trường thực thi:** Docker Desktop 28.5.1, Docker Compose v2.40.3-desktop.1, Windows 11 Host
- **Cam kết tính trung thực:** Mọi minh chứng dưới đây được chạy thật trong phiên này.

---

### Bảng tổng hợp 7 mục kiểm tra bảo mật (OWASP Top 10:2025)

| STT | Mục kiểm tra | Trạng thái | Minh chứng kiểm tra thực tế | Kế hoạch khắc phục |
| :---: | :--- | :---: | :--- | :--- |
| 1 | **Không có secret nào trong repo (OWASP A02)** | ĐÃ LÀM | `git ls-files \| Select-String -Pattern '\.env$'` rỗng; `.gitignore:9` chặn `.env`; `.env.example` chỉ chứa cấu hình demo công khai; mã nguồn không còn mật khẩu plaintext, chỉ có hash BCrypt trong bảng `users`. | Không có |
| 2 | **Dữ liệu vào được kiểm ở tầng ứng dụng (OWASP A05)** | ĐÃ LÀM | Annotation `@NotBlank`, `@NotNull`, `@Min(1)`, `@Size` tại `ImportTransactionRequestDTO.java` (dòng 16–27); gửi `{}` tới `POST /api/v1/transactions/import` bị từ chối bằng HTTP 422 `VALIDATION_ERROR`. | Không có |
| 3 | **Kiểm soát truy cập theo chủ sở hữu (OWASP A01)** | ĐÃ LÀM | **Manual:** đăng nhập `ktv_cantho` gọi `POST /api/v1/transactions/export` với `X-Center-Id: 1` bị chặn HTTP 403 `FORBIDDEN`. **Automated:** `TC-07 / testExportPart_Error_CrossCenterOwnershipViolation` ném `IllegalArgumentException` PASS ở tầng service. | Không có |
| 4 | **Mật khẩu và dữ liệu nhạy cảm được băm/mã hoá (OWASP A04, A07)** | ĐÃ LÀM | `migrations/V2__add_users_table.sql` tạo bảng `users` với cột `password_hash`; `AuthController` xác thực bằng `BCryptPasswordEncoder.matches()`; `SELECT username, LEFT(password_hash,7) AS prefix, LENGTH(password_hash) AS len FROM users;` trả về `$2a$10$` / 60 ký tự cho cả 3 tài khoản. | Không có |
| 5 | **Thông báo lỗi không lộ cấu trúc bên trong (OWASP A10, A02)** | ĐÃ LÀM | `GlobalExceptionHandler.java` (dòng 68–78) bọc lỗi 500 thành `INTERNAL_SERVER_ERROR` trung tính; gửi JSON lỗi cú pháp `"{bad json"` nhận HTTP 500 không lộ stack trace hay cấu trúc CSDL. | Không có |
| 6 | **Nhật ký không ghi secret hay dữ liệu cá nhân (OWASP A09)** | ĐÃ LÀM | `docker compose logs inventory-service 2>&1 \| Select-String -Pattern '\b(password\|token\|secret\|bearer\|authorization)\b\|0[0-9]{9}'` lọc chính xác không có dòng nào rò rỉ secret hay dữ liệu cá nhân. | Không có |
| 7 | **Phụ thuộc được ghim phiên bản và đã kiểm cảnh báo lỗ hổng (OWASP A03)** | CHƯA LÀM | `pom.xml` (dòng 7–12, 21, 46–60) đã khóa cứng phiên bản `spring-boot 3.2.5`, `Java 17`, `mysql-connector-j`, `flyway`, nhưng repo chưa tích hợp tool tự động quét lỗ hổng CVE trong CI/CD. | Cấu hình OWASP Dependency-Check Maven Plugin hoặc GitHub Dependabot scanner trong pipeline CI/CD ở học phần CĐTN2. |

---

### Chi tiết nhật ký thực thi các lệnh kiểm chứng

#### 1. Kiểm tra không rò rỉ secret vào git (Mục 1)
```text
$ git ls-files | Select-String -Pattern '\.env$'
(kết quả: rỗng)

$ git log --all --oneline -- .env
(kết quả: rỗng)

$ Select-String -Path .gitignore -Pattern '\.env'
.gitignore:9:.env
.gitignore:10:.env.local
```

#### 2. Kiểm tra Bean Validation từ chối dữ liệu rỗng (Mục 2)
```text
$ curl.exe -s -w "\nHTTP_CODE:%{http_code}" -X POST http://localhost:8088/api/v1/transactions/import -H "Content-Type: application/json" -d "{}"
{"success":false,"error":{"code":"VALIDATION_ERROR","message":"Dữ liệu gửi lên không đúng định dạng","details":{"performedBy":"Người thực hiện không được để trống","quantity":"Số lượng nhập không được để trống","partId":"Mã linh kiện không được để trống"}},"timestamp":"2026-11-12T01:01:49.424064112"}
HTTP_CODE:422
```

#### 3. Kiểm tra Authorization RBAC và Center ID Ownership (Mục 3)
```text
# Minh chứng thủ công (Manual API check):
# KTV Cần Thơ (Center 4) cố tình gửi header giả mạo X-Center-Id=1:
$ Invoke-RestMethod -Uri "http://localhost:8088/api/v1/transactions/export" -Headers @{"Authorization"="Bearer mk_token_MTAz...[DEMO_TOKEN_MASKED]...OTA=","X-Center-Id"="1"}
HTTP Status: 403 Forbidden
Body: {"success":false,"error":{"code":"FORBIDDEN","message":"Từ chối truy cập (403 Forbidden): X-Center-Id=1 không khớp với trung tâm 4 của tài khoản 'ktv_cantho'."},"timestamp":"2026-11-12T01:01:49.764696299"}

# Minh chứng tự động (Automated Unit Test check):
# TC-07 / testExportPart_Error_CrossCenterOwnershipViolation kiểm tra ở tầng service thuần (kỳ vọng IllegalArgumentException, không phải HTTP 403):
$ .\mvnw.cmd test -Dtest=PartTransactionServiceTest#testExportPart_Error_CrossCenterOwnershipViolation
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- BUILD SUCCESS
```

#### 4. Kiểm tra bảng users lưu mật khẩu băm BCrypt (Mục 4)
```text
$ docker compose exec -T mysql-db mysql -uspringstudent -pspringstudent mekong_crm_inventory -e "SELECT username, LEFT(password_hash,7) AS prefix, LENGTH(password_hash) AS len FROM users;"
username      prefix   len
ktv_trong     $2a$10$  60
quanly_kho    $2a$10$  60
ktv_cantho    $2a$10$  60

$ curl.exe -s -o NUL -w "%{http_code}" -X POST http://localhost:8088/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"quanly_kho","password":"123456"}'
200
$ curl.exe -s -o NUL -w "%{http_code}" -X POST http://localhost:8088/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"quanly_kho","password":"wrong"}'
409
```

#### 5. Kiểm tra thông báo lỗi hệ thống không lộ cấu trúc (Mục 5)
```text
$ curl.exe -s -X POST http://localhost:8088/api/v1/transactions/import -H "Content-Type: application/json" -d "{bad json"
{"success":false,"error":{"code":"INTERNAL_SERVER_ERROR","message":"Đã xảy ra lỗi hệ thống. Vui lòng liên hệ quản trị viên."},"timestamp":"2026-11-12T01:01:49.833481901"}
```
> Known limitation: malformed JSON hiện trả 500 vì `HttpMessageNotReadableException` rơi vào handler `Exception.class`; đúng ra nên là 400. Phản hồi vẫn không rò rỉ stack hay cấu trúc CSDL.

#### 6. Kiểm tra nhật ký container (Mục 6)
```text
$ docker compose logs inventory-service 2>&1 | Select-String -Pattern '\b(password|token|secret|bearer|authorization)\b|0[0-9]{9}'
(kết quả: rỗng - không có dòng nào ghi nhận password, token phiên, header authorization, hoặc số điện thoại)
```

#### 7. Kiểm tra ghim phiên bản trong Maven (Mục 7)
```text
pom.xml dòng 7-12: spring-boot-starter-parent version 3.2.5
pom.xml dòng 21: java.version 17
pom.xml dòng 46-60: mysql-connector-j, flyway-core, flyway-mysql
```
