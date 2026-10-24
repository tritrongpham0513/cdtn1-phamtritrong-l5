# Nhật ký thay đổi thiết kế

> **Dự án:** Smart CRM – Mekong Mobile  
> **Học phần:** Chuyên đề tốt nghiệp 1 · Học kỳ 1, Năm học 2026 – 2027  
> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525  
> **Chuyên ngành (Track):** Kỹ thuật Phần mềm (Software Engineering - SE)  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế  

---

## Sau phản hồi Bài tập 1 (Buổi 8, ngày 24/10/2026)

### TD-01: Chuẩn hóa và đồng bộ tên kỹ thuật giữa Glossary, DDL, API Contract và Wireframe
- **Phản hồi của GV:** Tên kỹ thuật chưa khớp: glossary ghi `IMPORT`/`EXPORT`, `part_transaction` nhưng DDL và wireframe dùng `'NHAP'`/`'XUAT'`, `part_transactions`; trạng thái `'Mới'` trong glossary nhưng DDL là `'TIEP_NHAN'`.
- **Quyết định:** SỬA.
- **Lý do:** Tuân thủ triệt để nguyên tắc cốt lõi của môn học: mỗi khái niệm chỉ dùng ĐÚNG MỘT TÊN kỹ thuật bất biến trên toàn bộ vòng đời phân tích - thiết kế - cài đặt. Giao diện người dùng có thể dùng nhãn tiếng Việt để tối ưu trải nghiệm (UX), nhưng tầng dữ liệu lưu trữ CSDL và API payload phải chuẩn hóa bằng enum tiếng Anh bất biến.
- **Ảnh hưởng:**
  - **Bảng thuật ngữ (Glossary - Mục 1.1.4):** Chuẩn hóa tên bảng CSDL là `part_transactions`, bản ghi giao dịch là `part_transaction`; quy định giá trị kỹ thuật loại giao dịch là `IMPORT` và `EXPORT`; chuẩn hóa vòng đời 6 trạng thái phiếu bảo hành: `TIEP_NHAN` -> `DA_PHAN_CONG` -> `DANG_XU_LY` -> `CHO_LINH_KIEN` -> `HOAN_TAT` -> `DA_DONG` (loại bỏ hoàn toàn từ đơn lẻ `'Mới'`).
  - **Từ điển dữ liệu & DDL (Mục 4.2.5, 4.3 trong báo cáo; `db/schema.sql`):** Quy định kiểu `transaction_type VARCHAR(10)`, bổ sung ràng buộc kiểm tra toàn vẹn `CONSTRAINT chk_tx_type CHECK (transaction_type IN ('IMPORT', 'EXPORT'))`, ràng buộc trạng thái phiếu `CONSTRAINT chk_tickets_status CHECK (status IN ('TIEP_NHAN', 'DA_PHAN_CONG', 'DANG_XU_LY', 'CHO_LINH_KIEN', 'HOAN_TAT', 'DA_DONG'))`, và quy định `ticket_id` bắt buộc khi `EXPORT`, nhận `NULL` khi `IMPORT`.
  - **Wireframe giao diện (Mục 5.1, 5.2, 5.3):** Bổ sung *Ghi chú kỹ thuật ánh xạ giao diện - CSDL* cho cả M1, M2, M3 phân định rõ nhãn tiếng Việt hiển thị trên UI và giá trị lưu trữ kỹ thuật tương ứng trong CSDL và API.

---

