# Thành phần 3: Thiết kế Kiến trúc Hệ thống – Kho linh kiện thay thế (L5)

> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525 – Track SE  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế (Smart CRM Mekong Mobile)  
> **File sơ đồ gốc:** [`docs/architecture.drawio`](architecture.drawio) — vẽ bằng draw.io, lưu file XML gốc  

---

## 1. Sơ đồ Kiến trúc Phân tầng (Layered Architecture)

Kiến trúc của module **L5 – Kho linh kiện thay thế** được thiết kế theo mẫu **Kiến trúc phân tầng 4 lớp (4-Tier Layered Architecture)** kết hợp với **Repository Pattern**, tuân thủ nghiêm ngặt **nguyên tắc phụ thuộc một chiều** (Top-down dependency).

```text
HỆ THỐNG: Kho linh kiện thay thế – Luồng L5  (track SE)

+-------------------------------------------------------------------------------+
|  LỚP TRÌNH BÀY                                                                |
|  [ Danh sách tồn kho ]  [ Nhập / Xuất kho ]  [ Lịch sử giao dịch ]            |
+-------------------------------------------------------------------------------+
|  v   HTTP + JSON   (hợp đồng API ở docs/api-contract.md)                      |
+-------------------------------------------------------------------------------+
|  LỚP NGHIỆP VỤ  (Service)                                                     |
|  StockService           – tra cứu tồn kho theo trung tâm, quản lý danh mục    |
|  PartTransactionService – nhập kho, xuất kho cho phiếu BH, chặn xuất âm       |
|  StockAlertService      – cảnh báo tồn thấp, thiết lập ngưỡng tối thiểu       |
|  >>> TOÀN BỘ quy tắc nghiệp vụ nằm trong lớp này <<<                          |
+-------------------------------------------------------------------------------+
|  v   gọi qua giao diện Repository                                             |
+-------------------------------------------------------------------------------+
|  LỚP TRUY CẬP DỮ LIỆU                                                         |
|  PartStockRepository   PartTransactionRepository                              |
|  PartRepository        TicketReadOnlyRepository                               |
+-------------------------------------------------------------------------------+
|  v   SQL                                                                      |
+-------------------------------------------------------------------------------+
|  LỚP LƯU TRỮ – MySQL 8.x                                                      |
|  parts, part_stock, part_transactions, service_centers,                       |
|  tickets  (dữ liệu mẫu từ luồng L2 theo RB-03)                                |
+-------------------------------------------------------------------------------+

CHÚ THÍCH:  [ ] = màn hình   |   v = hướng phụ thuộc (một chiều)
NGOÀI PHẠM VI (WON'T): đặt hàng nhà cung cấp tự động, điều chuyển linh kiện liên trung tâm
```

---

## 2. Trách nhiệm và Cách trao đổi giữa các lớp

### 2.1. Lớp Trình bày (Presentation Layer)
- **Thành phần:** Giao diện Web Prototype: `[ Danh sách tồn kho ]`, `[ Nhập / Xuất kho ]`, `[ Lịch sử giao dịch ]`.
- **Trách nhiệm:** Tiếp nhận thao tác từ người dùng, validate cú pháp sơ bộ (dữ liệu không rỗng, đúng định dạng số), gửi yêu cầu HTTP REST/JSON xuống tầng Service và hiển thị kết quả/thông báo lỗi.
- **Điều cấm:** Tuyệt đối không chứa câu lệnh SQL và không chứa quy tắc nghiệp vụ kiểm tra số tồn kho.

### 2.2. Lớp Nghiệp vụ (Business / Service Layer)
- **Thành phần:** Gồm 3 dịch vụ chuyên biệt thực thi 100% quy tắc nghiệp vụ của luồng L5:
  1. **`StockService`** (Quản lý số tồn & Danh mục):
     - Tra cứu số tồn kho của từng linh kiện theo từng trung tâm bảo hành cụ thể (**QT-14**, **US-01**).
     - Quản lý thông tin danh mục linh kiện, tra cứu mã/tên linh kiện (**AC-2.3**).
  2. **`PartTransactionService`** (Quản lý giao dịch Nhập / Xuất):
     - Xử lý nhập kho: kiểm tra số lượng nguyên $> 0$, cập nhật tăng số tồn (**QT-L5-01**, **US-02**).
     - Xử lý xuất kho: kiểm tra phiếu bảo hành phải thuộc cùng trung tâm và ở trạng thái hợp lệ (`Đang xử lý` / `Chờ linh kiện`) (**QT-06**, **QT-L5-02**, **US-03**).
     - **Chặn xuất vượt số tồn & Kiểm soát đồng thời:** Kiểm tra số lượng xuất $\le$ số tồn thực tế qua Khóa bi quan (Pessimistic Locking), tuyệt đối không để số tồn bị âm (**QT-09**, **QT-L5-03**, **NFR-02**, **US-04**).
     - **Tra cứu lịch sử giao dịch:** Truy vấn lịch sử nhập/xuất theo khoảng ngày, theo linh kiện hoặc theo phiếu bảo hành (**US-06**, **US-07**).
     - **Ghi log bất biến:** Ghi nhận mọi giao dịch theo mô hình Append-only log, không cho phép xóa sửa vật lý (**QT-13**, **NFR-04**).
  3. **`StockAlertService`** (Quản lý cảnh báo & Ngưỡng an toàn):
     - Tự động phát hiện và kích hoạt cảnh báo tồn thấp khi $quantity < min\_threshold$ (**QT-09**, **US-05**).
     - Cung cấp chức năng thiết lập và cập nhật ngưỡng cảnh báo tối thiểu cho từng linh kiện tại trung tâm (**US-08**, **FR-07**).
