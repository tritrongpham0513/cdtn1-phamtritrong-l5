-- =============================================================================
-- TRƯỜNG ĐẠI HỌC VĂN LANG · KHOA CÔNG NGHỆ THÔNG TIN
-- CHUYÊN ĐỀ TỐT NGHIỆP 1 · HỌC KỲ 1, NĂM HỌC 2026 – 2027
-- BÀI TẬP 3: BỔ SUNG BẢNG users VỚI MẬT KHẨU BĂM BCRYPT (MIGRATION V2)
--
-- Sinh viên: Phạm Trí Trọng – MSSV: 2374802010525
-- Luồng nghiệp vụ: L5 – Kho linh kiện thay thế (Smart CRM Mekong Mobile)
-- Mục đích: Đáp ứng tiêu chí S3 - mật khẩu trong CSDL là chuỗi băm, không đọc được.
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    center_id     BIGINT       NOT NULL,
    center_name   VARCHAR(120) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('TECHNICIAN', 'MANAGER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Mật khẩu demo "123456" được băm bằng BCrypt (cost 10). Không lưu plaintext.
INSERT IGNORE INTO users (username, password_hash, full_name, role, center_id, center_name) VALUES
('ktv_trong',  '$2a$10$vZedEyVQhGGeSrJ/NVomhe/SkYC5XDBXCYnO7L3k1koBk9Rfko46i', 'Phạm Trí Trọng', 'TECHNICIAN', 1, 'TT01 — TTBH Quận 10 (TP.HCM)'),
('quanly_kho', '$2a$10$tJWfTe4YQTGyCoErEIuS8uzZa5yeMvXnD.S83abF5VDQlztOVNPCC', 'Quản Lý Kho',   'MANAGER',    1, 'TT01 — TTBH Quận 10 (TP.HCM)'),
('ktv_cantho', '$2a$10$3lfNK7DaBsTS8iEo6iXpZOKIKq5V05thVuVMazTWHeas4HOkWAc.6', 'Lê Văn Cần Thơ', 'TECHNICIAN', 4, 'TT04 — TTBH Ninh Kiều (Cần Thơ)');
