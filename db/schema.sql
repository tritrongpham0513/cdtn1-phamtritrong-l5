-- =============================================================================
-- TRƯỜNG ĐẠI HỌC VĂN LANG · KHOA CÔNG NGHỆ THÔNG TIN
-- HỌC PHẦN: CHUYÊN ĐỀ TỐT NGHIỆP 1 · HỌC KỲ 1, NĂM HỌC 2026 – 2027
-- BÀI TẬP 1: THIẾT KẾ KIẾN TRÚC VÀ MÔ HÌNH DỮ LIỆU
-- TRACK: SOFTWARE ENGINEERING (SE)
-- 
-- Sinh viên: Phạm Trí Trọng – MSSV: 2374802010525
-- Luồng nghiệp vụ: L5 – Kho linh kiện thay thế (Smart CRM Mekong Mobile)
-- File: schema.sql (Hệ quản trị CSDL: MySQL 8.x / MariaDB)
-- =============================================================================

DROP TABLE IF EXISTS part_transactions;
DROP TABLE IF EXISTS part_stock;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS parts;
DROP TABLE IF EXISTS service_centers;

-- =============================================================================
-- 1. BẢNG service_centers: Danh mục các Trung tâm Bảo hành của Mekong Mobile
-- Phục vụ: QT-14 (phân vùng tồn kho theo trung tâm), NFR-03 (cô lập dữ liệu)
-- =============================================================================
CREATE TABLE service_centers (
    center_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    center_code   VARCHAR(20)  NOT NULL UNIQUE,               -- Ví dụ: TT-CT1, TT-Q10 (Khóa nghiệp vụ)
    center_name   VARCHAR(120) NOT NULL,                      -- Tên chi nhánh trung tâm bảo hành
    address       VARCHAR(255) NOT NULL,                      -- Địa chỉ vật lý
    phone         VARCHAR(20)  NOT NULL,                      -- Số điện thoại liên hệ
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,         -- Trạng thái hoạt động
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 2. BẢNG parts: Danh mục các loại linh kiện thay thế chuẩn hóa
-- Phục vụ: FR-01, FR-02, US-01, AC-2.3 (kiểm tra linh kiện hợp lệ trong danh mục)
-- =============================================================================
CREATE TABLE parts (
    part_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_code     VARCHAR(30)   NOT NULL UNIQUE,              -- Ví dụ: PIN-IP15, LCD-IP15, MH-OP-R5 (QT-01)
    part_name     VARCHAR(150)  NOT NULL,                     -- Tên mô tả linh kiện
    category      VARCHAR(50)   NOT NULL,                     -- Phân loại: PIN, MAN_HINH, CAMERA, MAINBOARD...
    unit          VARCHAR(20)   NOT NULL DEFAULT 'Cái',       -- Đơn vị tính
    unit_price    DECIMAL(12,2) NOT NULL DEFAULT 0.00,        -- Đơn giá tham chiếu (VNĐ)
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,        -- Trạng thái kinh doanh / sử dụng
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_parts_price CHECK (unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 3. BẢNG part_stock: Quản lý số lượng tồn kho theo từng trung tâm bảo hành
-- Phục vụ: FR-01, FR-03, FR-04, FR-07, US-01, US-04, US-05, US-08
-- Ràng buộc cốt lõi: quantity >= 0 (Tuyệt đối không âm kho theo NFR-02, QT-09, QT-L5-03)
-- =============================================================================
CREATE TABLE part_stock (
    stock_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    center_id     BIGINT NOT NULL,
    part_id       BIGINT NOT NULL,
    quantity      INT    NOT NULL DEFAULT 0,                  -- Số lượng tồn thực tế tại trung tâm
    min_threshold INT    NOT NULL DEFAULT 5,                  -- Ngưỡng cảnh báo tồn thấp (US-05, US-08)
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_stock_center FOREIGN KEY (center_id) REFERENCES service_centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT fk_stock_part   FOREIGN KEY (part_id)   REFERENCES parts(part_id) ON DELETE RESTRICT,
    CONSTRAINT uk_stock_center_part UNIQUE (center_id, part_id), -- Một linh kiện tại 1 TT chỉ có 1 dòng tồn
    CONSTRAINT chk_stock_qty_positive CHECK (quantity >= 0),     -- QT-09, NFR-02: Chặn xuất âm ở tầng CSDL
    CONSTRAINT chk_stock_threshold_positive CHECK (min_threshold >= 0) -- US-08: Ngưỡng tối thiểu không âm
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 4. BẢNG tickets: Bảng phiếu bảo hành (Dữ liệu mẫu chia sẻ từ luồng L2 theo RB-03)
-- Phục vụ: FR-02, US-03, QT-06 (xuất kho phải gắn phiếu BH hợp lệ cùng trung tâm)
-- =============================================================================
CREATE TABLE tickets (
    ticket_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_code   VARCHAR(30)  NOT NULL UNIQUE,               -- Ví dụ: BH-000456/2026 (Khóa nghiệp vụ)
    center_id     BIGINT       NOT NULL,                      -- Thuộc trung tâm bảo hành nào (QT-14)
    status        VARCHAR(30)  NOT NULL DEFAULT 'TIEP_NHAN',  -- TIEP_NHAN, DANG_XU_LY, CHO_LINH_KIEN, HOAN_TAT, DA_DONG
    customer_name VARCHAR(120) NOT NULL,                      -- Tên khách hàng mang máy đến
    device_model  VARCHAR(100) NOT NULL,                      -- Model thiết bị (iPhone 15, Oppo Reno 5...)
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_tickets_center FOREIGN KEY (center_id) REFERENCES service_centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT chk_tickets_status CHECK (status IN ('TIEP_NHAN', 'DANG_XU_LY', 'CHO_LINH_KIEN', 'HOAN_TAT', 'DA_DONG'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 5. BẢNG part_transactions: Lịch sử giao dịch nhập/xuất kho (Append-Only Log)
-- Phục vụ: FR-02, FR-05, FR-06, US-02, US-03, US-06, US-07, QT-13, NFR-04
-- Không có UPDATE/DELETE vật lý — giải quyết dứt điểm lỗi số 2 trong 5 lỗi ERD
-- =============================================================================
CREATE TABLE part_transactions (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_code VARCHAR(30)  NOT NULL UNIQUE,            -- Mã giao dịch (GD-NK-202610-001, GD-XK-202610-002)
    center_id        BIGINT       NOT NULL,                   -- Trung tâm bảo hành phát sinh giao dịch
    part_id          BIGINT       NOT NULL,                   -- Linh kiện được nhập hoặc xuất
    ticket_id        BIGINT       NULL,                       -- Phiếu BH (Bắt buộc với XUAT, NULL với NHAP)
    transaction_type VARCHAR(10)  NOT NULL,                   -- NHAP hoặc XUAT (QT-L5-01, QT-L5-02)
    quantity         INT          NOT NULL,                   -- Số lượng linh kiện giao dịch
    performed_by     VARCHAR(100) NOT NULL,                   -- Nhân viên / KTV thực hiện (NFR-04)
    note             VARCHAR(255) NULL,                       -- Ghi chú bổ sung
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm giao dịch (Bất biến)
    
    CONSTRAINT fk_tx_center FOREIGN KEY (center_id) REFERENCES service_centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_part   FOREIGN KEY (part_id)   REFERENCES parts(part_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE RESTRICT,
    CONSTRAINT chk_tx_type  CHECK (transaction_type IN ('NHAP', 'XUAT')),
    CONSTRAINT chk_tx_qty   CHECK (quantity > 0)              -- Số lượng nhập/xuất phải nguyên dương (> 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- CHIẾN LƯỢC ĐÁNH CHỈ MỤC (INDEX) GẮN VỚI YÊU CẦU PHI CHỨC NĂNG (NFR)
-- =============================================================================

-- 1. Index ghép (center_id, part_id) trên part_stock:
-- Phục vụ NFR-01 (tra cứu tồn kho < 500ms) và NFR-02 (khóa bi quan SELECT FOR UPDATE)
CREATE INDEX idx_stock_center_part ON part_stock(center_id, part_id);

-- 2. Index phục vụ lọc cảnh báo tồn thấp (US-05):
-- Cho phép truy vấn nhanh các linh kiện có quantity < min_threshold tại 1 trung tâm
CREATE INDEX idx_stock_alert ON part_stock(center_id, quantity, min_threshold);

-- 3. Index phục vụ tra cứu lịch sử giao dịch theo trung tâm và ngày (NFR-01, US-06):
-- Tối ưu câu lệnh ORDER BY created_at DESC và phân trang LIMIT 20
CREATE INDEX idx_tx_center_created ON part_transactions(center_id, created_at);

-- 4. Index phục vụ tra cứu lịch sử xuất linh kiện theo từng Phiếu bảo hành (US-07):
-- Cho phép KTV mở tab "Linh kiện đã dùng" của phiếu BH tải ngay lập tức
CREATE INDEX idx_tx_ticket ON part_transactions(ticket_id);

-- 5. Index phục vụ tra cứu lịch sử theo từng linh kiện (US-06):
CREATE INDEX idx_tx_part_created ON part_transactions(part_id, created_at);

-- =============================================================================
-- DỮ LIỆU KHỞI TẠO MẪU (SEED DATA) PHỤC VỤ CHẠY THỬ VÀ KIỂM CHỨNG
-- =============================================================================

-- Trung tâm bảo hành mẫu
INSERT INTO service_centers (center_code, center_name, address, phone) VALUES
('TT-CT1', 'Trung tâm Bảo hành Mekong Cần Thơ 1', 'Số 123 đường 30/4, Ninh Kiều, Cần Thơ', '0292.3888.999'),
('TT-Q10', 'Trung tâm Bảo hành Mekong Quận 10', 'Số 456 đường 3/2, Quận 10, TP. Hồ Chí Minh', '028.3999.888');

-- Danh mục linh kiện mẫu
INSERT INTO parts (part_code, part_name, category, unit, unit_price) VALUES
('PIN-IP15',   'Pin zin Apple iPhone 15 dung lượng 3349mAh', 'PIN',      'Cái', 850000.00),
('LCD-IP15',   'Màn hình Super Retina XDR iPhone 15',        'MAN_HINH', 'Cái', 3200000.00),
('PIN-SS-S24', 'Pin Samsung Galaxy S24 tiêu chuẩn',          'PIN',      'Cái', 720000.00),
('MH-OP-R5',   'Màn hình AMOLED Oppo Reno 5',                'MAN_HINH', 'Cái', 1450000.00),
('SAC-IP15',   'Cổng sạc USB-C flex cable iPhone 15',        'LINH_KIEN','Cái', 350000.00);

-- Số lượng tồn kho ban đầu theo trung tâm
INSERT INTO part_stock (center_id, part_id, quantity, min_threshold) VALUES
(1, 1, 15, 5),   -- Cần Thơ 1: Pin IP15 (tồn 15, ngưỡng 5 -> Đủ hàng)
(1, 2, 4,  5),   -- Cần Thơ 1: Màn hình IP15 (tồn 4, ngưỡng 5 -> CẢNH BÁO TỒN THẤP US-05)
(1, 3, 8,  3),   -- Cần Thơ 1: Pin S24 (tồn 8, ngưỡng 3 -> Đủ hàng)
(1, 4, 2,  3),   -- Cần Thơ 1: Màn hình Reno 5 (tồn 2, ngưỡng 3 -> CẢNH BÁO TỒN THẤP)
(1, 5, 6,  5),   -- Cần Thơ 1: Cổng sạc IP15 (tồn 6, ngưỡng 5 -> Đủ hàng)
(2, 1, 20, 5),   -- Quận 10: Pin IP15 (tồn 20, ngưỡng 5)
(2, 4, 0,  2);   -- Quận 10: Màn hình Reno 5 (tồn 0 -> Hết hàng AC-4.3)

-- Phiếu bảo hành mẫu (từ luồng L2)
INSERT INTO tickets (ticket_code, center_id, status, customer_name, device_model) VALUES
('BH-000456/2026', 1, 'DANG_XU_LY',    'Nguyễn Văn An',  'Apple iPhone 15'),
('BH-000457/2026', 1, 'CHO_LINH_KIEN', 'Trần Thị Mai',   'Oppo Reno 5'),
('BH-000789/2026', 1, 'DA_DONG',       'Lê Hoàng Long',  'Samsung Galaxy S24'),
('BH-000111/2026', 2, 'DANG_XU_LY',    'Phạm Quốc Huy',  'Apple iPhone 15');

-- Lịch sử giao dịch ban đầu (Append-Only Log)
INSERT INTO part_transactions (transaction_code, center_id, part_id, ticket_id, transaction_type, quantity, performed_by, note) VALUES
('GD-NK-202610-001', 1, 1, NULL, 'NHAP', 20, 'Quản lý kho - Nguyễn Trí Hải', 'Nhập kho định kỳ đầu tháng từ NPP'),
('GD-XK-202610-001', 1, 1, 1,    'XUAT', 5,  'KTV - Phạm Trí Trọng',          'Xuất thay pin cho phiếu BH-000456/2026');
