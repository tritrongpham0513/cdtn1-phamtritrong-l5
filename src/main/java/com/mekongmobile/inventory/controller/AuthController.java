package com.mekongmobile.inventory.controller;

import com.mekongmobile.inventory.dto.request.LoginRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO;
import com.mekongmobile.inventory.dto.response.LoginResponseDTO;
import com.mekongmobile.inventory.entity.User;
import com.mekongmobile.inventory.exception.BusinessRuleException;
import com.mekongmobile.inventory.exception.ForbiddenException;
import com.mekongmobile.inventory.repository.UserRepository;
import com.mekongmobile.inventory.service.AuthTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

/**
 * Controller xử lý xác thực đăng nhập ở mức cơ bản.
 * Mật khẩu được đối chiếu với hash BCrypt lưu trong bảng users (Tiêu chí S3),
 * không còn lưu mật khẩu plaintext trong mã nguồn.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthTokenService authTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Endpoint đăng nhập cơ bản
     * Endpoint: POST http://localhost:8088/api/v1/auth/login
     * Body mẫu:
     * {
     * "username": "ktv_trong",
     * "password": "123456"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        String username = request.getUsername().trim().toLowerCase();
        User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);

        if (user == null || !Boolean.TRUE.equals(user.getIsActive())
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException(
                    "Tên đăng nhập hoặc mật khẩu không chính xác. Thử lại với ktv_trong / 123456");
        }

        // Tạo token cơ bản (Base64 chuỗi session), có kèm centerId để chống giả mạo X-Center-Id
        String rawToken = user.getUserId() + ":" + user.getUsername() + ":" + user.getRole() + ":" + user.getCenterId() + ":" + System.currentTimeMillis();
        String token = "mk_token_" + Base64.getEncoder().encodeToString(rawToken.getBytes());

        LoginResponseDTO response = LoginResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .centerId(user.getCenterId())
                .centerName(user.getCenterName())
                .token(token)
                .build();

        return ResponseEntity.ok(ApiResponseDTO.ok(response));
    }

    /**
     * Endpoint kiểm tra thông tin phiên làm việc hiện tại
     * Endpoint: GET http://localhost:8088/api/v1/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        AuthTokenService.UserPrincipal principal = authTokenService.extractUser(authHeader);
        if (principal == null) {
            throw new ForbiddenException("Từ chối truy cập (403 Forbidden): Chưa đăng nhập hoặc token không hợp lệ.");
        }

        User user = userRepository.findByUsernameIgnoreCase(principal.username()).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new ForbiddenException("Từ chối truy cập (403 Forbidden): Tài khoản trong token không tồn tại.");
        }

        LoginResponseDTO response = LoginResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .centerId(user.getCenterId())
                .centerName(user.getCenterName())
                .token(authHeader.substring(7))
                .build();

        return ResponseEntity.ok(ApiResponseDTO.ok(response));
    }
}
