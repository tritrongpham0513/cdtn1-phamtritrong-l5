# Phụ Lục Bắt Buộc — Bảng Khai Báo Sử Dụng Công Cụ AI Hỗ Trợ

> **Học phần:** Chuyên đề tốt nghiệp 1 – Học kỳ 1, Năm học 2026 – 2027  
> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525  
> **Chuyên ngành (Track):** Kỹ thuật Phần mềm (Software Engineering - SE)  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế (Mekong Mobile Smart CRM)  
> **Bài tập:** Bài tập 2 – Hiện thực hóa module cốt lõi  
> **Phiên bản:** Release Tag `v0.2`  

---

## 1. Bảng kê khai chi tiết sử dụng công cụ AI (Chuẩn hóa 4 cột theo Rubric BT2)

| Công cụ | Dùng vào việc gì | Áp dụng ở tệp / hàm nào | Em đã kiểm chứng thế nào |
| :--- | :--- | :--- | :--- |
| **— Không dùng AI —** | Tự thiết kế và hiện thực toàn bộ quy tắc nghiệp vụ kiểm tra trạng thái vòng đời phiếu bảo hành (QT-06); tự tay thiết kế thuật toán khóa bi quan chống xuất âm kho; tự cài đặt cơ chế xác thực token cơ bản và ma trận phân quyền RBAC; tự cấu hình toàn bộ ràng buộc toàn vẹn CSDL MySQL 8.x. | • `PartTransactionServiceImpl.java` (hàm `validateTicketForExport`, `exportPart`)<br>• `AuthTokenService.java` (hàm `requireRole`, `validateToken`)<br>• `migrations/V1__init_schema.sql` (ràng buộc `CHECK (quantity >= 0)`) | Tự tay kiểm chứng từng câu lệnh điều kiện; viết các kịch bản kiểm thử biên; kiểm tra log Hibernate trên console; gọi thử nghiệm trực tiếp qua curl và Postman với các dữ liệu hợp lệ và không hợp lệ. |
| **Antigravity AI IDE (Claude 3.7 Sonnet & Gemini 3.8 Flash)** | Gợi ý cấu trúc phân tầng Spring Boot 3 lớp (Routes/Services/Repositories) chuẩn Clean Architecture và sinh mã khung (scaffold) các Entity JPA, Repository và DTO. | • `src/main/java/com/mekongmobile/inventory/entity/*`<br>• `repository/*`<br>• `dto/request/*`, `dto/response/*` | Tự rà soát 100% thuộc tính của các Entity, đối chiếu từng tên bảng, tên cột và kiểu dữ liệu với kịch bản CSDL `V1__init_schema.sql`; xóa bỏ các thuộc tính thừa; bổ sung các annotation `@NotBlank`, `@DecimalMin` để kiểm soát dữ liệu đầu vào. |
| **Claude Sonnet** | Đề xuất cú pháp sử dụng chú thích `@Lock(LockModeType.PESSIMISTIC_WRITE)` của Spring Data JPA và cấu hình Flyway Migration tự động từ CSDL trống. | • `PartStockRepository.java` (phương thức `findByCenterIdAndPartCodeForUpdate`)<br>• `pom.xml`, `application-dev.properties`<br>• `migrations/V1__init_schema.sql` | Chạy thử nghiệm thực tế với MySQL 8.0; kiểm tra log SQL của Hibernate để xác nhận câu lệnh `SELECT ... FOR UPDATE` được phát sinh chính xác; thử nghiệm khởi động ứng dụng trên một database MySQL hoàn toàn trống và xác nhận Flyway tự động tạo đủ 5 bảng cùng các chỉ mục. |
| **Claude Sonnet** | Gợi ý thuật toán nạp dữ liệu mẫu Seeder chống trùng lặp dữ liệu theo Khóa duy nhất (Unique Key) và công thức đối soát tồn kho thực tế NFR-04. | • `src/main/java/com/mekongmobile/inventory/config/DataInitializer.java` (hàm `seedDataIfEmpty`) | Khởi động ứng dụng hai lần liên tiếp; truy vấn CSDL MySQL đếm số dòng bằng lệnh `SELECT COUNT(*)` để đảm bảo số dòng không bị nhân đôi (đáp ứng tiêu chí cốt lõi C5); kiểm tra công thức $\text{tồn kho} = \sum(\text{nhập}) - \sum(\text{xuất})$ khớp 100% với dữ liệu mẫu CSV. |
| **Antigravity AI IDE** | Gợi ý khung mã kiểm thử đơn vị với Mockito cho 3 nhóm kiểm thử bắt buộc (Luồng đúng, Giá trị biên, Dữ liệu sai/quy tắc nghiệp vụ) và bộ test phân quyền RBAC. | • `src/test/java/com/mekongmobile/inventory/service/PartTransactionServiceTest.java`<br>• `PartServiceTest.java`<br>• `AuthTokenServiceTest.java` | Tự bổ sung các câu lệnh `assertEquals`, `assertThrows` cho từng trường hợp biên; bổ sung test case từ chối xuất cho phiếu đã đóng; chạy lệnh `./mvnw test` xác nhận toàn bộ 17/17 test cases PASS 100% trên CSDL H2 in-memory độc lập không phụ thuộc dữ liệu ngoài. |
| **Antigravity AI IDE** | Gợi ý khung mã HTML/CSS/JS cho 3 màn hình Prototype và lớp client gọi API trung gian `api.js` xử lý 3 trạng thái (*Loading, Empty, Error*). | • `src/main/resources/static/index.html`<br>• `src/main/resources/static/js/api.js`<br>• `src/main/resources/static/js/app.js`<br>• `src/main/resources/static/css/style.css` | Tự kiểm chứng trực quan trên trình duyệt Chrome: thử ngắt kết nối backend để kiểm tra trạng thái Lỗi có nút Thử lại hoạt động; tìm kiếm từ khóa không có để kiểm tra trạng thái Rỗng; đăng nhập bằng tài khoản KTV và Quản lý kho để xác nhận giao diện ẩn/khóa nút nghiệp vụ đúng ma trận RBAC. |
| **Claude Sonnet** | Gợi ý cấu trúc tệp `Dockerfile` đa tầng (Multi-stage) và `docker-compose.yml` tích hợp sẵn container MySQL 8.0 và healthcheck. | • `Dockerfile`<br>• `docker-compose.yml`<br>• `.dockerignore` | Đọc và kiểm tra từng chỉ thị trong Dockerfile; cấu hình người dùng non-root (`appuser`) để đảm bảo an toàn vận hành; kiểm tra cấu hình mạng và dependency giữa MySQL và Spring Boot. |