- **Điều cấm:** Không phụ thuộc trực tiếp vào công nghệ lưu trữ CSDL (chỉ gọi qua Repository Interface).

### 2.3. Lớp Truy cập dữ liệu (Repository / DAO Layer)
- **Thành phần:** `PartStockRepository`, `PartTransactionRepository`, `PartRepository`, `TicketReadOnlyRepository`.
- **Trách nhiệm:** Cung cấp giao diện trừu tượng để đọc và ghi dữ liệu đối tượng miền; che giấu cú pháp SQL và cơ chế giao tiếp JDBC/Hibernate bên dưới. Cung cấp hàm khóa dòng dữ liệu phục vụ xử lý đồng thời (`findWithLockByCenterIdAndPartId`).
- **Điều cấm:** Không chứa quy tắc kiểm tra logic nghiệp vụ (không tự quyết định khi nào được xuất kho).

### 2.4. Lớp Lưu trữ (Data Store Layer)
- **Thành phần:** Hệ quản trị CSDL quan hệ **MySQL 8.x**.
- **Trách nhiệm:** Đảm bảo toàn vẹn dữ liệu ở mức vật lý (ràng buộc khóa chính PK, khóa ngoại FK, ràng buộc toàn vẹn `CHECK quantity >= 0`, `CHECK min_threshold >= 0`, `UNIQUE part_code`). Thực thi lưu trữ bền vững (ACID transactions).

### 2.5. Nguyên tắc phụ thuộc một chiều (One-Way Dependency)
Hệ thống tuân thủ nghiêm ngặt nguyên tắc phụ thuộc từ trên xuống dưới:
$$\text{Presentation} \longrightarrow \text{Business Service} \longrightarrow \text{Repository} \longrightarrow \text{Data Store}$$
- Lớp trên gọi lớp dưới liền kề thông qua giao thức chuẩn hoặc Interface.
- Lớp dưới **tuyệt đối không bao giờ gọi ngược lên lớp trên** (ví dụ: Repository không bao giờ gọi Service hoặc Controller).

---

## 3. Lập luận Lựa chọn Kiến trúc gắn với Yêu cầu Phi chức năng (NFR)

Bốn câu lập luận dưới đây được xây dựng đúng theo khuôn mẫu bắt buộc của học phần:  
> *"Vì `<mã NFR>` yêu cầu `<ngưỡng cụ thể>`, tôi chọn `<quyết định kiến trúc>`, đánh đổi là `<chi phí/hạn chế>`."*

### Câu lập luận 1 – Gắn với NFR-01 (Hiệu năng tra cứu và giao dịch):
> **"Vì NFR-01 yêu cầu thời gian phản hồi API tra cứu tồn kho và lịch sử giao dịch $\le 500\text{ ms}$ cho 95% request với 10.000 bản ghi dữ liệu, tôi chọn đánh Index ghép `(center_id, part_id)` trên bảng `part_stock` và Index `(center_id, created_at)` trên bảng `part_transactions`, đồng thời áp dụng phân trang (Pagination) ở tầng CSDL thay vì tải toàn bộ dữ liệu lên bộ nhớ; đánh đổi là tăng thêm dung lượng lưu trữ index và phát sinh chi phí tính toán nhỏ khi ghi dữ liệu (INSERT/UPDATE)."**

### Câu lập luận 2 – Gắn với NFR-02 (Truy cập đồng thời & Chặn xuất âm kho):
> **"Vì NFR-02 yêu cầu số tồn kho không bị âm khi 50 yêu cầu xuất kho cùng lúc cho linh kiện có số tồn = 10 (đúng 10 thành công, 40 từ chối), tôi chọn cơ chế Khóa bi quan (Pessimistic Locking `SELECT ... FOR UPDATE`) tại `PartStockRepository` kết hợp giao dịch `@Transactional` ở Service layer thay vì Khóa lạc quan (Optimistic Locking); đánh đổi là thông lượng xử lý đồng thời (throughput) của các yêu cầu trên cùng một dòng linh kiện bị tuần tự hóa tạm thời trong vài phần nghìn giây của giao dịch."**

