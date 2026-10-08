# Thành phần 2: Use Case – Kho linh kiện thay thế (L5)

> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525 – Track SE  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế (Smart CRM Mekong Mobile)  
> **File sơ đồ gốc:** [`docs/usecase.drawio`](usecase.drawio) — vẽ bằng draw.io, lưu file XML gốc

---

## 1. Use Case Diagram

### 1.1. Ranh giới hệ thống (System Boundary)

**Hệ thống Kho linh kiện thay thế (L5) – Smart CRM Mekong Mobile**

### 1.2. Các Actor

| # | Actor | Loại | Vai trò trong luồng L5 |
|:---:|---|:---:|---|
| 1 | **Kỹ thuật viên** | Người dùng | Tra cứu tồn kho linh kiện tại trung tâm mình; xuất linh kiện cho phiếu bảo hành đang xử lý. |
| 2 | **Quản lý trung tâm** | Người dùng | Xem tồn kho; ghi nhận nhập kho; xem cảnh báo tồn thấp; xem lịch sử giao dịch linh kiện; thiết lập ngưỡng tối thiểu. |

*(Lưu ý: Dữ liệu phiếu bảo hành được kế thừa từ luồng L2 dưới dạng dữ liệu mẫu theo ràng buộc RB-03, L5 chỉ đọc dữ liệu và không can thiệp vòng đời phiếu).*

### 1.3. Danh sách 7 Use Case

Tên gọi Use Case khớp chính xác với bảng truy vết yêu cầu (Bảng 6) trong tài liệu SRS:

| Mã UC | Tên Use Case | Actor tương tác | FR | User Story | MoSCoW |
|:---:|---|---|:---:|:---:|:---:|
| **UC-01** | Xem tồn kho linh kiện | Kỹ thuật viên, Quản lý trung tâm | FR-01 | US-01 | MUST |
| **UC-02** | Ghi nhận nhập kho | Quản lý trung tâm | FR-02 | US-02 | MUST |
| **UC-03** | Xuất linh kiện cho phiếu bảo hành | Kỹ thuật viên | FR-03 | US-03 | MUST |
| **UC-04** | Kiểm tra số tồn trước khi xuất | *(gọi tự động bởi UC-03)* | FR-04 | US-04 | MUST |
| **UC-05** | Xem cảnh báo tồn thấp | Quản lý trung tâm | FR-05 | US-05 | SHOULD |
| **UC-06** | Xem lịch sử giao dịch linh kiện | Quản lý trung tâm | FR-06 | US-06, US-07 | SHOULD |
| **UC-07** | Thiết lập ngưỡng tối thiểu | Quản lý trung tâm | FR-07 | US-08 | COULD |

### 1.4. Quan hệ giữa các Use Case

| Quan hệ | Nguồn → Đích | Giải thích |
|:---:|---|---|
| **«include»** | UC-03 → UC-04 | Mỗi lần xuất linh kiện, hệ thống **luôn luôn** gọi UC-04 để kiểm tra số tồn trước khi cho phép xuất (QT-09, QT-L5-03). |

### 1.5. Chú thích ký hiệu (Legend)

Bảng chú thích này khớp với legend trong file [`usecase.drawio`](usecase.drawio):

| Ký hiệu | Ý nghĩa |
|---|---|
| Hình người (stick figure) | **Actor** là người dùng — nằm **NGOÀI** ranh giới hệ thống |
| Hình elip (oval) | **Use Case** — mục tiêu của actor, đặt tên bằng **động từ + đối tượng** |
| Đường liền nét (——) | **Association** — quan hệ tương tác giữa actor và use case |
| Mũi tên nét đứt (- - ->) với nhãn «include» | **«include»** — use case nguồn luôn gọi use case đích |
| Hình chữ nhật viền đậm bao quanh các use case | **Ranh giới hệ thống L5** — phân định phạm vi chức năng |

---

## 2. Đặc tả chi tiết 2 Use Case quan trọng nhất

### 2.1. UC-03 – Xuất linh kiện cho phiếu bảo hành

