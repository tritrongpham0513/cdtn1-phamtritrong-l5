package com.mekongmobile.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Bảng users: Tài khoản đăng nhập của nhân viên Mekong Mobile.
 * Mật khẩu chỉ lưu dạng băm BCrypt (Tiêu chí S3: không đọc được mật khẩu gốc).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "center_id", nullable = false)
    private Long centerId;

    @Column(name = "center_name", nullable = false, length = 120)
    private String centerName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
