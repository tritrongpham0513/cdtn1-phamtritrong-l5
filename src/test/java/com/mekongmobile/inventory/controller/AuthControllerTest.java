package com.mekongmobile.inventory.controller;

import com.mekongmobile.inventory.dto.response.ApiResponseDTO;
import com.mekongmobile.inventory.dto.response.LoginResponseDTO;
import com.mekongmobile.inventory.service.AuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Kiểm thử hồi quy BUG-BT3-02: /api/v1/auth/me phải trả đúng danh tính từ token,
 * không được hard-code tài khoản ktv_trong cho mọi phiên đăng nhập.
 */
@DisplayName("Kiểm thử AuthController — danh tính phiên làm việc")
class AuthControllerTest {

    private AuthTokenService authTokenService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authTokenService = new AuthTokenService();
        authController = new AuthController(authTokenService);
    }

    private String generateToken(Long userId, String username, String role, Long centerId) {
        String raw = userId + ":" + username + ":" + role + ":" + centerId + ":" + System.currentTimeMillis();
        return "Bearer mk_token_" + Base64.getEncoder().encodeToString(raw.getBytes());
    }

    @Test
    @DisplayName("BUG-BT3-02: Token của quản lý kho phải trả về đúng MANAGER, không phải TECHNICIAN")
    void testGetCurrentUser_ReturnsIdentityFromToken() {
        String managerToken = generateToken(102L, "quanly_kho", "MANAGER", 1L);

        ResponseEntity<ApiResponseDTO<LoginResponseDTO>> response =
                authController.getCurrentUser(managerToken);

        assertNotNull(response.getBody());
        LoginResponseDTO data = response.getBody().getData();
        assertEquals(102L, data.getUserId());
        assertEquals("quanly_kho", data.getUsername());
        assertEquals("MANAGER", data.getRole());
        assertEquals(1L, data.getCenterId());
    }
}