| Mục | Nội dung |
|---|---|
| **Mã UC** | UC-03 |
| **Tên Use Case** | Xuất linh kiện cho phiếu bảo hành |
| **Actor** | Kỹ thuật viên |
| **Mục tiêu** | Ghi nhận xuất linh kiện từ kho trung tâm, gắn với phiếu bảo hành đang xử lý, giảm số tồn kho tức thời và lưu vết giao dịch. |
| **User Story liên quan** | **US-03** – Xuất linh kiện cho phiếu bảo hành *(MUST)* |
| **Quan hệ «include»** | Bao gồm **UC-04 – Kiểm tra số tồn trước khi xuất** (US-04, QT-09, QT-L5-03, NFR-02) |
| **Quy tắc nghiệp vụ** | QT-06 (Vòng đời phiếu), QT-09 (Chặn xuất quá tồn), QT-13 (Không xóa vật lý), QT-14 (Cô lập trung tâm), QT-L5-02 (Ràng buộc xuất kho), QT-L5-03 (Số tồn không âm) |
| **Điều kiện trước** | 1) Kỹ thuật viên đã đăng nhập hệ thống, thuộc một trung tâm bảo hành cụ thể.<br>2) Phiếu bảo hành tồn tại, thuộc cùng trung tâm và đang ở trạng thái **Đang xử lý** hoặc **Chờ linh kiện** (dữ liệu mẫu từ L2 theo RB-03).<br>3) Linh kiện cần xuất tồn tại trong danh mục hệ thống. |
| **Điều kiện sau (thành công)** | Tồn kho giảm đúng bằng số lượng xuất. Một giao dịch xuất kho bất biến được lưu gắn mã phiếu bảo hành, người thực hiện, thời điểm. Nếu tồn xuống dưới ngưỡng tối thiểu → cảnh báo tồn thấp được kích hoạt. |
| **Điều kiện sau (thất bại)** | Tồn kho không đổi. Không có giao dịch nào được lưu. Hệ thống hiển thị thông báo lỗi cụ thể. |

**Luồng chính (Main Flow):**

1. Kỹ thuật viên chọn phiếu bảo hành cần lấy linh kiện từ danh sách phiếu của trung tâm mình.
2. Hệ thống kiểm tra thông tin phiếu bảo hành (dữ liệu từ L2 theo RB-03) và xác nhận phiếu đang ở trạng thái hợp lệ (**Đang xử lý** hoặc **Chờ linh kiện**) và thuộc cùng trung tâm.
3. Kỹ thuật viên chọn mã linh kiện từ danh mục và nhập số lượng cần xuất (số nguyên > 0).
4. Kỹ thuật viên bấm nút **"Xác nhận xuất linh kiện"**.
5. Hệ thống thực thi **UC-04 – Kiểm tra số tồn trước khi xuất** (`«include»`): đối chiếu số lượng yêu cầu xuất với số tồn hiện có tại trung tâm.
6. Hệ thống trừ số tồn kho linh kiện đúng bằng số lượng xuất (áp dụng khóa đồng thời theo NFR-02 để chặn tồn âm).
7. Hệ thống tạo một bản ghi giao dịch xuất kho gồm: mã linh kiện, số lượng, loại giao dịch (xuất kho), mã phiếu bảo hành, người thực hiện, thời điểm ghi nhận.
8. Hệ thống kiểm tra nếu tồn kho sau xuất < ngưỡng tối thiểu → kích hoạt cảnh báo tồn thấp.
9. Hệ thống hiển thị thông báo: *"Xuất linh kiện thành công cho phiếu [Mã phiếu]"* và cập nhật số tồn mới trên màn hình.

**Luồng ngoại lệ (Exception Flows):**

- **2a. Phiếu bảo hành không hợp lệ:**
  - 2a.1. Hệ thống phát hiện phiếu đang ở trạng thái *Hoàn tất*, *Đã đóng* hoặc *Đã hủy*, hoặc phiếu thuộc trung tâm khác (QT-06, QT-14).
  - 2a.2. Hệ thống hiển thị thông báo: *"Không thể xuất linh kiện: phiếu bảo hành không ở trạng thái hợp lệ hoặc không thuộc trung tâm của bạn."*
  - 2a.3. Use case kết thúc thất bại. Tồn kho không đổi.

- **3a. Số lượng xuất không hợp lệ:**
  - 3a.1. Kỹ thuật viên nhập số lượng ≤ 0 hoặc không phải số nguyên (QT-L5-02).
  - 3a.2. Hệ thống hiển thị lỗi: *"Số lượng linh kiện xuất phải là số nguyên lớn hơn 0."*
  - 3a.3. Kỹ thuật viên quay lại **Bước 3** để nhập lại.

