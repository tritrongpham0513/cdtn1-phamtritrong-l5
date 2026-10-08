# Đặc tả Yêu cầu Phần mềm (SRS) – Rút gọn

> **Dự án:** Smart CRM – Mekong Mobile  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế  
> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525  
> **Track:** Software Engineering (SE)  
> **Phiên bản:** 1.1 – Học kỳ 1, Năm học 2026 – 2027  
> **Tham chiếu:** Bản SRS rút gọn theo tinh thần chuẩn ISO/IEC/IEEE 29148

---

## Mục lục

1. [Giới thiệu và Phạm vi](#1-giới-thiệu-và-phạm-vi)  
2. [Các bên liên quan](#2-các-bên-liên-quan-và-vai-trò)  
3. [Yêu cầu chức năng (FR) và User Story](#3-yêu-cầu-chức-năng-fr-và-user-story)  
4. [Yêu cầu phi chức năng (NFR)](#4-yêu-cầu-phi-chức-năng-nfr)  
5. [Ràng buộc và quy tắc nghiệp vụ](#5-ràng-buộc-và-quy-tắc-nghiệp-vụ)  
6. [Bảng truy vết yêu cầu](#6-bảng-truy-vết-yêu-cầu-traceability-matrix)  

---

## 1. Giới thiệu và Phạm vi

### 1.1. Bối cảnh

Công ty Cổ phần Bán lẻ & Dịch vụ **Mekong Mobile** là chuỗi bán lẻ và sửa chữa thiết bị di động, hiện vận hành **24 cửa hàng** và **6 trung tâm bảo hành** với khoảng 38 kỹ thuật viên phụ trách sửa chữa, trung bình tiếp nhận **260 phiếu bảo hành mỗi tháng**.

Hiện tại, tồn kho linh kiện thay thế tại các trung tâm bảo hành được ghi chép **thủ công bằng sổ giấy**, chỉ cập nhật vào cuối ngày. Thực trạng này gây ra các vấn đề trực tiếp:

- Số tồn trong sổ **lệch 3–8%** so với thực tế trong kho.
- Kỹ thuật viên nhận phiếu bảo hành xong **mới phát hiện hết linh kiện**, buộc phải hẹn lại khách — xảy ra khoảng **20 lần/tháng**, ảnh hưởng trực tiếp đến uy tín dịch vụ.
- Quản lý trung tâm bảo hành **không nhận được cảnh báo tồn thấp** sớm khi linh kiện sắp cạn, dẫn đến thiếu hàng đột ngột.

Module **L5 – Kho linh kiện thay thế** được xây dựng nhằm số hóa toàn bộ quy trình nhập kho – xuất kho, đảm bảo số tồn luôn chính xác tức thời và giảm thiểu tình trạng hẹn lại khách do thiếu linh kiện.

### 1.2. Luồng nghiệp vụ chọn và Phạm vi

> **Quản lý kho linh kiện thay thế: quản lý trung tâm ghi nhập kho, kỹ thuật viên xuất linh kiện cho phiếu bảo hành, hệ thống chặn xuất quá tồn, tự cập nhật số tồn và cảnh báo khi tồn dưới ngưỡng tối thiểu, đến khi mọi lần nhập – xuất được lưu vào lịch sử.**

### 1.3. Điều chủ ý KHÔNG làm (Mức WON'T của MoSCoW)

Để bảo đảm ranh giới của một đồ án cá nhân (prototype 1–2 module cốt lõi chạy cục bộ):

1. **Không** xây dựng module tự động đặt hàng nhà cung cấp (Procurement / Purchasing System).
2. **Không** điều chuyển linh kiện giữa các trung tâm bảo hành (Inter-center transfer).
3. **Không** áp dụng mô hình AI/ML dự báo nhu cầu linh kiện theo chuỗi thời gian (thuộc học phần CĐTN2).
4. **Không** tích hợp cổng thanh toán trực tuyến tiền linh kiện cho khách hàng.

### 1.4. Bảng thuật ngữ (Glossary)

> Mỗi khái niệm chỉ dùng **một tên** trong toàn bộ SRS, Use Case, kiến trúc, ERD, wireframe và API contract.

| Thuật ngữ | Định nghĩa | Tên kỹ thuật |
|---|---|---|
| **Phiếu bảo hành** | Một yêu cầu bảo hành hoặc sửa chữa được ghi nhận, có mã duy nhất và vòng đời trạng thái | `ticket` |
| **Mã phiếu bảo hành** | Mã hiển thị của phiếu bảo hành, dạng `BH-000123/2026` | `ticket_code` |
| **Trạng thái phiếu** | Vị trí hiện tại của phiếu bảo hành trong vòng đời: Mới → Đã phân công → Đang xử lý → Chờ linh kiện → Hoàn tất → Đã đóng | `status` |
| **Kỹ thuật viên** | Nhân viên thực hiện sửa chữa, có danh sách tay nghề và địa bàn làm việc | `technician` |
| **Quản lý trung tâm** | Nhân viên phụ trách vận hành một trung tâm bảo hành, có quyền nhập kho và cấu hình ngưỡng tối thiểu | `center_manager` |
| **Trung tâm bảo hành** | Cơ sở nơi thực hiện bảo hành (Mekong Mobile có 6 trung tâm bảo hành) | `service_center` |
| **Linh kiện** | Bộ phận thay thế dùng trong sửa chữa, thuộc danh mục dùng chung toàn công ty | `part` |
| **Mã linh kiện** | Mã hiển thị của linh kiện, ví dụ `LCD-IP15` | `part_code` |
| **Tồn kho linh kiện** | Bản ghi theo dõi một linh kiện tại một trung tâm bảo hành, gồm số tồn và ngưỡng tối thiểu | `part_stock` |
| **Số tồn** | Số lượng hiện có của một linh kiện tại một trung tâm bảo hành; luôn ≥ 0 | `quantity` |
| **Ngưỡng tối thiểu** | Mức số tồn mà dưới đó hệ thống hiển thị cảnh báo tồn thấp | `min_threshold` |
| **Cảnh báo tồn thấp** | Thông báo hiển thị khi số tồn của một linh kiện < ngưỡng tối thiểu | `isLowStock` |
| **Giao dịch linh kiện** | Một lần nhập kho hoặc xuất kho linh kiện, ghi nhận đầy đủ ai, khi nào, bao nhiêu | `part_transaction` |
| **Nhập kho** | Loại giao dịch linh kiện ghi nhận linh kiện về trung tâm bảo hành, làm **tăng** số tồn | `IMPORT` *(giá trị của `transaction_type`)* |
| **Xuất kho** | Loại giao dịch linh kiện ghi nhận linh kiện lấy ra để sửa chữa, bắt buộc gắn với một phiếu bảo hành, làm **giảm** số tồn | `EXPORT` *(giá trị của `transaction_type`)* |
| **Lịch sử giao dịch linh kiện** | Danh sách các giao dịch linh kiện đã ghi nhận, xem theo linh kiện, khoảng ngày hoặc phiếu bảo hành | — |

---

## 2. Các bên liên quan và vai trò

| Vai trò | Số lượng | Được làm | Không được làm |
|---|:---:|---|---|
| **Kỹ thuật viên** | 38 | Xem tồn kho linh kiện tại trung tâm bảo hành của mình; tạo giao dịch xuất kho cho phiếu bảo hành của trung tâm bảo hành mình đang ở trạng thái **Đang xử lý** hoặc **Chờ linh kiện** | Nhập kho; xem lịch sử giao dịch linh kiện; chỉnh sửa ngưỡng tối thiểu; xem dữ liệu trung tâm bảo hành khác |
| **Quản lý trung tâm** | 6 | Xem tồn kho linh kiện tại trung tâm bảo hành của mình; tạo giao dịch nhập kho; cấu hình ngưỡng tối thiểu cho từng linh kiện; xem toàn bộ lịch sử giao dịch linh kiện; nhận cảnh báo tồn thấp | Xuất kho cho phiếu bảo hành; sửa hoặc xóa giao dịch linh kiện đã ghi nhận; xem hoặc chỉnh sửa dữ liệu trung tâm bảo hành khác |

---

## 3. Yêu cầu chức năng (FR) và User Story

### 3.1. Danh sách yêu cầu chức năng

| Mã FR | Tên yêu cầu | Mô tả | US liên quan | MoSCoW |
|---|---|---|:---:|:---:|
| **FR-01** | Xem tồn kho linh kiện | Hiển thị danh sách tồn kho linh kiện của trung tâm bảo hành mà người dùng thuộc về, gồm mã linh kiện, tên linh kiện, số tồn, ngưỡng tối thiểu; tìm được theo mã linh kiện hoặc tên linh kiện; không hiển thị dữ liệu trung tâm bảo hành khác | US-01 | MUST |
| **FR-02** | Ghi nhận nhập kho | Quản lý trung tâm tạo giao dịch nhập kho cho một linh kiện hợp lệ với số lượng nguyên > 0; sau khi lưu, số tồn tăng đúng bằng số lượng nhập và có đúng 1 giao dịch nhập kho được ghi. Số lượng ≤ 0 hoặc linh kiện không tồn tại thì bị từ chối | US-02 | MUST |
| **FR-03** | Xuất linh kiện cho phiếu bảo hành | Kỹ thuật viên tạo giao dịch xuất kho với số lượng nguyên > 0, bắt buộc gắn với phiếu bảo hành cùng trung tâm bảo hành đang ở trạng thái **Đang xử lý** hoặc **Chờ linh kiện**; sau khi lưu, số tồn giảm đúng bằng số lượng xuất và có đúng 1 giao dịch xuất kho gắn với phiếu bảo hành đó. Phiếu bảo hành ở trạng thái khác thì bị từ chối | US-03 | MUST |
| **FR-04** | Chặn xuất vượt số tồn | Khi số lượng xuất > số tồn hiện tại, hệ thống từ chối, thông báo nêu số lượng xuất và số tồn hiện có; số tồn không đổi và không ghi giao dịch linh kiện. Xuất đúng bằng số tồn thì được phép | US-04 | MUST |
| **FR-05** | Cảnh báo tồn thấp | Hiển thị cảnh báo tồn thấp cho mọi linh kiện có số tồn < ngưỡng tối thiểu, cập nhật sau mỗi giao dịch xuất kho, giao dịch nhập kho và khi đổi ngưỡng tối thiểu; quản lý trung tâm lọc được danh sách chỉ gồm linh kiện dưới ngưỡng; cảnh báo mất khi số tồn ≥ ngưỡng tối thiểu. Giá trị ngưỡng tối thiểu ban đầu lấy từ dữ liệu mẫu (`part_stock.min_threshold`) | US-05 | SHOULD |
| **FR-06** | Xem lịch sử giao dịch linh kiện | Quản lý trung tâm xem lịch sử giao dịch linh kiện, lọc theo linh kiện, loại giao dịch, phiếu bảo hành hoặc khoảng ngày; mỗi dòng gồm ngày giờ, loại giao dịch (nhập kho / xuất kho), số lượng, người thực hiện, mã phiếu bảo hành (nếu là xuất kho) | US-06, US-07 | SHOULD |
| **FR-07** | Thiết lập ngưỡng tối thiểu | Quản lý trung tâm đặt ngưỡng tối thiểu là số nguyên ≥ 0 cho từng linh kiện tại trung tâm bảo hành của mình; giá trị < 0 bị từ chối; sau khi lưu, cảnh báo tồn thấp cập nhật theo ngưỡng mới | US-08 | COULD |

---

### 3.2. User Story (8 story, chuẩn INVEST & MoSCoW)

#### US-01 · Xem tồn kho linh kiện — **MUST**
> Là **kỹ thuật viên**, tôi muốn **xem số tồn của từng linh kiện tại trung tâm bảo hành của mình** để **biết còn hàng hay không trước khi nhận sửa, tránh phải hẹn lại khách**.

| # | Given | When | Then |
|---|---|---|---|
| AC-1.1 | Kỹ thuật viên đã đăng nhập, thuộc trung tâm bảo hành "BH Quận 10" | Mở trang "Tồn kho linh kiện" | Hệ thống hiển thị danh sách tồn kho linh kiện chỉ của "BH Quận 10", mỗi dòng gồm mã linh kiện, tên linh kiện, số tồn và ngưỡng tối thiểu |
| AC-1.2 | Trang "Tồn kho linh kiện" đang mở, danh sách có 180 linh kiện | Nhập từ khóa "LCD" vào ô tìm kiếm | Danh sách chỉ hiển thị các linh kiện có mã linh kiện hoặc tên linh kiện chứa "LCD" |
| AC-1.3 | Kỹ thuật viên thuộc trung tâm bảo hành "BH Cần Thơ 1" | Cố truy cập tồn kho linh kiện của "BH Quận 10" | Hệ thống từ chối và hiển thị thông báo "Bạn không có quyền xem dữ liệu trung tâm bảo hành khác" |

---

#### US-02 · Ghi nhận nhập kho — **MUST**
> Là **quản lý trung tâm**, tôi muốn **ghi nhận nhập kho linh kiện** để **số tồn được cập nhật ngay, không phải chờ chốt cuối ngày như sổ tay giấy**.

| # | Given | When | Then |
|---|---|---|---|
| AC-2.1 | Linh kiện "LCD-IP15" có số tồn = 5 tại "BH Quận 10" | Quản lý trung tâm nhập kho 10 cái "LCD-IP15" và bấm Lưu | Số tồn "LCD-IP15" tăng lên 15; một giao dịch nhập kho được ghi nhận với số lượng = 10 |
| AC-2.2 | Quản lý trung tâm đang ở form "Nhập kho" | Nhập số lượng = 0 hoặc số âm và bấm Lưu | Hệ thống hiển thị lỗi "Số lượng nhập phải lớn hơn 0" và không lưu giao dịch nhập kho |
| AC-2.3 | Quản lý trung tâm nhập mã linh kiện không tồn tại "XYZ-999" | Bấm Lưu | Hệ thống hiển thị lỗi "Mã linh kiện không tồn tại trong danh mục" và không lưu giao dịch nhập kho |

---

#### US-03 · Xuất linh kiện cho phiếu bảo hành — **MUST**
> Là **kỹ thuật viên**, tôi muốn **xuất linh kiện gắn với một phiếu bảo hành cụ thể** để **biết mỗi phiếu bảo hành đã dùng linh kiện nào và truy vết được khi cần kiểm tra**.

| # | Given | When | Then |
|---|---|---|---|
| AC-3.1 | Linh kiện "PIN-SS-S24" có số tồn = 8; phiếu bảo hành "BH-000456/2026" đang ở trạng thái **Đang xử lý** | Kỹ thuật viên xuất kho 1 cái "PIN-SS-S24" cho phiếu bảo hành "BH-000456/2026" | Số tồn giảm xuống 7; một giao dịch xuất kho được ghi nhận kèm mã phiếu bảo hành "BH-000456/2026" |
| AC-3.2 | Phiếu bảo hành "BH-000789/2026" đang ở trạng thái **Đã đóng** | Kỹ thuật viên cố xuất kho linh kiện cho phiếu bảo hành này | Hệ thống từ chối: "Không thể xuất linh kiện cho phiếu bảo hành đã đóng"; số tồn không đổi |
| AC-3.3 | Phiếu bảo hành "BH-000111/2026" thuộc trung tâm bảo hành "BH Cần Thơ 1", kỹ thuật viên thuộc "BH Quận 10" | Kỹ thuật viên cố xuất kho linh kiện cho phiếu bảo hành này | Hệ thống từ chối: "Phiếu bảo hành không thuộc trung tâm bảo hành của bạn" |

---

#### US-04 · Chặn xuất vượt số tồn — **MUST**
> Là **kỹ thuật viên**, tôi muốn **hệ thống từ chối khi tôi xuất nhiều hơn số tồn hiện có** để **số tồn không bị âm và luôn khớp với thực tế trong kho**.

| # | Given | When | Then |
|---|---|---|---|
| AC-4.1 | Linh kiện "MH-OP-R5" có số tồn = 2 tại "BH Cần Thơ 1" | Kỹ thuật viên xuất kho 3 cái "MH-OP-R5" | Hệ thống từ chối, hiển thị "Số lượng xuất (3) vượt quá số tồn hiện tại (2). Giao dịch bị hủy."; số tồn giữ nguyên = 2 |
| AC-4.2 | Linh kiện "MH-OP-R5" có số tồn = 2 | Kỹ thuật viên xuất kho đúng 2 cái | Giao dịch xuất kho thành công; số tồn = 0 |
| AC-4.3 | Linh kiện "MH-OP-R5" có số tồn = 0 | Kỹ thuật viên cố xuất kho 1 cái | Hệ thống từ chối: "Linh kiện này đã hết hàng tại trung tâm bảo hành của bạn" |

---

#### US-05 · Cảnh báo tồn thấp — **SHOULD**
> Là **quản lý trung tâm**, tôi muốn **được cảnh báo khi số tồn của một linh kiện xuống dưới ngưỡng tối thiểu** để **đặt hàng bổ sung kịp trước khi hết, giảm số lần kỹ thuật viên phải hẹn lại khách**.

| # | Given | When | Then |
|---|---|---|---|
| AC-5.1 | Linh kiện "SAC-IP15" có ngưỡng tối thiểu = 5, số tồn = 6 | Một giao dịch xuất kho 2 cái làm số tồn giảm xuống 4 | Hệ thống hiển thị cảnh báo tồn thấp ngay trên trang "Tồn kho linh kiện": "SAC-IP15 – Số tồn (4) dưới ngưỡng tối thiểu (5)" |
| AC-5.2 | Trang "Tồn kho linh kiện" đang mở | Quản lý trung tâm bật bộ lọc "Chỉ hiện linh kiện dưới ngưỡng" | Chỉ hiển thị các linh kiện có số tồn nhỏ hơn ngưỡng tối thiểu của linh kiện đó |
| AC-5.3 | Linh kiện "SAC-IP15" đang có cảnh báo tồn thấp, số tồn = 4, ngưỡng tối thiểu = 5 | Quản lý trung tâm nhập kho thêm 2 cái, số tồn tăng lên 6 | Cảnh báo tồn thấp biến mất khỏi danh sách |

---

#### US-06 · Xem lịch sử giao dịch linh kiện theo linh kiện — **SHOULD**
> Là **quản lý trung tâm**, tôi muốn **xem lịch sử giao dịch linh kiện theo từng linh kiện, có thể lọc theo khoảng ngày** để **đối chiếu với số tồn thực tế và phát hiện sai lệch sớm**.

| # | Given | When | Then |
|---|---|---|---|
| AC-6.1 | Linh kiện "LCD-IP15" có 50 giao dịch linh kiện trong tháng 9/2026 | Quản lý trung tâm chọn linh kiện "LCD-IP15" và lọc khoảng ngày 01/09 – 30/09/2026 | Hiển thị đúng 50 giao dịch; mỗi dòng gồm ngày giờ, loại giao dịch (nhập kho / xuất kho), số lượng, người thực hiện, mã phiếu bảo hành (nếu là xuất kho) |
| AC-6.2 | Quản lý trung tâm đang xem lịch sử giao dịch linh kiện "LCD-IP15" | Đổi bộ lọc sang tháng 10/2026 (chưa có giao dịch nào) | Hệ thống hiển thị danh sách rỗng và thông báo "Không có giao dịch nào trong khoảng thời gian này" |

---

#### US-07 · Xem lịch sử giao dịch linh kiện theo phiếu bảo hành — **SHOULD**
> Là **quản lý trung tâm**, tôi muốn **xem tất cả linh kiện đã xuất kho cho một phiếu bảo hành cụ thể** để **kiểm tra việc dùng linh kiện của từng phiếu bảo hành và phát hiện bất thường**.

| # | Given | When | Then |
|---|---|---|---|
| AC-7.1 | Phiếu bảo hành "BH-000456/2026" đã có 2 giao dịch xuất kho: 1× "LCD-IP15" và 1× "PIN-IP15" | Quản lý trung tâm mở chi tiết phiếu bảo hành "BH-000456/2026", chọn tab "Linh kiện đã dùng" | Hiển thị bảng gồm: mã linh kiện, tên linh kiện, số lượng, người thực hiện, ngày giờ; tổng số linh kiện đã dùng hiển thị ở cuối bảng |
| AC-7.2 | Phiếu bảo hành "BH-000999/2026" chưa có giao dịch xuất kho nào | Quản lý trung tâm mở tab "Linh kiện đã dùng" của phiếu bảo hành này | Hệ thống hiển thị bảng rỗng và thông báo "Phiếu bảo hành này chưa có linh kiện nào được xuất" |

---

#### US-08 · Thiết lập ngưỡng tối thiểu — **COULD**
> Là **quản lý trung tâm**, tôi muốn **thiết lập và điều chỉnh ngưỡng tối thiểu cho từng linh kiện tại trung tâm bảo hành của mình** để **cảnh báo tồn thấp phù hợp với nhu cầu sử dụng thực tế, tránh thiếu hàng đột ngột**.

| # | Given | When | Then |
|---|---|---|---|
| AC-8.1 | Linh kiện "LCD-IP15" tại "BH Quận 10" có ngưỡng tối thiểu = 3, số tồn = 4 | Quản lý trung tâm sửa ngưỡng tối thiểu thành 5 và bấm Lưu | Ngưỡng tối thiểu mới được lưu; vì số tồn (4) < ngưỡng tối thiểu mới (5), cảnh báo tồn thấp xuất hiện ngay trên trang "Tồn kho linh kiện" |
| AC-8.2 | Quản lý trung tâm đang chỉnh sửa ngưỡng tối thiểu của một linh kiện | Nhập giá trị = -1 và bấm Lưu | Hệ thống từ chối và hiển thị lỗi "Ngưỡng tối thiểu phải là số nguyên ≥ 0" |

---

### 3.3. Tổng hợp User Story theo MoSCoW

| Mức ưu tiên | Nội dung | Số lượng | Ghi chú |
|---|---|:---:|---|
| **MUST** | US-01, US-02, US-03, US-04 | 4 | Bắt buộc hoàn thành trong BT2 |
| **SHOULD** | US-05, US-06, US-07 | 3 | Hiện thực trong BT2 nếu kịp tiến độ |
| **COULD** | US-08 | 1 | Tính năng mở rộng |
| **WON'T** | Đặt hàng nhà cung cấp tự động; điều chuyển linh kiện giữa các trung tâm bảo hành; dự báo nhu cầu bằng AI/ML; thanh toán trực tuyến (xem mục 1.3) | — | Không làm trong học phần CĐTN1 |
| **Tổng cộng** | | **8** | Mỗi story có mã US, mức MoSCoW và tiêu chí chấp nhận Given–When–Then |

---

## 4. Yêu cầu phi chức năng (NFR)

| Mã NFR | Phân loại | Yêu cầu | Ngưỡng đo lường |
|---|---|---|---|
| **NFR-01** | **Hiệu năng** *(Performance)* | Trang "Tồn kho linh kiện", lịch sử giao dịch linh kiện, nhập kho và xuất kho phản hồi nhanh khi nhiều người dùng cùng sử dụng | Thời gian phản hồi **≤ 500 ms** cho 95% request, với 10.000 giao dịch linh kiện và 20 người dùng đồng thời, trên máy 2 CPU, 4 GB RAM |
| **NFR-02** | **Truy cập đồng thời** *(Concurrency)* | Số tồn không bị âm khi nhiều kỹ thuật viên cùng xuất kho một linh kiện | Với 50 yêu cầu xuất kho (mỗi yêu cầu 1 cái) gửi cùng lúc cho linh kiện có số tồn = 10: đúng 10 yêu cầu thành công, 40 bị từ chối, số tồn cuối = 0, số lần số tồn âm = 0 |
| **NFR-03** | **Bảo mật và phân quyền** *(Security)* | Người dùng chỉ truy cập dữ liệu của trung tâm bảo hành mình và chỉ làm đúng quyền của vai trò | 100% API nghiệp vụ (trừ đăng nhập và kiểm tra hệ thống) yêu cầu đăng nhập; truy cập dữ liệu trung tâm bảo hành khác hoặc thao tác ngoài quyền bị từ chối (HTTP 403) trong 100% của ≥ 10 ca kiểm thử |
| **NFR-04** | **Truy vết và toàn vẹn dữ liệu** *(Traceability & Data Integrity)* | Mọi giao dịch linh kiện truy vết được và không bị xóa; số tồn luôn khớp với lịch sử giao dịch linh kiện | 100% giao dịch linh kiện có người thực hiện và thời điểm; số lần xóa vật lý = 0; sai lệch giữa số tồn và (tổng nhập kho − tổng xuất kho) = 0 trên 100% linh kiện sau bộ kiểm thử |

---

## 5. Ràng buộc và quy tắc nghiệp vụ

### 5.1. Quy tắc nghiệp vụ
*(QT-xx trích từ Bảng 9.1 của case study; QT-L5-xx do phân tích luồng L5 suy ra)*

| Mã quy tắc | Tên quy tắc | Nội dung | Nguồn |
|---|---|---|---|
| **QT-06** | Vòng đời phiếu bảo hành | Phiếu bảo hành chỉ chuyển trạng thái theo đúng vòng đời, không quay lại trạng thái trước. Luồng L5 chỉ đọc trạng thái phiếu, không đổi trạng thái phiếu | Case study, Bảng 9.1 |
| **QT-09** | Chặn xuất vượt số tồn và cảnh báo tồn thấp | Không được xuất kho vượt số tồn hiện tại của trung tâm bảo hành. Khi số tồn xuống dưới ngưỡng tối thiểu, hệ thống phải hiển thị cảnh báo tồn thấp | Case study, Bảng 9.1 |
| **QT-13** | Không xóa vật lý | Không xóa vật lý dữ liệu, chỉ đánh dấu ngừng sử dụng. Với luồng L5: giao dịch linh kiện đã ghi không được sửa hoặc xóa | Case study, Bảng 9.1 |
| **QT-14** | Cô lập dữ liệu trung tâm bảo hành | Nhân viên chỉ xem và thao tác trên dữ liệu của trung tâm bảo hành mình làm việc; quản lý xem toàn bộ đơn vị mình phụ trách | Case study, Bảng 9.1 |
| **QT-L5-01** | Ràng buộc nhập kho | Mỗi giao dịch nhập kho phải có linh kiện hợp lệ, số lượng nguyên > 0, người thực hiện và thời điểm | Phân tích từ sổ tay tồn kho |
| **QT-L5-02** | Ràng buộc xuất kho | Mỗi giao dịch xuất kho phải có số lượng nguyên > 0 và gắn với đúng một phiếu bảo hành cùng trung tâm bảo hành, đang ở trạng thái **Đang xử lý** hoặc **Chờ linh kiện** | Phân tích quy trình thực tế |
| **QT-L5-03** | Số tồn không âm | Số tồn của mọi linh kiện luôn ≥ 0, kể cả khi nhiều kỹ thuật viên cùng xuất kho một linh kiện | Suy ra từ QT-09 và NFR-02 |

### 5.2. Ràng buộc (Constraints)

- **RB-01**: Triển khai độc lập từng luồng nghiệp vụ, không phụ thuộc đồng bộ vào luồng khác (chỉ đạo của Phó Tổng giám đốc: làm từng phần, phần nào xong dùng phần đó). L5 chỉ đọc dữ liệu phiếu bảo hành mẫu.
- **RB-02**: Công nghệ: Java 17, Spring Boot, MySQL 8; kiểu dữ liệu của từ điển tham chiếu (PostgreSQL) được quy đổi sang MySQL 8.
- **RB-03**: Phiếu bảo hành do luồng L2 quản lý; luồng L5 chỉ đọc dữ liệu phiếu bảo hành (tập dữ liệu mẫu khoảng 400 phiếu bảo hành).

---

## 6. Bảng truy vết yêu cầu (Traceability Matrix)

| Mã FR | Tên yêu cầu chức năng | User Story | Use Case | MoSCoW | Bảng dữ liệu liên quan | Màn hình giao diện |
|:---:|---|:---:|---|:---:|---|---|
| **FR-01** | Xem tồn kho linh kiện theo trung tâm | US-01 | UC-01 – Xem tồn kho linh kiện | **MUST** | `parts`, `part_stock`, `service_centers` | **M1** – Danh sách tồn kho |
| **FR-02** | Ghi nhận nhập kho linh kiện | US-02 | UC-02 – Ghi nhận nhập kho | **MUST** | `part_stock`, `part_transactions` | **M2** – Form Nhập kho |
| **FR-03** | Xuất linh kiện cho phiếu bảo hành | US-03 | UC-03 – Xuất linh kiện cho phiếu bảo hành | **MUST** | `part_stock`, `part_transactions`, `tickets` | **M2** – Form Xuất kho |
| **FR-04** | Chặn xuất vượt quá số tồn kho | US-04 | UC-04 – Kiểm tra số tồn trước khi xuất | **MUST** | `part_stock` (ràng buộc `quantity >= 0`) | **M2** (Thông báo lỗi chặn xuất) |
| **FR-05** | Cảnh báo tồn kho dưới ngưỡng tối thiểu | US-05 | UC-05 – Xem cảnh báo tồn thấp | **SHOULD** | `part_stock` (`quantity < min_threshold`) | **M1** (Badge Cảnh báo tồn thấp) |
| **FR-06** | Xem lịch sử giao dịch linh kiện | US-06, US-07 | UC-06 – Xem lịch sử giao dịch linh kiện | **SHOULD** | `part_transactions`, `parts`, `tickets` | **M3** – Lịch sử giao dịch |
| **FR-07** | Thiết lập ngưỡng cảnh báo tối thiểu | US-08 | UC-07 – Thiết lập ngưỡng tối thiểu | **COULD** | `part_stock` (`min_threshold`) | **M1** – Cửa sổ cài đặt ngưỡng |

#### Nguyên tắc kiểm chứng ma trận:
- **Đọc theo HÀNG (Mỗi yêu cầu đủ chân liên kết):** 100% yêu cầu chức năng (FR-01 đến FR-07) đều được điền đầy đủ User Story, Use Case, Bảng dữ liệu và Màn hình giao diện tương ứng — **tuyệt đối không có ô trống**. Các tính năng ngoài phạm vi (WON'T) được đặc tả riêng tại Mục 1.3.
- **Đọc theo CỘT (Không có bảng cô lập):** Toàn bộ 5 bảng dữ liệu trong ERD (`parts`, `part_stock`, `part_transactions`, `service_centers`, `tickets`) đều xuất hiện tại cột "Bảng dữ liệu liên quan", đảm bảo không có bảng cô lập theo tiêu chí kỹ thuật của Track SE.

---

*Tài liệu SRS rút gọn – Luồng L5 – Kho linh kiện thay thế (Track SE).*
