# Thành phần 5: Wireframe 3 Màn hình – Kho linh kiện thay thế (Luồng L5)

> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525 – Track SE  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế (Smart CRM Mekong Mobile)  
> **File thiết kế Figma:** Được thiết kế dựa trên mô tả chi tiết bên dưới  
> **Tiêu chí bắt buộc đã đáp ứng:** Nối về Use Case UC-01, UC-02, UC-03; mọi trường hiển thị tồn tại trong ERD; nhãn dùng đúng bảng thuật ngữ SRS.

---

## TÀI LIỆU MÔ TẢ WIREFRAME CHO FIGMA

### Bố cục chung toàn màn hình (Global Layout)
- **Kích thước màn hình:** Desktop 1440 x 900 px
- **Thanh điều hướng bên trái (Left Sidebar):** 220px, nền màu xanh đậm
  - Logo Mekong Mobile (trên cùng)
  - Menu items:
    - 📦 Tồn kho linh kiện *(màn hình M1 – trang hiện tại)*
    - ➕ Nhập / Xuất kho *(màn hình M2)*
    - 📋 Lịch sử giao dịch *(màn hình M3)*
  - Thông tin người dùng (avatar + tên + trung tâm bảo hành) ở cuối sidebar
- **Vùng nội dung chính (Main Content):** 1220px, nền màu xám nhạt (#F5F6FA)
- **Thanh tiêu đề trang (Page Header):** Breadcrumb + tên trang + nút hành động chính

---

## [M1] MÀN HÌNH DANH SÁCH TỒN KHO & CẢNH BÁO TỒN THẤP

**Nối Use Case:** UC-01 (Xem tồn kho), UC-05 (Cảnh báo tồn thấp)  
**Bảng dữ liệu nguồn:** `part_stock` JOIN `parts` JOIN `service_centers`  
**Người dùng:** Quản lý trung tâm, Kỹ thuật viên

---

### Bố cục màn hình M1 (1440 x 900)

```
+--Sidebar 220px--+-------------------------------------------Main Content 1220px-------------------------------------------+
|                 |  [Breadcrumb: Trang chủ > Tồn kho linh kiện]                                                          |
| 📦 Tồn kho      |                                                                                                        |
| ➕ Nhập/Xuất    |  TIÊU ĐỀ TRANG: "Tồn kho linh kiện"           [Nút chính: "+ Nhập kho mới"] [Nút phụ: "Xuất kho"]    |
| 📋 Lịch sử      |  Trung tâm đang xem: "BH Cần Thơ 1"                                                                  |
|                 |  -------------------------------------------------------------------------------------------             |
|  [Avatar]       |  [VÙNG THỐNG KÊ NHANH – 3 Card nằm ngang]                                                            |
|  Nguyễn Văn A   |  +----------------+  +------------------+  +-------------------+                                      |
|  BH Cần Thơ 1  |  | Tổng loại LK    |  | Loại dưới ngưỡng |  | Tổng GD hôm nay   |                                      |
|                 |  |   52 loại       |  |  🔴  4 loại cần  |  |   18 giao dịch    |                                      |
|                 |  |                 |  |  bổ sung gấp     |  |                   |                                      |
|                 |  +----------------+  +------------------+  +-------------------+                                      |
|                 |                                                                                                        |
|                 |  [VÙNG LỌC & TÌM KIẾM]                                                                               |
|                 |  [🔍 Tìm kiếm mã/tên linh kiện...]  [Phân loại: Tất cả ▼]  [☑ Chỉ hiện dưới ngưỡng]  [Làm mới]     |
|                 |                                                                                                        |
|                 |  [BẢNG DANH SÁCH TỒN KHO]                                                                            |
|                 |  +-------+-------------+-----------+--------+----------+----------+---------+----------+             |
|                 |  | STT   | Mã LK       | Tên LK    | Loại   | Số tồn   | Ngưỡng   | Trạng   | Hành động|             |
|                 |  |       |             |           |        | (cái)    | tối thiểu| thái    |          |             |
|                 |  +-------+-------------+-----------+--------+----------+----------+---------+----------+             |
|                 |  |  1    | PIN-IP15    | Pin IP 15 | PIN    |   15     |    5     |  ✅ Đủ  | [Chi tiết]|             |
|                 |  |  2    | LCD-IP15    | Màn hình..| MÀN..  |    4     |    5     |  🔴 Thấp| [Chi tiết]|             |
|                 |  |  3    | PIN-SS-S24  | Pin S24   | PIN    |    8     |    3     |  ✅ Đủ  | [Chi tiết]|             |
|                 |  |  4    | MH-OP-R5   | Màn hình..| MÀN..  |    2     |    3     |  🔴 Thấp| [Chi tiết]|             |
|                 |  |  5    | SAC-IP15    | Cổng sạc..| LK     |    6     |    5     |  ✅ Đủ  | [Chi tiết]|             |
|                 |  +-------+-------------+-----------+--------+----------+----------+---------+----------+             |
|                 |                                                                                                        |
|                 |  [Phân trang: Hiển thị 1–20 / 52 dòng]  [< Trước]  [1] [2] [3]  [Sau >]                             |
+--Sidebar 220px--+--------------------------------------------------------------------------------------------------------+
```

---

### Mô tả chi tiết từng vùng màn hình M1

#### A. Thanh tiêu đề trang
- **Tiêu đề:** `Tồn kho linh kiện` (cỡ chữ H1, đậm)
- **Chip thông tin:** `📍 Trung tâm: BH Cần Thơ 1` (hiển thị để xác nhận phạm vi dữ liệu – NFR-03)
- **Nút "+ Nhập kho mới":** Màu xanh dương (#1976D2), icon `+`, điều hướng sang M2
- **Nút "Xuất kho":** Màu cam (#F57C00), icon `↑`, điều hướng sang M2 (chế độ xuất)

#### B. Vùng 3 Card Thống kê nhanh
| Card | Nội dung hiển thị | Màu viền | Dữ liệu từ |
|---|---|---|---|
| Card 1 | `52` – Tổng số loại linh kiện đang quản lý | Xanh dương | `COUNT(*) FROM part_stock WHERE center_id = ?` |
| Card 2 | `4` – Loại linh kiện **dưới ngưỡng tối thiểu** (nền đỏ nhạt nếu > 0) | Đỏ (US-05) | `WHERE quantity < min_threshold AND center_id = ?` |
| Card 3 | `18` – Tổng giao dịch hôm nay | Xám xanh | `COUNT(*) FROM part_transactions WHERE DATE(created_at) = TODAY()` |

#### C. Vùng Lọc & Tìm kiếm
- **Ô tìm kiếm:** Placeholder `Tìm kiếm mã hoặc tên linh kiện...`, lọc realtime theo `part_code` và `part_name`
- **Dropdown Phân loại:** `Tất cả` | `PIN` | `MÀN HÌNH` | `CAMERA` | `MAINBOARD` | `LINH KIỆN KHÁC` (lấy từ cột `category` trong bảng `parts`)
- **Checkbox "Chỉ hiện dưới ngưỡng":** Khi bật, lọc chỉ hiển thị các dòng `quantity < min_threshold` (US-05, AC-5.2)
- **Nút "Làm mới":** Reset toàn bộ bộ lọc

#### D. Bảng Danh sách Tồn kho (data table)
| Cột | Dữ liệu hiển thị | Bảng nguồn & Cột | Định dạng |
|---|---|---|---|
| STT | Số thứ tự | – | Số nguyên |
| Mã LK | `part_code` | `parts.part_code` | Chữ in hoa, ví dụ `PIN-IP15` |
| Tên LK | `part_name` | `parts.part_name` | Văn bản, cắt bớt nếu dài |
| Loại | `category` | `parts.category` | Chip nhỏ có màu nền |
| Số tồn | `quantity` | `part_stock.quantity` | **In đậm đỏ** nếu < `min_threshold` |
| Ngưỡng tối thiểu | `min_threshold` | `part_stock.min_threshold` | Số xám nhạt |
| Trạng thái | Logic | `quantity >= min_threshold` | ✅ Chip xanh "Đủ hàng" / 🔴 Chip đỏ "Tồn thấp" |
| Hành động | Nút | – | `[Chi tiết]` – xem popup hoặc điều hướng |

#### E. Banner Cảnh báo Tồn thấp (hiển thị khi có ít nhất 1 dòng dưới ngưỡng)
- Hiển thị ngay phía trên bảng, nền vàng nhạt `#FFF9C4`, icon chuông `🔔`
- Nội dung: `4 loại linh kiện đang dưới ngưỡng tối thiểu – Cần bổ sung kho sớm`
- Nút hành động: `[Xem danh sách]` kích hoạt bộ lọc "Chỉ hiện dưới ngưỡng" (AC-5.1)

---
---

## [M2] MÀN HÌNH FORM NHẬP KHO / XUẤT KHO

**Nối Use Case:** UC-02 (Nhập kho), UC-03 (Xuất kho cho phiếu bảo hành)  
**Bảng dữ liệu nguồn:** `part_transactions`, `parts`, `part_stock`, `tickets`  
**Người dùng:** Quản lý trung tâm (nhập kho), Kỹ thuật viên (xuất kho)

---

### Bố cục màn hình M2 (1440 x 900)

```
+--Sidebar 220px--+-------------------------------------------Main Content 1220px-------------------------------------------+
|                 |  [Breadcrumb: Trang chủ > Nhập / Xuất kho]                                                            |
| 📦 Tồn kho      |                                                                                                        |
| ➕ Nhập/Xuất ◀  |  TIÊU ĐỀ TRANG: "Tạo giao dịch kho"                             [Nút: "← Quay lại Tồn kho"]          |
| 📋 Lịch sử      |  -------------------------------------------------------------------------------------------             |
|                 |                                                                                                        |
|  [Avatar]       |  [TAB CHUYỂN ĐỔI]                                                                                    |
|  Nguyễn Văn A   |  [  📥 NHẬP KHO  ]   [  📤 XUẤT KHO  ]                                                              |
|  BH Cần Thơ 1  |  (Tab NHẬP KHO đang được chọn – viền xanh dương bên dưới)                                            |
|                 |  -------------------------------------------------------------------------------------------             |
|                 |                                                                                                        |
|                 |  [PHẦN A: THÔNG TIN GIAO DỊCH NHẬP KHO]                                                              |
|                 |  +-----------------------------------------------------------------------------------------+           |
|                 |  | Trường bắt buộc có dấu (*) đỏ                                                          |           |
|                 |  |                                                                                         |           |
|                 |  |  Mã linh kiện (*):  [🔍 Nhập mã hoặc tên để tìm kiếm linh kiện... ▼]                  |           |
|                 |  |  (Khi chọn xong, hiện thông tin xem trước bên dưới ô)                                  |           |
|                 |  |                                                                                         |           |
|                 |  |  [THÔNG TIN LINH KIỆN ĐÃ CHỌN – hiện sau khi tìm kiếm xong]                           |           |
|                 |  |  +------------------+------------------+------------------+                            |           |
|                 |  |  | Mã: PIN-IP15     | Tên: Pin iPhone  | Loại: PIN        |                            |           |
|                 |  |  | Tồn hiện tại: 15 | Đơn giá: 850.000đ| Đơn vị: Cái     |                            |           |
|                 |  |  +------------------+------------------+------------------+                            |           |
|                 |  |                                                                                         |           |
|                 |  |  Số lượng nhập (*): [__________ Cái]   (Nhập số nguyên dương > 0 – QT-L5-01)         |           |
|                 |  |                                                                                         |           |
|                 |  |  Người thực hiện: [Nguyễn Văn A] (Tự điền từ tài khoản đăng nhập – NFR-04)           |           |
|                 |  |                                                                                         |           |
|                 |  |  Ghi chú:          [____________________________________________]                       |           |
|                 |  |                    Ví dụ: Nhập kho từ NPP tháng 10/2026                                |           |
|                 |  |                                                                                         |           |
|                 |  |  [THÔNG TIN SAU KHI NHẬP – xem trước kết quả]                                         |           |
|                 |  |  Số tồn sau khi nhập: 15 + [số lượng nhập] = [Hiển thị động]                          |           |
|                 |  |                                                                                         |           |
|                 |  |                       [Nút HỦY – màu xám]  [Nút XÁC NHẬN NHẬP KHO – màu xanh lá]     |           |
|                 |  +-----------------------------------------------------------------------------------------+           |
|                 |                                                                                                        |
|                 |  [PHẦN B: (TAB XUẤT KHO – hiển thị khi click tab "Xuất kho")]                                       |
|                 |  (xem mô tả chi tiết phần Tab Xuất kho bên dưới)                                                     |
+--Sidebar 220px--+--------------------------------------------------------------------------------------------------------+
```

---

### Mô tả chi tiết – Tab NHẬP KHO

#### A. Tab chuyển đổi NHẬP / XUẤT
- **Tab "📥 NHẬP KHO":** Dành cho Quản lý kho. Không có trường "Phiếu bảo hành".
- **Tab "📤 XUẤT KHO":** Dành cho Kỹ thuật viên. Bắt buộc chọn Phiếu bảo hành.
- Tab hiện tại highlight viền xanh dương bên dưới, nền trắng; tab kia nền xám nhạt.

#### B. Ô tìm kiếm linh kiện (Mã linh kiện)
- **Loại:** Autocomplete dropdown, gõ ít nhất 2 ký tự để tìm kiếm
- **Nguồn dữ liệu:** `SELECT part_code, part_name, category FROM parts WHERE is_active = TRUE`
- **Gợi ý hiển thị trong dropdown:** `[PIN-IP15] – Pin zin Apple iPhone 15`
- **Sau khi chọn:** Tự động hiển thị khung "Thông tin linh kiện đã chọn" gồm `part_code`, `part_name`, `category`, `quantity` (tồn hiện tại), `unit_price`, `unit`

#### C. Ô Số lượng nhập
- **Loại:** Input number, chỉ nhận số nguyên dương
- **Validation phía giao diện:** Nếu nhập ≤ 0 thì hiện text đỏ `"Số lượng phải lớn hơn 0"` (QT-L5-01)
- **Preview động:** Khi nhập số lượng, dòng "Số tồn sau khi nhập" tự cập nhật hiển thị = `tồn hiện tại + số lượng nhập`

#### D. Nút XÁC NHẬN NHẬP KHO
- **Màu:** Xanh lá (#388E3C), icon `✓`
- **Khi bấm:** Gửi yêu cầu lên `PartTransactionService`, hiển thị modal thành công:  
  `"✅ Nhập kho thành công! Đã nhập 20 cái PIN-IP15. Tồn mới: 35 cái."`
- **Lỗi (nếu có):** Modal đỏ `"❌ Lỗi: Mã linh kiện không tồn tại trong danh mục"` (AC-2.3)

---

### Mô tả chi tiết – Tab XUẤT KHO

```
  [PHẦN B: FORM XUẤT KHO – hiển thị khi bấm Tab "Xuất kho"]
  +-----------------------------------------------------------------------------------------+
  |  ⚠️ Lưu ý: Chỉ xuất kho được khi phiếu bảo hành đang ở trạng thái                     |
  |  "Đang xử lý" hoặc "Chờ linh kiện" và thuộc trung tâm bảo hành của bạn (QT-06)        |
  |                                                                                         |
  |  Mã Phiếu bảo hành (*):  [🔍 Nhập mã phiếu: BH-000456/2026... ▼]                     |
  |  (Chỉ hiện các phiếu thuộc trung tâm hiện tại và đang ở trạng thái hợp lệ)            |
  |                                                                                         |
  |  [THÔNG TIN PHIẾU BẢO HÀNH ĐÃ CHỌN]                                                   |
  |  +---------------------------+---------------------------+---------------------------+  |
  |  | Mã phiếu: BH-000456/2026 | Khách hàng: Nguyễn Văn An | Model: Apple iPhone 15    |  |
  |  | Trạng thái: Đang xử lý   | Trung tâm: BH Cần Thơ 1   | Ngày lập: 07/10/2026     |  |
  |  +---------------------------+---------------------------+---------------------------+  |
  |                                                                                         |
  |  Mã linh kiện (*):  [🔍 Nhập mã hoặc tên để tìm kiếm linh kiện... ▼]                  |
  |  [THÔNG TIN LINH KIỆN + TỒN KHO HIỆN TẠI]                                             |
  |  +------------------+------------------+------------------+                            |
  |  | Mã: PIN-IP15     | Tên: Pin iPhone  | Tồn kho: 15 cái  |                            |
  |  +------------------+------------------+------------------+                            |
  |                                                                                         |
  |  Số lượng xuất (*): [__________ Cái]   (Không được vượt quá 15 cái – QT-09)           |
  |  [Thanh progress hiển thị: ██████░░░░ 10/15 cái đã nhập]                              |
  |                                                                                         |
  |  Người thực hiện: [Nguyễn Văn A] (Tự điền – NFR-04)                                  |
  |  Ghi chú: [____________________________________________]                               |
  |                                                                                         |
  |  Số tồn sau khi xuất: 15 – [số lượng xuất] = [Hiển thị động]                         |
  |  (Nền đỏ nhạt và cảnh báo nếu kết quả < min_threshold)                                |
  |                                                                                         |
  |                    [Nút HỦY – màu xám]  [Nút XÁC NHẬN XUẤT KHO – màu cam]           |
  +-----------------------------------------------------------------------------------------+
```

**Validation đặc biệt của Tab Xuất kho:**
- Nếu `số lượng xuất > tồn hiện tại`: hiện text đỏ `"❌ Số lượng xuất (X) vượt quá số tồn hiện tại (Y). Giao dịch sẽ bị từ chối."` (AC-4.1, NFR-02)
- Nếu chọn phiếu bảo hành ở trạng thái "Đã đóng": hiện lỗi `"❌ Không thể xuất linh kiện cho phiếu bảo hành đã đóng"` (AC-3.2)
- Kết quả sau xuất dưới ngưỡng: hiện cảnh báo vàng `"⚠️ Sau giao dịch này, số tồn PIN-IP15 sẽ xuống dưới ngưỡng tối thiểu (5 cái)"` (AC-5.1)

---
---

## [M3] MÀN HÌNH LỊCH SỬ GIAO DỊCH KHO

**Nối Use Case:** UC-06 (Xem lịch sử theo linh kiện), UC-07 (Xem lịch sử theo phiếu BH)  
**Bảng dữ liệu nguồn:** `part_transactions` JOIN `parts` JOIN `tickets` JOIN `service_centers`  
**Người dùng:** Quản lý trung tâm

---

### Bố cục màn hình M3 (1440 x 900)

```
+--Sidebar 220px--+-------------------------------------------Main Content 1220px-------------------------------------------+
|                 |  [Breadcrumb: Trang chủ > Lịch sử giao dịch]                                                          |
| 📦 Tồn kho      |                                                                                                        |
| ➕ Nhập/Xuất    |  TIÊU ĐỀ TRANG: "Lịch sử giao dịch kho"                        [Nút: "📥 Xuất Excel"]                |
| 📋 Lịch sử ◀   |  Trung tâm: "BH Cần Thơ 1"  |  Tổng GD trong kỳ: 127 giao dịch                                      |
|                 |  -------------------------------------------------------------------------------------------             |
|  [Avatar]       |                                                                                                        |
|  Nguyễn Văn A   |  [VÙNG LỌC – 1 hàng, 4 bộ lọc]                                                                      |
|  BH Cần Thơ 1  |  [📅 Từ ngày: 01/10/2026]  [📅 Đến ngày: 08/10/2026]  [Loại GD: Tất cả ▼]  [Mã/Tên LK: Tìm kiếm]  |
|                 |  [Mã Phiếu BH: ___________]                    [Nút: 🔍 Lọc]   [Nút: Xóa lọc]                       |
|                 |                                                                                                        |
|                 |  [BẢNG LỊCH SỬ GIAO DỊCH]                                                                            |
|                 |  +--------+-------------+-------------+-----------+----------+----------+----------+-----------+      |
|                 |  | STT    | Mã GD       | Ngày giờ    | Loại GD   | Mã LK    | Tên LK   | Số lượng | Mã Phiếu  |      |
|                 |  |        |             |             |           |          |          | (Cái)    | BH        |      |
|                 |  +--------+-------------+-------------+-----------+----------+----------+----------+-----------+      |
|                 |  |  1     |GD-XK-001    |08/10 09:45  |📤 Xuất kho| PIN-IP15 | Pin IP15 |   5      |BH-000456  |      |
|                 |  |  2     |GD-NK-001    |08/10 08:30  |📥 Nhập kho| PIN-IP15 | Pin IP15 |  20      |     –     |      |
|                 |  |  3     |GD-XK-002    |07/10 14:20  |📤 Xuất kho| LCD-IP15 | Màn hình |   1      |BH-000457  |      |
|                 |  |  4     |GD-NK-002    |06/10 10:00  |📥 Nhập kho| MH-OP-R5 | Màn hình |   5      |     –     |      |
|                 |  |  5     |GD-XK-003    |05/10 16:15  |📤 Xuất kho| MH-OP-R5 | Màn hình |   3      |BH-000789  |      |
|                 |  +--------+-------------+-------------+-----------+----------+----------+----------+-----------+      |
|                 |                                                                                                        |
|                 |  [Phân trang: 1–20 / 127 giao dịch]  [< Trước]  [1] [2] [3] ... [7]  [Sau >]                         |
+--Sidebar 220px--+--------------------------------------------------------------------------------------------------------+
```

---

### Mô tả chi tiết từng vùng màn hình M3

#### A. Vùng Lọc & Tìm kiếm nâng cao
| Bộ lọc | Loại control | Giá trị mặc định | Cột CSDL tương ứng |
|---|---|---|---|
| Từ ngày | Date picker | Đầu tháng hiện tại | `part_transactions.created_at >= ?` |
| Đến ngày | Date picker | Hôm nay | `part_transactions.created_at <= ?` |
| Loại giao dịch | Dropdown 3 giá trị | `Tất cả` | `transaction_type IN ('NHAP', 'XUAT')` |
| Mã/Tên linh kiện | Text input | Rỗng | `part_code LIKE ?` hoặc `part_name LIKE ?` |
| Mã phiếu BH | Text input | Rỗng | `ticket_code LIKE ?` (tìm theo US-07) |

- **Nút "Lọc":** Áp dụng toàn bộ bộ lọc, load lại dữ liệu bảng
- **Nút "Xóa lọc":** Reset về khoảng 30 ngày gần nhất, loại = Tất cả

**Khi không có kết quả:** Hiển thị ảnh minh họa trống và dòng chữ `"Không có giao dịch nào trong khoảng thời gian này"` (AC-6.2)

#### B. Bảng Lịch sử giao dịch
| Cột | Dữ liệu hiển thị | Bảng nguồn & Cột | Định dạng |
|---|---|---|---|
| STT | Số thứ tự theo trang | – | Số |
| Mã GD | `transaction_code` | `part_transactions.transaction_code` | Chữ đậm: `GD-NK-202610-001` |
| Ngày giờ | `created_at` | `part_transactions.created_at` | `dd/MM HH:mm`, tooltip hiển thị đầy đủ giây |
| Loại GD | `transaction_type` | `part_transactions.transaction_type` | Chip: 📤 `Xuất kho` đỏ / 📥 `Nhập kho` xanh |
| Mã LK | `part_code` | `parts.part_code` | Chữ đơn giản |
| Tên LK | `part_name` | `parts.part_name` | Cắt ngắn 20 ký tự |
| Số lượng | `quantity` | `part_transactions.quantity` | Số, `+20` (nhập) màu xanh / `-5` (xuất) màu đỏ |
| Mã Phiếu BH | `ticket_code` | `tickets.ticket_code` | Link bấm được, `–` nếu là giao dịch nhập kho |
| Người thực hiện | `performed_by` | `part_transactions.performed_by` | Tên rút gọn |

#### C. Hàng Tổng kết cuối bảng (trước phân trang)
- `Tổng nhập kho trong kỳ: +127 cái` (màu xanh)
- `Tổng xuất kho trong kỳ: -89 cái` (màu đỏ)
- `Chênh lệch ròng: +38 cái`

#### D. Chức năng Xuất Excel
- **Nút "📥 Xuất Excel":** Xuất toàn bộ kết quả lọc hiện tại (không chỉ trang đang xem) ra file `.xlsx`
- **Tên file tự sinh:** `LichSuGiaoDich_BHCanTho1_20261001_20261008.xlsx`

---
---

## TỔNG HỢP MỐI NỐI GIỮA 3 MÀN HÌNH VÀ ERD

```
M1 – Danh sách tồn kho
  [Nút "+ Nhập kho mới"]  ──────────────────→  M2 – Tab NHẬP KHO
  [Nút "Xuất kho"]        ──────────────────→  M2 – Tab XUẤT KHO
  Bảng: part_stock JOIN parts JOIN service_centers

M2 – Form Nhập / Xuất kho
  [Sau khi GD thành công] ──────────────────→  M1 – Làm mới số tồn kho
  [Sau khi GD thành công] ──────────────────→  M3 – Xuất hiện dòng GD mới ở đầu danh sách
  Bảng ghi vào: part_transactions (Append-Only) + UPDATE part_stock

M3 – Lịch sử giao dịch
  [Bấm Mã Phiếu BH]      ──────────────────→  Mở popup/trang chi tiết phiếu bảo hành
  [Bấm Mã LK]            ──────────────────→  Điều hướng về M1, filter theo linh kiện đó
  Bảng: part_transactions JOIN parts JOIN tickets JOIN service_centers
```

---

## BẢNG KIỂM TRA TIÊU CHÍ BT1 – THÀNH PHẦN 5

| Tiêu chí yêu cầu | Kiểm tra | Bằng chứng cụ thể |
|---|:---:|---|
| Nối về ít nhất 1 use case | ✅ | M1 → UC-01, UC-05; M2 → UC-02, UC-03; M3 → UC-06, UC-07 |
| Trường hiển thị tồn tại trong mô hình dữ liệu | ✅ | `quantity`, `min_threshold` từ `part_stock`; `part_code` từ `parts`; `ticket_code` từ `tickets`; `transaction_type`, `created_at` từ `part_transactions` |
| Nhãn dùng đúng bảng thuật ngữ SRS | ✅ | "Kho linh kiện", "Phiếu bảo hành", "Trung tâm bảo hành", "Giao dịch nhập/xuất kho" – đúng bảng thuật ngữ mục 1 SRS |
| Có đủ 3 màn hình SE (Danh sách, Tạo/Sửa, Chi tiết/Lịch sử) | ✅ | M1 = Danh sách tồn kho; M2 = Form tạo giao dịch; M3 = Lịch sử giao dịch (Chi tiết) |