- **5a. Tồn kho không đủ (UC-04 trả về thất bại):**
  - 5a.1. Hệ thống phát hiện số lượng xuất > số tồn hiện có (hoặc tranh chấp đồng thời làm hết tồn) (QT-09, QT-L5-03, NFR-02).
  - 5a.2. Hệ thống hiển thị: *"Số lượng xuất ([Yêu cầu]) vượt quá tồn kho hiện tại ([Tồn thực tế]). Giao dịch bị hủy."*
  - 5a.3. Tồn kho giữ nguyên. Không có giao dịch được lưu. Use case kết thúc thất bại.

---

### 2.2. UC-02 – Ghi nhận nhập kho

| Mục | Nội dung |
|---|---|
| **Mã UC** | UC-02 |
| **Tên Use Case** | Ghi nhận nhập kho |
| **Actor chính** | Quản lý trung tâm |
| **Mục tiêu** | Ghi nhận linh kiện mới nhập về trung tâm, cập nhật tức thời số tồn kho để kỹ thuật viên có hàng sửa chữa, và lưu vết giao dịch nhập kho. |
| **User Story liên quan** | **US-02** – Ghi nhận nhập kho *(MUST)* |
| **Quy tắc nghiệp vụ** | QT-13 (Không xóa vật lý), QT-14 (Cô lập trung tâm), QT-L5-01 (Ràng buộc nhập kho), NFR-04 (Truy vết và toàn vẹn dữ liệu) |
| **Điều kiện trước** | 1) Quản lý trung tâm đã đăng nhập hệ thống với vai trò Quản lý.<br>2) Mã linh kiện cần nhập đã tồn tại trong danh mục linh kiện. |
| **Điều kiện sau (thành công)** | Tồn kho tăng đúng bằng số lượng nhập. Một giao dịch nhập kho bất biến được lưu (người thực hiện, thời điểm, không được sửa/xóa). Nếu trước đó linh kiện đang cảnh báo tồn thấp và tồn mới ≥ ngưỡng tối thiểu → cảnh báo tự động tắt. |
| **Điều kiện sau (thất bại)** | Tồn kho không đổi. Không có giao dịch nào được lưu. Hệ thống hiển thị thông báo lỗi cụ thể. |

**Luồng chính (Main Flow):**

1. Quản lý trung tâm mở màn hình **"Ghi nhận nhập kho"**.
2. Quản lý chọn mã linh kiện từ danh mục, nhập số lượng cần nhập (số nguyên > 0) và ghi chú (nếu có).
3. Quản lý bấm nút **"Lưu nhập kho"**.
4. Hệ thống xác thực dữ liệu: mã linh kiện tồn tại trong danh mục, số lượng nhập là số nguyên > 0 (QT-L5-01).
5. Hệ thống cập nhật tăng số lượng tồn kho của linh kiện tại đúng trung tâm của quản lý.
6. Hệ thống tạo một bản ghi giao dịch nhập kho gồm: mã linh kiện, số lượng, loại giao dịch (nhập kho), mã trung tâm, người thực hiện, thời điểm ghi nhận (QT-13, NFR-04).
7. Hệ thống đối chiếu tồn mới với ngưỡng tối thiểu; nếu tồn mới ≥ ngưỡng → tự động tắt cảnh báo tồn thấp (nếu đang bật).
8. Hệ thống hiển thị thông báo: *"Ghi nhận nhập kho thành công cho linh kiện [Tên linh kiện]"* và cập nhật số tồn mới trên giao diện.

**Luồng ngoại lệ (Exception Flows):**

- **4a. Số lượng nhập không hợp lệ:**
  - 4a.1. Hệ thống phát hiện số lượng nhập ≤ 0 hoặc không phải số nguyên (QT-L5-01).
  - 4a.2. Hệ thống hiển thị lỗi: *"Số lượng nhập phải là số nguyên lớn hơn 0."*
  - 4a.3. Quản lý quay lại **Bước 2** để nhập lại.

- **4b. Mã linh kiện không tồn tại:**
  - 4b.1. Hệ thống không tìm thấy mã linh kiện trong danh mục.
  - 4b.2. Hệ thống hiển thị lỗi: *"Mã linh kiện không tồn tại trong danh mục hệ thống."*
  - 4b.3. Quản lý kiểm tra lại mã hoặc hủy bỏ thao tác.
