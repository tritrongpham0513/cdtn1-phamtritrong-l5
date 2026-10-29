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
| **— Không dùng AI —** *(Tự làm hoàn toàn 100%)* | Tự thiết kế và hiện thực toàn bộ quy tắc nghiệp vụ kiểm tra trạng thái vòng đời phiếu bảo hành (QT-06); tự tay thiết kế thuật toán khóa bi quan chống xuất âm kho; tự cài đặt cơ chế xác thực token cơ bản và ma trận phân quyền RBAC; tự cấu hình toàn bộ ràng buộc toàn vẹn CSDL MySQL 8.x. | • `PartTransactionServiceImpl.java` (hàm `validateTicketForExport`, `exportPart`)<br>• `AuthTokenService.java` (hàm `requireRole`, `validateToken`)<br>• `migrations/V1__init_schema.sql` (ràng buộc `CHECK (quantity >= 0)`) | Tự tay kiểm chứng từng câu lệnh điều kiện; viết các kịch bản kiểm thử biên; kiểm tra log Hibernate trên console; gọi thử nghiệm trực tiếp qua curl và Postman với các dữ liệu hợp lệ và không hợp lệ. |
| **Antigravity AI IDE (Claude 3.7 Sonnet & Gemini 3.8 Flash)** | Gợi ý cấu trúc phân tầng Spring Boot 3 lớp (Routes/Services/Repositories) chuẩn Clean Architecture và sinh mã khung (scaffold) các Entity JPA, Repository và DTO. | • `src/main/java/com/mekongmobile/inventory/entity/*`<br>• `repository/*`<br>• `dto/request/*`, `dto/response/*` | Tự rà soát 100% thuộc tính của các Entity, đối chiếu từng tên bảng, tên cột và kiểu dữ liệu với kịch bản CSDL `V1__init_schema.sql`; xóa bỏ các thuộc tính thừa; bổ sung các annotation `@NotBlank`, `@DecimalMin` để kiểm soát dữ liệu đầu vào. |
| **Claude Sonnet** | Đề xuất cú pháp sử dụng chú thích `@Lock(LockModeType.PESSIMISTIC_WRITE)` của Spring Data JPA và cấu hình Flyway Migration tự động từ CSDL trống. | • `PartStockRepository.java` (phương thức `findByCenterIdAndPartCodeForUpdate`)<br>• `pom.xml`, `application-dev.properties`<br>• `migrations/V1__init_schema.sql` | Chạy thử nghiệm thực tế với MySQL 8.0; kiểm tra log SQL của Hibernate để xác nhận câu lệnh `SELECT ... FOR UPDATE` được phát sinh chính xác; thử nghiệm khởi động ứng dụng trên một database MySQL hoàn toàn trống và xác nhận Flyway tự động tạo đủ 5 bảng cùng các chỉ mục. |
| **Claude Sonnet** | Gợi ý thuật toán nạp dữ liệu mẫu Seeder chống trùng lặp dữ liệu theo Khóa duy nhất (Unique Key) và công thức đối soát tồn kho thực tế NFR-04. | • `src/main/java/com/mekongmobile/inventory/config/DataInitializer.java` (hàm `seedDataIfEmpty`) | Khởi động ứng dụng hai lần liên tiếp; truy vấn CSDL MySQL đếm số dòng bằng lệnh `SELECT COUNT(*)` để đảm bảo số dòng không bị nhân đôi (đáp ứng tiêu chí cốt lõi C5); kiểm tra công thức $\text{tồn kho} = \sum(\text{nhập}) - \sum(\text{xuất})$ khớp 100% với dữ liệu mẫu CSV. |
| **Antigravity AI IDE** | Gợi ý khung mã kiểm thử đơn vị với Mockito cho 3 nhóm kiểm thử bắt buộc (Luồng đúng, Giá trị biên, Dữ liệu sai/quy tắc nghiệp vụ) và bộ test phân quyền RBAC. | • `src/test/java/com/mekongmobile/inventory/service/PartTransactionServiceTest.java`<br>• `PartServiceTest.java`<br>• `AuthTokenServiceTest.java` | Tự bổ sung các câu lệnh `assertEquals`, `assertThrows` cho từng trường hợp biên; bổ sung test case từ chối xuất cho phiếu đã đóng; chạy lệnh `./mvnw test` xác nhận toàn bộ 17/17 test cases PASS 100% trên CSDL H2 in-memory độc lập không phụ thuộc dữ liệu ngoài. |
| **Antigravity AI IDE** | Gợi ý khung mã HTML/CSS/JS cho 3 màn hình Prototype và lớp client gọi API trung gian `api.js` xử lý 3 trạng thái (*Loading, Empty, Error*). | • `src/main/resources/static/index.html`<br>• `src/main/resources/static/js/api.js`<br>• `src/main/resources/static/js/app.js`<br>• `src/main/resources/static/css/style.css` | Tự kiểm chứng trực quan trên trình duyệt Chrome: thử ngắt kết nối backend để kiểm tra trạng thái Lỗi có nút Thử lại hoạt động; tìm kiếm từ khóa không có để kiểm tra trạng thái Rỗng; đăng nhập bằng tài khoản KTV và Quản lý kho để xác nhận giao diện ẩn/khóa nút nghiệp vụ đúng ma trận RBAC. |
| **Claude Sonnet** | Gợi ý cấu trúc tệp `Dockerfile` đa tầng (Multi-stage) và `docker-compose.yml` tích hợp sẵn container MySQL 8.0 và healthcheck. | • `Dockerfile`<br>• `docker-compose.yml`<br>• `.dockerignore` | Đọc và kiểm tra từng chỉ thị trong Dockerfile; cấu hình người dùng non-root (`appuser`) để đảm bảo an toàn vận hành; kiểm tra cấu hình mạng và dependency giữa MySQL và Spring Boot. |

