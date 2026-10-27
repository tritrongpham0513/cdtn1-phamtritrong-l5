package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử kiểm soát phân quyền RBAC (AuthTokenService)")
class AuthTokenServiceTest {

    private AuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        authTokenService = new AuthTokenService();
    }

    private String generateToken(Long userId, String username, String role) {
        String raw = userId + ":" + username + ":" + role + ":" + System.currentTimeMillis();
        return "Bearer mk_token_" + Base64.getEncoder().encodeToString(raw.getBytes());
    }

    @Test
    @DisplayName("Kỹ thuật viên thực hiện Nhập kho -> Ném lỗi ForbiddenException (HTTP 403)")
    void testTechnicianCannotImport() {
        String ktvToken = generateToken(101L, "ktv_trong", "TECHNICIAN");

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            authTokenService.requireRole(ktvToken, "MANAGER", "Nhập kho linh kiện mới");
        });

        assertTrue(exception.getMessage().contains("403 Forbidden"));
        assertTrue(exception.getMessage().contains("Kỹ thuật viên"));
        assertTrue(exception.getMessage().contains("Quản lý kho"));
    }

    @Test
    @DisplayName("Quản lý kho thực hiện Xuất kho sửa chữa -> Ném lỗi ForbiddenException (HTTP 403)")
    void testManagerCannotExport() {
        String managerToken = generateToken(102L, "quanly_kho", "MANAGER");

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            authTokenService.requireRole(managerToken, "TECHNICIAN", "Xuất kho linh kiện");
        });

        assertTrue(exception.getMessage().contains("403 Forbidden"));
        assertTrue(exception.getMessage().contains("Quản lý kho"));
        assertTrue(exception.getMessage().contains("Kỹ thuật viên"));
    }

    @Test
    @DisplayName("Quản lý kho thực hiện Nhập kho -> Thành công không có ngoại lệ")
    void testManagerCanImport() {
        String managerToken = generateToken(102L, "quanly_kho", "MANAGER");

        assertDoesNotThrow(() -> {
            authTokenService.requireRole(managerToken, "MANAGER", "Nhập kho linh kiện mới");
        });
    }

    @Test
    @DisplayName("Kỹ thuật viên thực hiện Xuất kho sửa chữa -> Thành công không có ngoại lệ")
    void testTechnicianCanExport() {
        String ktvToken = generateToken(101L, "ktv_trong", "TECHNICIAN");

        assertDoesNotThrow(() -> {
            authTokenService.requireRole(ktvToken, "TECHNICIAN", "Xuất kho linh kiện");
        });
    }

    @Test
    @DisplayName("Không truyền Authorization header -> Ném lỗi ForbiddenException yêu cầu đăng nhập")
    void testMissingTokenThrowsForbidden() {
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            authTokenService.requireRole(null, "MANAGER", "Nhập kho linh kiện");
        });

        assertTrue(exception.getMessage().contains("403 Forbidden"));
        assertTrue(exception.getMessage().contains("yêu cầu đăng nhập"));
    }
}
