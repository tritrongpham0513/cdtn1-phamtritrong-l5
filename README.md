# Quản lý kho linh kiện thay thế: Quản lý trung tâm ghi nhập kho, kỹ thuật viên xuất linh kiện cho phiếu bảo hành, hệ thống chặn xuất quá tồn, tự cập nhật số tồn và cảnh báo khi tồn dưới ngưỡng tối thiểu, đến khi mọi lần nhập – xuất được lưu vào lịch sử.

**Sinh viên**: Phạm Trí Trọng — MSSV: 2374802010525 — **Track**: SE (Kỹ thuật Phần mềm)  
**Học phần**: Chuyên đề Tốt nghiệp 1 · HK1, Năm học 2026 – 2027 · Trường Đại học Văn Lang  
**Luồng nghiệp vụ**: **L5 – Kho linh kiện thay thế** (Case study: Mekong Mobile Smart CRM)  

---

## 1. Mục tiêu
Hệ thống giải quyết bài toán quản lý kho linh kiện thay thế tại 6 trung tâm bảo hành của Mekong Mobile:
- Giúp **Kỹ thuật viên** tra cứu số tồn tức thời trước khi nhận sửa máy, tránh hẹn lại khách (~20 lần/tháng).
- Giúp **Quản lý trung tâm** ghi nhận nhập kho tức thời, theo dõi tồn kho và cảnh báo khi tồn xuống dưới ngưỡng tối thiểu (`min_threshold`).
- Chặn hoàn toàn việc xuất linh kiện vượt quá tồn kho thực tế, bảo đảm số tồn không âm và giảm tỉ lệ sai lệch 3–8% của sổ tay giấy.

## 2. Yêu cầu môi trường
- Java: JDK 17 LTS trở lên
- Build tool: Apache Maven 3.8+
- Cơ sở dữ liệu: MySQL 8.x
- Biến môi trường: xem file `.env.example`

## 3. Hướng dẫn chạy (BT2 yêu cầu ≤ 4 bước)
1. `cp .env.example .env` và điền cấu hình kết nối MySQL.
2. Nạp dữ liệu danh mục linh kiện mẫu từ thư mục `data/parts.csv`.
3. `mvn clean install`
4. `mvn spring-boot:run` -> Mở kiểm tra tại `http://localhost:8080/api/health`

## 4. Cấu trúc thư mục
- `docs/`: Chứa bản đặc tả yêu cầu SRS (`srs.md`), tài liệu API contract (`api-contract.md`), nhật ký thay đổi thiết kế (`thay-doi-thiet-ke.md`), bảng khai báo sử dụng AI (`ai-declaration.md`) và các file vẽ gốc (`.drawio`, `.fig`).
- `db/`: Chứa mã kịch bản khởi tạo CSDL MySQL 8.x (`schema.sql`) và dữ liệu khởi tạo.
- `src/`: Mã nguồn ứng dụng backend Spring Boot REST API và frontend prototype.
- `tests/`: Kịch bản kiểm thử tự động (JUnit 5 unit test & integration test).
- `data/`: Dữ liệu mẫu CSV của case study Mekong Mobile.

## 5. Kiểm thử
- `mvn test` -> Hiển thị kết quả kiểm thử và số lượng test case PASS.

## 6. Trạng thái hiện tại
- [x] Khởi tạo project, dựng cấu trúc chuẩn và smoke test môi trường (Buổi 2)
- [x] Hoàn thiện tài liệu kiến trúc, API contract, DDL CSDL và nhật ký thay đổi thiết kế theo phản hồi GVHD (`docs/thay-doi-thiet-ke.md`) (Buổi 8 - Chặng 1)
- [ ] Xây dựng module cốt lõi Sprint 1: Controller, Service, Repository, DTO và Entity (Buổi 8 - Chặng 2)
- [ ] Module nhập kho và tra cứu số tồn theo trung tâm (Buổi 8–10)
- [ ] Module xuất linh kiện theo phiếu bảo hành, chặn xuất quá tồn & cảnh báo tồn tối thiểu (Buổi 10–12)

