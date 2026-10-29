# NHẬT KÝ SỬA LỖI HỆ THỐNG (BUG LOG) — GIAI ĐOẠN 2

> **Dự án:** Mekong Mobile Smart CRM — Phân hệ Luồng L5 (Kho linh kiện thay thế)  
> **Học phần:** Chuyên đề Tốt nghiệp 1 · Học kỳ 1, Năm học 2026 – 2027 · Đại học Văn Lang  
> **Sinh viên:** Phạm Trí Trọng — MSSV: 2374802010525 (Track SE)  
> **Quy chuẩn ghi nhận:** Tuân thủ quy trình chẩn đoán 4 bước và cấu trúc chuẩn 7 dòng bắt buộc theo yêu cầu Buổi 11 và Buổi 12 (*Mã lỗi · Triệu chứng · Tái lập · Khoanh vùng · Nguyên nhân · Cách sửa · Xác nhận & Commit*).

---

## BUG-01: Lỗi nạp bỏ qua dữ liệu lịch sử giao dịch kho từ tệp CSV

- **Mã lỗi:** `BUG-BT2-01` (Severity: Major · Component: Backend Seeder)
- **Triệu chứng:** Khi mở Màn hình M3 (Sổ cái lịch sử giao dịch), bảng dữ liệu trống trơn (Empty State) dù tệp hạt giống `seeds/part_transactions.csv` có gần 5.000 dòng dữ liệu thực tế.
- **Tái lập:** Khởi động ứng dụng với profile `local` hoặc `dev`, truy cập endpoint `GET /api/v1/transactions?limit=20`, nhận về mảng rỗng `{"items": [], "meta": {"total": 0}}`.
- **Khoanh vùng:** Kiểm tra tệp `DataInitializer.java` tại phương thức `loadTransactions()` đọc tệp CSV.
- **Nguyên nhân:** Điều kiện kiểm tra định dạng dòng CSV yêu cầu `cols.length >= 8`, trong khi tập dữ liệu mẫu chuẩn `seeds/part_transactions.csv` của đề bài Mekong Mobile chỉ có đúng 7 cột, khiến logic seeder bỏ qua toàn bộ 4.954 dòng dữ liệu.
- **Cách sửa:** Sửa lại điều kiện kiểm tra `if (cols.length >= 7)`, chuẩn hóa ánh xạ từng cột theo đúng thứ tự (`transaction_code`, `center_id`, `part_code`, `ticket_code`, `type`, `quantity`, `created_at`), tự động định dạng mã giao dịch dạng `TX-%06d` nếu thiếu.
- **Xác nhận & Commit:** Chạy lại ứng dụng, nạp thành công 4.954 bản ghi giao dịch; kiểm tra commit `3fae257` (`fix(backend): toi uu khoi tao du lieu, countQuery phan trang va profile local h2`).

---

## BUG-02: Lỗi biên dịch phân trang khi kết hợp JOIN FETCH trong Spring Data JPA

- **Mã lỗi:** `BUG-BT2-02` (Severity: Critical · Component: Data Persistence)
- **Triệu chứng:** Khi truy vấn phân trang danh sách giao dịch kho tại Màn hình M3, ứng dụng ném ngoại lệ `QueryException: query specified join fetching, but the owner of the fetched association was not present in the select list` hoặc đếm sai tổng số trang.
- **Tái lập:** Gửi yêu cầu `GET /api/v1/transactions?page=1&limit=20` kèm các tham số lọc `partId` hoặc `type`.
- **Khoanh vùng:** Tệp [PartTransactionRepository.java](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/repository/PartTransactionRepository.java), phương thức truy vấn `findTransactionHistory`.
- **Nguyên nhân:** Khi sử dụng `JOIN FETCH` để tải trước các thực thể quan hệ (`serviceCenter`, `part`, `ticket`) tránh lỗi N+1 Query trong Spring Data JPA, Hibernate cần một câu truy vấn đếm riêng (`countQuery`) nhưng câu query đếm mặc định lại giữ nguyên mệnh đề `FETCH JOIN`, gây xung đột cú pháp đếm số bản ghi.
- **Cách sửa:** Bổ sung thuộc tính `countQuery` tường minh bên trong chú thích `@Query`:  
  `countQuery = "SELECT COUNT(t) FROM PartTransaction t WHERE ..."` tách rời hoàn toàn với câu lệnh `SELECT t FROM PartTransaction t JOIN FETCH ...`.
