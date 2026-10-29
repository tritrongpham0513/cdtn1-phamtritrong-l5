-- =============================================================================
-- TRƯỜNG ĐẠI HỌC VĂN LANG · KHOA CÔNG NGHỆ THÔNG TIN
-- CHUYÊN ĐỀ TỐT NGHIỆP 1 · HỌC KỲ 1, NĂM HỌC 2026 – 2027
-- BÀI TẬP 2: HIỆN THỰC HÓA MODULE CỐT LÕI (MIGRATION V1)
-- 
-- Sinh viên: Phạm Trí Trọng – MSSV: 2374802010525
-- Luồng nghiệp vụ: L5 – Kho linh kiện thay thế (Smart CRM Mekong Mobile)
-- File: V1__init_schema.sql (Hệ quản trị CSDL: MySQL 8.x)
-- =============================================================================

-- 1. BẢNG service_centers: Danh mục các Trung tâm Bảo hành của Mekong Mobile
CREATE TABLE IF NOT EXISTS service_centers (
    center_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    center_code   VARCHAR(20)  NOT NULL UNIQUE,
    center_name   VARCHAR(120) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG parts: Danh mục các loại linh kiện thay thế chuẩn hóa
CREATE TABLE IF NOT EXISTS parts (
    part_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_code     VARCHAR(30)   NOT NULL UNIQUE,
    part_name     VARCHAR(150)  NOT NULL,
    category      VARCHAR(50)   NOT NULL,
    unit          VARCHAR(20)   NOT NULL DEFAULT 'Cái',
    unit_price    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_parts_price CHECK (unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG part_stock: Quản lý số lượng tồn kho theo từng trung tâm bảo hành
CREATE TABLE IF NOT EXISTS part_stock (
    stock_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    center_id     BIGINT NOT NULL,
    part_id       BIGINT NOT NULL,
    quantity      INT    NOT NULL DEFAULT 0,
    min_threshold INT    NOT NULL DEFAULT 5,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_stock_center FOREIGN KEY (center_id) REFERENCES service_centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT fk_stock_part   FOREIGN KEY (part_id)   REFERENCES parts(part_id) ON DELETE RESTRICT,
    CONSTRAINT uk_stock_center_part UNIQUE (center_id, part_id),
    CONSTRAINT chk_stock_qty_positive CHECK (quantity >= 0),
    CONSTRAINT chk_stock_threshold_positive CHECK (min_threshold >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BẢNG tickets: Bảng phiếu bảo hành (Dữ liệu mẫu chia sẻ từ luồng L2)
CREATE TABLE IF NOT EXISTS tickets (
    ticket_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_code   VARCHAR(30)  NOT NULL UNIQUE,
    center_id     BIGINT       NOT NULL,
    status        VARCHAR(30)  NOT NULL DEFAULT 'TIEP_NHAN',
    customer_name VARCHAR(120) NOT NULL,
    device_model  VARCHAR(100) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_tickets_center FOREIGN KEY (center_id) REFERENCES service_centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT chk_tickets_status CHECK (status IN ('TIEP_NHAN', 'DA_PHAN_CONG', 'DANG_XU_LY', 'CHO_LINH_KIEN', 'HOAN_TAT', 'DA_DONG'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG part_transactions: Lịch sử giao dịch nhập/xuất kho (Append-Only Log)
CREATE TABLE IF NOT EXISTS part_transactions (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_code VARCHAR(30)  NOT NULL UNIQUE,
    center_id        BIGINT       NOT NULL,
    part_id          BIGINT       NOT NULL,
    ticket_id        BIGINT       NULL,
    transaction_type VARCHAR(10)  NOT NULL,
    quantity         INT          NOT NULL,
    performed_by     VARCHAR(100) NOT NULL,
    note             VARCHAR(255) NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_tx_center FOREIGN KEY (center_id) REFERENCES service_centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_part   FOREIGN KEY (part_id)   REFERENCES parts(part_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE RESTRICT,
    CONSTRAINT chk_tx_type  CHECK (transaction_type IN ('IMPORT', 'EXPORT')),
    CONSTRAINT chk_tx_qty   CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. CHỈ MỤC TỐI ƯU HIỆU NĂNG NFR-01 (Thời gian phản hồi <= 500ms)
CREATE INDEX idx_stock_center_part ON part_stock(center_id, part_id);
CREATE INDEX idx_stock_low ON part_stock(center_id, quantity, min_threshold);
CREATE INDEX idx_tx_search ON part_transactions(center_id, transaction_type, created_at);
CREATE INDEX idx_tx_ticket ON part_transactions(ticket_id);
CREATE INDEX idx_tickets_center_status ON tickets(center_id, status);
