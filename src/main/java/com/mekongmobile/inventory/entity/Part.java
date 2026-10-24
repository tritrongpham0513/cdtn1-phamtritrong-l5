package com.mekongmobile.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bảng parts: Danh mục các loại linh kiện thay thế chuẩn hóa
 * Phục vụ: FR-01, FR-02, US-01, AC-2.3
 */
@Entity
@Table(name = "parts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_id")
    private Long partId;

    @Column(name = "part_code", nullable = false, unique = true, length = 30)
    private String partCode;

    @Column(name = "part_name", nullable = false, length = 150)
    private String partName;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "unit", nullable = false, length = 20)
    @Builder.Default
    private String unit = "Cái";

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
