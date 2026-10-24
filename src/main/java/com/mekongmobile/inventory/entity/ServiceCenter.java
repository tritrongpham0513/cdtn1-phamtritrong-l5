package com.mekongmobile.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Bảng service_centers: Danh mục các Trung tâm Bảo hành của Mekong Mobile
 * Phục vụ: QT-14 (phân vùng tồn kho theo trung tâm), NFR-03 (cô lập dữ liệu)
 */
@Entity
@Table(name = "service_centers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "center_id")
    private Long centerId;

    @Column(name = "center_code", nullable = false, unique = true, length = 20)
    private String centerCode;

    @Column(name = "center_name", nullable = false, length = 120)
    private String centerName;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
