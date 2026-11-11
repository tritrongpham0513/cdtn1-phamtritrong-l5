package com.mekongmobile.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cấu hình băm mật khẩu BCrypt (Tiêu chí S3: mật khẩu trong CSDL không đọc được).
 * Chỉ dùng spring-security-crypto, không bật toàn bộ Spring Security filter chain
 * để giữ nguyên cơ chế Bearer token hiện có.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