- **Xác nhận & Commit:** Kiểm tra phân trang hoạt động chính xác từ trang 1 đến trang cuối; xác nhận qua commit `3fae257`.

---

## BUG-03: Lỗi không tìm thấy file thực thi khi build Docker trên Windows (`./mvnw: not found`)

- **Mã lỗi:** `BUG-BT2-03` (Severity: Blocker · Component: DevOps / Docker)
- **Triệu chứng:** Khi chạy lệnh `docker compose up -d --build`, Docker dừng ở bước 9/9 với thông báo lỗi: `/bin/sh: ./mvnw: not found` (exit code: 127).
- **Tái lập:** Chạy lệnh `docker compose up -d --build` trên môi trường Windows với repository vừa clone từ GitHub.
- **Khoanh vùng:** Tệp [Dockerfile](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/Dockerfile) tại giai đoạn Stage 1 (Builder trên nền image `eclipse-temurin:17-jdk-alpine`).
- **Nguyên nhân:** Do cơ chế `core.autocrlf=true` của Git trên Windows tự động chuyển đổi ký tự kết thúc dòng của script `mvnw` thành Windows CRLF (`\r\n`). Khi đưa vào Alpine Linux, trình thông dịch tìm kiếm `/bin/sh\r` (không tồn tại), dẫn đến lỗi báo không tìm thấy tệp.
- **Cách sửa:** Bổ sung lệnh chuyển đổi dòng `sed -i 's/\r$//' mvnw` và cài đặt `curl` trong Dockerfile trước khi thực thi Maven; đồng thời tạo tệp `.gitattributes` cấu hình `mvnw text eol=lf` để ép buộc định dạng Unix LF vĩnh viễn trên mọi hệ điều hành.
- **Xác nhận & Commit:** Docker build thành công 100% không còn lỗi; xác nhận qua commit `52f8aaf` (`fix(docker): chuan hoa line endings cho mvnw tren windows va loai bo version obsolete`).

---

## BUG-04: Lỗi cú pháp tạo chỉ mục và xung đột cổng khi chạy MySQL trong Docker

- **Mã lỗi:** `BUG-BT2-04` (Severity: Major · Component: Database / Docker Compose)
- **Triệu chứng:** Container MySQL và Spring Boot trong Docker báo lỗi không chiếm được cổng `3306` và `8080` trên máy chủ; Flyway báo lỗi cú pháp SQL `Error Code: 1064 near 'IF NOT EXISTS idx_stock_center_part...'`.
- **Tái lập:** Chạy `docker compose up -d` trên máy tính đã có cài sẵn MySQL Server cục bộ và Windows WinNAT đang chiếm dải cổng `7991–8090`.
- **Khoanh vùng:** Tệp [docker-compose.yml](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/docker-compose.yml) và kịch bản [V1__init_schema.sql](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/resources/db/migration/V1__init_schema.sql).
- **Nguyên nhân:** 
  1. MySQL Server trên máy thật đang chiếm cổng `3306`, và dải cổng loại trừ của Windows Hyper-V/WinNAT chiếm cổng `8080`.
  2. Hệ quản trị CSDL MySQL 8.0 không hỗ trợ mệnh đề `CREATE INDEX IF NOT EXISTS` (cú pháp này của PostgreSQL/SQLite).
- **Cách sửa:** 
  1. Sửa `V1__init_schema.sql` thành cú pháp chuẩn của MySQL: `CREATE INDEX idx_... ON table(...)`.
  2. Điều chỉnh mapping cổng trong `docker-compose.yml`: cổng MySQL chuyển sang `${MYSQL_HOST_PORT:-3307}:3306` và cổng Web chuyển sang `${APP_PORT:-8888}:8080`.
- **Xác nhận & Commit:** Cả 2 container khởi động thành công ở trạng thái `healthy`; API phản hồi tốt tại `http://localhost:8888/api/health`; xác nhận qua commit `551b5a1` (`fix(db): tuong thich cu phap index mysql 8 va tranh xung dot port tren windows`).

