package com.mekongmobile.inventory.controller;

import com.mekongmobile.inventory.config.StartupReadiness;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Endpoint kiểm tra tình trạng ứng dụng: GET /api/health (khớp với README.md)
 * Trả 503 trong lúc Flyway/Seeder còn đang chạy để Docker healthcheck chỉ báo
 * "healthy" khi dữ liệu mẫu đã nạp xong.
 */
@RestController
@RequestMapping({"/api/health", "/api/v1/health"})
@RequiredArgsConstructor
public class HealthController {

    private final StartupReadiness startupReadiness;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        if (!startupReadiness.isReady()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "STARTING",
                    "service", "inventory-service",
                    "module", "L5 - Kho linh kien thay the",
                    "timestamp", LocalDateTime.now()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "inventory-service",
                "module", "L5 - Kho linh kien thay the",
                "timestamp", LocalDateTime.now()
        ));
    }
}