---

## 2. Chuẩn bị cho Bộ Ba Câu Hỏi Chuẩn tại Buổi 12

Để đảm bảo đáp ứng yêu cầu giải trình trực tiếp tại buổi đánh giá theo quy định của môn học, em đã tự rà soát và hiểu rõ từng dòng mã của hệ thống theo 3 câu hỏi cốt lõi:

### Câu 1: Vì sao em chọn giải pháp này mà không phải giải pháp kia?
* **Khóa bi quan (`PESSIMISTIC_WRITE`) thay vì Khóa lạc quan (`@Version`):**  
  * *Lý do:* Nghiệp vụ xuất kho linh kiện sửa chữa tại trung tâm bảo hành đòi hỏi tính nhất quán tuyệt đối của số tồn tức thời (QT-09 & NFR-02). Nếu dùng Khóa lạc quan, khi 2 kỹ thuật viên cùng bấm xuất 1 linh kiện chỉ còn tồn 1 cái, người thứ hai sẽ bị văng ngoại lệ `OptimisticLockException` và phải thao tác lại từ đầu. Khóa bi quan phát sinh `SELECT ... FOR UPDATE` tuần tự hóa các giao dịch ở tầng CSDL, giúp giao dịch thứ hai đọc được số tồn đã về 0 và nhận thông báo lỗi nghiệp vụ rõ ràng, không làm gián đoạn trải nghiệm của kỹ thuật viên.
* **Cơ chế Soft-Delete cho Linh kiện thay vì Hard-Delete vật lý:**  
  * *Lý do:* Để bảo toàn tính toàn vẹn dữ liệu lịch sử của sổ cái giao dịch (`part_transactions`) và các phiếu bảo hành cũ theo nguyên tắc NFR-04. Khi xóa linh kiện, hệ thống chỉ cập nhật cờ `isActive = false` để ẩn khỏi danh mục xuất mới, không làm hỏng các ràng buộc khóa ngoại trong quá khứ.
* **Kiểm thử trên CSDL H2 in-memory thay vì kết nối trực tiếp MySQL:**  
  * *Lý do:* Đảm bảo tiêu chí C4: bộ kiểm thử tự động độc lập 100%, có thể chạy trên bất kỳ máy tính nào của Giảng viên chỉ bằng 1 lệnh `./mvnw test` mà không cần cài đặt hoặc khởi động CSDL MySQL ngoài.

### Câu 2: Nếu dữ liệu tăng gấp 10 lần thì chỗ nào hỏng trước?
* **Điểm nghẽn:** Bảng sổ cái giao dịch `part_transactions` sẽ tăng nhanh nhất (từ 5.000 lên 50.000+ bản ghi), dẫn đến việc truy vấn tra cứu lịch sử trên màn hình M3 bị suy giảm hiệu năng.
* **Biện pháp khắc phục đã chuẩn bị:**  
  1. Trong kịch bản [V1__init_schema.sql](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/migrations/V1__init_schema.sql), em đã đánh sẵn chỉ mục phức hợp `idx_tx_part_created (part_id, created_at)` để tăng tốc độ quét theo linh kiện và mốc thời gian.
  2. Về lâu dài, có thể áp dụng kỹ thuật phân vùng bảng (Table Partitioning) theo tháng hoặc chuyển các giao dịch cũ hơn 1 năm sang bảng lưu trữ lạnh (Cold Storage).
  3. Nếu một mã linh kiện cực kỳ "hot" có hàng chục kỹ thuật viên cùng thao tác, hàng đợi khóa bi quan có thể kéo dài; giải pháp nâng cao ở Bài tập 3 sẽ là áp dụng cơ chế giữ chỗ trước (Reservation Queue) qua Redis.

### Câu 3: Chỉ cho thầy dòng mã chịu trách nhiệm cho việc này?
* **Cơ chế Khóa bi quan chống xuất âm kho:**  
  $\rightarrow$ Tệp [PartStockRepository.java](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/repository/PartStockRepository.java), phương thức `findByCenterIdAndPartCodeForUpdate`, dòng chứa chú thích `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
* **Quy tắc nghiệp vụ chặn xuất âm kho:**  
  $\rightarrow$ Tệp [PartTransactionServiceImpl.java](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/service/impl/PartTransactionServiceImpl.java), phương thức `exportPart`, đoạn kiểm tra:  
  `if (stock.getQuantity() < request.getQuantity()) throw new BusinessRuleException(...)`.
* **Quy tắc nghiệp vụ kiểm tra trạng thái phiếu bảo hành (QT-06):**  
  $\rightarrow$ Tệp [PartTransactionServiceImpl.java](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/service/impl/PartTransactionServiceImpl.java), phương thức `validateTicketForExport`, đoạn kiểm tra:  
  `if (!STATUS_CAN_EXPORT.contains(ticket.getStatus())) throw new BusinessRuleException(...)`.
* **Kiểm soát phân quyền KTV vs Quản lý kho (RBAC):**  
  $\rightarrow$ Tệp [AuthTokenService.java](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/service/AuthTokenService.java), phương thức `requireRole`.
* **Cơ chế nạp dữ liệu mẫu chống trùng lặp (C5):**  
  $\rightarrow$ Tệp [DataInitializer.java](file:///d:/chuyen-de-tot-nghiep-01/cdtn1-phamtritrong-l5/src/main/java/com/mekongmobile/inventory/config/DataInitializer.java), phương thức `seedDataIfEmpty`.

---

## 3. Lời Cam Kết Liêm Chính Học Thuật và Trách Nhiệm Cá Nhân

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