### TD-02: Chuẩn hóa liên kết Use Case UC-07 và UC-06 trên Wireframe M3 và M1
- **Phản hồi của GV:** UC-07 là 'Cài đặt ngưỡng' trong bảng truy vết nhưng wireframe M3 ghi 'UC-07 (Tra cứu theo phiếu bảo hành)'.
- **Quyết định:** SỬA.
- **Lý do:** Theo Bảng ma trận truy vết yêu cầu (Traceability Matrix - Mục 1.6 & 2.1) và Sơ đồ Use Case, `US-06` (tra cứu theo linh kiện) và `US-07` (xem linh kiện đã xuất theo phiếu bảo hành) cùng được quy tụ vào một Use Case duy nhất là **`UC-06 (Xem lịch sử giao dịch kho)`** thể hiện trên **Màn hình M3**. Trong khi đó, **`UC-07`** là **`UC-07 (Thiết lập ngưỡng tối thiểu)`** (tương ứng `US-08`), được thao tác thông qua Modal Cài đặt ngưỡng tại **Màn hình M1**.
- **Ảnh hưởng:**
  - **Màn hình M3 (Mục 5.3):** Sửa Use Case mapping thành: `UC-06 (Xem lịch sử giao dịch kho – bao gồm tra cứu lịch sử theo linh kiện [US-06] và xem linh kiện đã xuất theo phiếu bảo hành [US-07])`.
  - **Màn hình M1 (Mục 5.1):** Khẳng định rõ Use Case mapping: `UC-01 (Xem tồn kho linh kiện), UC-05 (Cảnh báo tồn kho dưới ngưỡng tối thiểu), UC-07 (Thiết lập ngưỡng tối thiểu – qua Modal cài đặt ngưỡng)`.
  - Đảm bảo tính truy vết hai chiều (Traceability) xuyên suốt 100% giữa Bảng truy vết ↔ Sơ đồ Use Case ↔ Wireframe.

---

### TD-03: Chuẩn hóa diễn đạt số lượng User Story tại Mục 1.3.3
- **Phản hồi của GV:** Có 8 User Story nhưng mục 1.3.3 lại ghi 'đạt chuẩn 5–7 story'.
- **Quyết định:** SỬA.
- **Lý do:** Báo cáo đã phân tích, đặc tả hoàn chỉnh và chi tiết cả 8 User Story (từ US-01 đến US-08) theo chuẩn INVEST kèm đầy đủ tiêu chí chấp nhận GWT (Given-When-Then), bao phủ 100% phạm vi nghiệp vụ luồng L5. Câu chữ cũ diễn đạt chưa chuẩn xác gây cảm giác mâu thuẫn số liệu.
- **Ảnh hưởng:**
  - Cập nhật dòng **TỔNG CỘNG** tại Bảng tổng hợp Story Points (Mục 1.3.3 trong báo cáo): `8 story | Vượt mức tối thiểu đề bài (yêu cầu 5–7 story), bao phủ toàn diện 100% luồng nghiệp vụ L5. | 26 SP`.
  - Đồng bộ nhất quán với Mục 3.3 trong tài liệu `docs/srs.md`.

---

### TD-04: Khôi phục tài liệu API Contract và bổ sung cam kết liêm chính học thuật trong Phụ lục AI
- **Phản hồi của GV:** Phụ lục AI thiếu dòng cam kết, họ tên, MSSV, ngày; repo đã xóa api-contract.md.
- **Quyết định:** SỬA.
- **Lý do:** Đối với Track SE, tài liệu `docs/api-contract.md` là sản phẩm kỹ thuật bắt buộc để định nghĩa hợp đồng giao tiếp giữa frontend prototype và backend service, chuẩn bị cho việc viết mã nguồn ở Sprint 1 & 2. Bản kê khai AI phải tuân thủ nghiêm ngặt Quy định về Liêm chính học thuật của Trường Đại học Văn Lang với đầy đủ cam kết trách nhiệm cá nhân của sinh viên.
- **Ảnh hưởng:**
  - **Khôi phục `docs/api-contract.md`:** Khôi phục trọn vẹn 729 dòng đặc tả RESTful API chi tiết (bao quát từ UC-01 đến UC-07, 9 endpoint, request/response DTO, mã lỗi nghiệp vụ, JWT auth).
  - **Bổ sung Mục 2 trong Phụ lục AI (báo cáo và `docs/ai-declaration.md`):** Đưa vào đầy đủ khẩu lệnh cam kết nguyên văn: `> "Tôi xác nhận đã đọc, hiểu và chịu trách nhiệm về toàn bộ nội dung nộp."`, cùng thông tin sinh viên Phạm Trí Trọng, MSSV 2374802010525, Track SE và ngày ký cam kết 15/10/2026.