### Câu lập luận 3 – Gắn với NFR-03 (Bảo mật & Cô lập dữ liệu trung tâm bảo hành):
> **"Vì NFR-03 yêu cầu 100% API nghiệp vụ phải xác thực và ngăn chặn hoàn toàn việc kỹ thuật viên thao tác trên dữ liệu của trung tâm bảo hành khác, tôi chọn kiểm tra quyền hạn và áp đặt điều kiện lọc `center_id` bắt buộc tại tầng nghiệp vụ `StockService` (Defense in depth) thay vì chỉ ẩn hiện nút bấm trên giao diện người dùng; đánh đổi là mọi phương thức Service và đối tượng DTO truyền vào đều bắt buộc phải mang theo thông tin ngữ cảnh người dùng đã xác thực."**

### Câu lập luận 4 – Gắn với NFR-04 (Toàn vẹn dữ liệu & Lịch sử bất biến):
> **"Vì NFR-04 yêu cầu mọi giao dịch kho phải có người thực hiện, thời điểm và số lần xóa vật lý = 0, tôi chọn thiết kế bảng `part_transactions` theo mô hình ghi nối tiếp bất biến (Append-Only Log) và hoàn toàn không cung cấp API cập nhật (PUT) hay xóa vật lý (DELETE) cho giao dịch đã ghi; đánh đổi là dung lượng bảng giao dịch tăng tuyến tính theo thời gian và cần phân vùng dữ liệu (partitioning) khi hệ thống hoạt động lâu dài."**

---

## 4. Phân tích Đối sánh Hai phương án Kỹ thuật cho NFR-02

Để giải quyết bài toán cốt lõi của Luồng L5: **Chặn xuất quá tồn kho và không để tồn kho bị âm khi nhiều kỹ thuật viên cùng thao tác**, kiến trúc đã đối sánh hai phương án:

| Tiêu chí | Phương án A: Khóa lạc quan (Optimistic Locking) | Phương án B: Khóa bi quan (Pessimistic Locking) — [ĐƯỢC CHỌN] |
|---|---|---|
| **Cơ chế hoạt động** | Sử dụng cột `@Version`. Khi commit giao dịch, nếu số version đã bị thay đổi bởi request khác thì ném `OptimisticLockException`. | Sử dụng câu lệnh `SELECT ... FOR UPDATE`. Cơ sở dữ liệu khóa dòng dữ liệu của linh kiện đó cho đến khi transaction hoàn tất. |
| **Ưu điểm** | Không giữ khóa CSDL, hiệu năng đọc rất cao, phù hợp khi tần suất xung đột dữ liệu thấp. | **Đảm bảo tính chính xác 100%**, xử lý dứt điểm tình trạng tranh chấp, request đến sau đọc ngay số tồn mới nhất sau khi request trước đã trừ kho. |
| **Nhược điểm** | Khi 50 KTV cùng xuất 1 linh kiện tồn = 10, nhiều request hợp lệ sẽ bị văng lỗi version và buộc phải thử lại (retry) phức tạp. | Giao dịch giữ khóa bản ghi vài ms, có thể gây chờ đợi ngắn giữa các luồng cùng thao tác trên đúng 1 linh kiện đó. |
| **Đánh giá quyết định** | Không phù hợp với nghiệp vụ kho linh kiện có giá trị cao, nơi tính toàn vẹn (không âm kho) là ưu tiên sống còn. | **LỰA CHỌN CHÍNH THỨC:** Đảm bảo đúng 10 request đầu tiên trừ kho thành công, 40 request sau thấy tồn = 0 và bị từ chối dứt khoát theo đúng kịch bản kiểm chứng của NFR-02. |

---

## 5. Rà soát với 5 Tiêu chí Thiết kế Tốt của Buổi 5

1. **Rõ ràng (Clarity):** Sơ đồ thể hiện rõ 4 tầng riêng biệt, protocol trao đổi (HTTP REST/JSON, Method Call, SQL) và có chú thích ký hiệu (Legend) đọc được độc lập.
2. **Nhất quán (Consistency):** Tên các bảng ở Lớp Lưu trữ (`parts`, `part_stock`, `part_transactions`, `service_centers`, `tickets`) khớp 100% với Bảng thuật ngữ trong SRS và sơ đồ ERD.
3. **Tách trách nhiệm (Separation of Concerns):** Controller chỉ điều hướng HTTP; Service giữ toàn bộ business rules; Repository che giấu chi tiết SQL. Không có tầng nào "nhảy cóc".
4. **Có lập luận (Well-Justified):** Đủ 4 câu lập luận chuẩn mẫu gắn với NFR-01, NFR-02, NFR-03, NFR-04 và nêu rõ đánh đổi kỹ thuật.
5. **Mở rộng được (Extensibility):** Khi cần thêm loại giao dịch mới (ví dụ: xuất hủy linh kiện hỏng), chỉ cần thêm giá trị Enum và xử lý logic trong Service mà không phải sửa cấu trúc phân tầng.
