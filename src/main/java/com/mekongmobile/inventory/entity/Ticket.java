package com.mekongmobile.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Bảng tickets: Phiếu bảo hành (Dữ liệu mẫu chia sẻ từ luồng L2 theo RB-03)
 * Luồng L5 chỉ đọc dữ liệu phiếu bảo hành để kiểm tra điều kiện xuất linh kiện
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 30)
    private String ticketCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private ServiceCenter serviceCenter;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "TIEP_NHAN";

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "device_model", nullable = false, length = 100)
    private String deviceModel;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * QT-06: Kiểm tra phiếu bảo hành có ở trạng thái hợp lệ để xuất linh kiện hay không
     * Trạng thái hợp lệ: DANG_XU_LY hoặc CHO_LINH_KIEN
     */
    public boolean isValidForExport() {
        return "DANG_XU_LY".equals(this.status) || "CHO_LINH_KIEN".equals(this.status);
    }
}
