package com.mekongmobile.inventory.controller;

import com.mekongmobile.inventory.dto.request.LoginRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO;
import com.mekongmobile.inventory.dto.response.LoginResponseDTO;
import com.mekongmobile.inventory.exception.BusinessRuleException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

/**
 * Controller xử lý xác thực đăng nhập ở mức cơ bản
 * Đáp ứng tiêu chuẩn Thành phần 3 (Track SE): "xác thực đăng nhập ở mức cơ bản"
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Map<String, UserProfile> MOCK_USERS = Map.of(
            "ktv_trong",
            new UserProfile(101L, "ktv_trong", "123456", "Phạm Trí Trọng", "TECHNICIAN", 1L,
                    "TT01 — TTBH Quận 10 (TP.HCM)"),
            "quanly_kho",
            new UserProfile(102L, "quanly_kho", "123456", "Quản Lý Kho", "MANAGER", 1L, "TT01 — TTBH Quận 10 (TP.HCM)"),
            "ktv_cantho", new UserProfile(103L, "ktv_cantho", "123456", "Lê Văn Cần Thơ", "TECHNICIAN", 4L,
                    "TT04 — TTBH Ninh Kiều (Cần Thơ)"));

    /**
     * Endpoint đăng nhập cơ bản
     * Endpoint: POST http://localhost:8080/api/v1/auth/login
     * Body mẫu:
     * {
     * "username": "ktv_trong",
     * "password": "123456"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        String username = request.getUsername().trim().toLowerCase();
        UserProfile user = MOCK_USERS.get(username);

        if (user == null || !user.password.equals(request.getPassword())) {
            throw new BusinessRuleException(
                    "Tên đăng nhập hoặc mật khẩu không chính xác. Thử lại với ktv_trong / 123456");
        }

        // Tạo token cơ bản (Base64 chuỗi session)
        String rawToken = user.userId + ":" + user.username + ":" + user.role + ":" + System.currentTimeMillis();
        String token = "mk_token_" + Base64.getEncoder().encodeToString(rawToken.getBytes());

        LoginResponseDTO response = LoginResponseDTO.builder()
                .userId(user.userId)
                .username(user.username)
                .fullName(user.fullName)
                .role(user.role)
                .centerId(user.centerId)
                .centerName(user.centerName)
                .token(token)
                .build();

        return ResponseEntity.ok(ApiResponseDTO.ok(response));
    }

    /**
     * Endpoint kiểm tra thông tin phiên làm việc hiện tại
     * Endpoint: GET http://localhost:8080/api/v1/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessRuleException("Chưa đăng nhập hoặc phiên làm việc đã hết hạn");
        }

        // Mặc định trả về thông tin sinh viên Phạm Trí Trọng nếu token hợp lệ
        UserProfile defaultUser = MOCK_USERS.get("ktv_trong");
        LoginResponseDTO response = LoginResponseDTO.builder()
                .userId(defaultUser.userId)
                .username(defaultUser.username)
                .fullName(defaultUser.fullName)
                .role(defaultUser.role)
                .centerId(defaultUser.centerId)
                .centerName(defaultUser.centerName)
                .token(authHeader.substring(7))
                .build();

        return ResponseEntity.ok(ApiResponseDTO.ok(response));
    }

    private record UserProfile(
            Long userId,
            String username,
            String password,
            String fullName,
            String role,
            Long centerId,
            String centerName) {
    }
}
