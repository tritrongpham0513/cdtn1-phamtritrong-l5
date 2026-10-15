# Bảng khai báo sử dụng công cụ AI hỗ trợ (Phụ lục bắt buộc)

> **Học phần:** Chuyên đề tốt nghiệp 1 – Học kỳ 1, Năm học 2026 – 2027  
> **Sinh viên:** Phạm Trí Trọng – MSSV: 2374802010525  
> **Luồng nghiệp vụ:** L5 – Kho linh kiện thay thế (Track SE)  
> **Bài tập:** Bài tập 1 – Phân tích và Thiết kế hệ thống  

---

## 1. Bảng kê khai chi tiết sử dụng công cụ AI

| Công cụ AI | Nội dung hỗ trợ | Áp dụng ở phần nào | Phương pháp kiểm chứng & Tự điều chỉnh của Sinh viên |
| :--- | :--- | :--- | :--- |
| Antigravity AI IDE<br>(Gemini 3.8 Flash & Claude Sonnet 4.6) | Viết context nghiệp vụ, Gợi ý cấu trúc dàn bài chuẩn IEEE 29148, rà soát 8 User Story chuẩn INVEST và bảng truy vết yêu cầu,<br><br>Hỗ trợ rà soát logic và kiểm tra tính đúng đắn khi vẽ sơ đồ Use Case & đặc tả Use Case.<br><br>Đề xuất khung kiến trúc phân tầng 4 lớp và gợi ý các câu lập luận theo khuôn mẫu NFR. | Mục 1 – Bản SRS rút gọn (docs/srs.md)<br><br>Mục 2 – Use Case<br><br>Mục 3 – Thiết kế kiến trúc (docs/architecture.md, architecture.drawio) | Đối chiếu trực tiếp với nghiệp vụ case study Mekong Mobile và giáo trình Kỹ thuật phần mềm; tự điều chỉnh các ngưỡng NFR có số đo cụ thể,<br><br>Vẽ và tinh chỉnh sơ đồ Use Case trên Draw.io (kiểm chứng Actor, ranh giới hệ thống, quan hệ «include»)<br><br>Vẽ và kiểm chứng nguyên tắc phụ thuộc một chiều, đối chiếu đánh đổi kiến trúc với bài toán xử lý đồng thời. |
| Claude Sonnet Web | Hỗ trợ sinh mã DDL skeleton và gợi ý các chỉ mục hiệu năng (Index). | Mục 4 – Mô hình dữ liệu (docs/erd.md, schema.sql, erd.drawio) | Đối chiếu 100% cột với bộ dữ liệu mẫu CSV (data/parts.csv); kiểm chứng chuẩn 3NF và kiểm tra cú pháp chạy thực tế trên MySQL 8.x. |
| Antigravity AI IDE<br>Figma AI | Hỗ trợ dựng bố cục dàn trang Wireframe đen trắng và prompt giao diện cho Figma. | Mục 5 – Wireframe 3 màn hình (docs/wireframe.md) | Đối chiếu 100% trường dữ liệu trên màn hình với các thuộc tính trong ERD; kiểm tra nhãn chuẩn theo Glossary. |

---

## 2. Lời cam kết liêm chính học thuật và trách nhiệm của sinh viên

> **"Tôi xác nhận đã đọc, hiểu và chịu trách nhiệm về toàn bộ nội dung nộp."**

Tôi xin cam đoan:
1. Bảng kê khai trên phản ánh trung thực, chính xác và đầy đủ 100% quá trình sử dụng các công cụ trí tuệ nhân tạo (AI) trong quá trình thực hiện Bài tập 1 môn Chuyên đề Tốt nghiệp 1.
2. Các công cụ AI chỉ đóng vai trò hỗ trợ gợi ý cấu trúc, rà soát logic và chuẩn hóa mẫu tài liệu. Toàn bộ nội dung phân tích nghiệp vụ luồng L5, đặc tả yêu cầu, tính toán chỉ số NFR có số đo, thiết kế kiến trúc, mô hình dữ liệu ERD, mã DDL và các quyết định kỹ thuật đều do chính tôi tự tìm hiểu, đối chiếu thực tế case study Mekong Mobile, kiểm chứng và chịu trách nhiệm hoàn toàn.
3. Tôi cam kết tuân thủ nghiêm ngặt Quy định về Liêm chính học thuật của Trường Đại học Văn Lang và Khoa Công nghệ Thông tin. Mọi vi phạm nếu có sẽ chịu xử lý theo quy chế của Nhà trường.

- **Sinh viên cam kết:** Phạm Trí Trọng  
- **Mã số sinh viên (MSSV):** 2374802010525  
- **Chuyên ngành (Track):** Kỹ thuật Phần mềm (Software Engineering - SE)  
- **Ngày cam kết:** 15/10/2026  
- **Chữ ký:** *(Đã ký xác nhận điện tử)*

