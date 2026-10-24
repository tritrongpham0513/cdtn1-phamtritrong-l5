package com.mekongmobile.inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Endpoint kiểm tra tình trạng ứng dụng: GET /api/health (khớp với README.md)
 */
@RestController
@RequestMapping({"/api/health", "/api/v1/health"})
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "inventory-service",
                "module", "L5 - Kho linh kien thay the",
                "timestamp", LocalDateTime.now()
        ));
    }
}