---

## 2. Lời Cam Kết Liêm Chính Học Thuật và Trách Nhiệm Cá Nhân

> **"Tôi xác nhận đã đọc, hiểu và chịu trách nhiệm về toàn bộ nội dung nộp."**

Tôi xin cam đoan:
1. Bảng kê khai trên phản ánh trung thực, chính xác và đầy đủ 100% quá trình sử dụng các công cụ trí tuệ nhân tạo (AI) trong quá trình thực hiện Bài tập 2 môn Chuyên đề Tốt nghiệp 1.
2. Các công cụ AI chỉ đóng vai trò hỗ trợ gợi ý cú pháp, cấu trúc khung mã và tối ưu thuật toán. Toàn bộ logic nghiệp vụ cốt lõi luồng L5, các quy tắc kiểm tra phiếu bảo hành (QT-06), chống xuất âm kho (QT-09 & NFR-02), kiến trúc phân tầng sạch và các bộ kiểm thử tự động đều do chính tôi trực tiếp kiểm soát, kiểm chứng trên môi trường thực tế và hoàn toàn hiểu rõ từng dòng mã.
3. Tôi sẵn sàng trả lời và giải thích trực tiếp với Giảng viên về bất kỳ dòng mã nào trong hệ thống theo bộ ba câu hỏi chuẩn tại buổi đánh giá.

- **Sinh viên cam kết:** Phạm Trí Trọng  
- **Mã số sinh viên (MSSV):** 2374802010525  
- **Chuyên ngành (Track):** Kỹ thuật Phần mềm (Software Engineering - SE)  
- **Luồng nghiệp vụ:** L5 — Kho linh kiện thay thế (Mekong Mobile Smart CRM)  
- **Ngày cam kết:** 28/10/2026  
- **Chữ ký xác nhận:** *(Đã ký điện tử)*  
