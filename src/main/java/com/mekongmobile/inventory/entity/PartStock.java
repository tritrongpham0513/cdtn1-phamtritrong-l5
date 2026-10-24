package com.mekongmobile.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Bảng part_stock: Quản lý số lượng tồn kho theo từng trung tâm bảo hành
 * Phục vụ: FR-01, FR-03, FR-04, FR-07, US-01, US-04, US-05, US-08
 * Ràng buộc cốt lõi: quantity >= 0 (NFR-02, QT-09, QT-L5-03)
 */
@Entity
@Table(
    name = "part_stock",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_center_part", columnNames = {"center_id", "part_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private ServiceCenter serviceCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "min_threshold", nullable = false)
    @Builder.Default
    private Integer minThreshold = 5;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Thuộc tính suy diễn (computed) phục vụ US-05 cảnh báo tồn thấp
     */
    @Transient
    public boolean isLowStock() {
        return this.quantity != null && this.minThreshold != null && this.quantity < this.minThreshold;
    }
}
