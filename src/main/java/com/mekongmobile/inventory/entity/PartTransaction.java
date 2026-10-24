package com.mekongmobile.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Bảng part_transactions: Lịch sử giao dịch nhập/xuất kho (Append-Only Log)
 * Phục vụ: FR-02, FR-03, US-02, US-03, US-06, US-07, QT-13, NFR-04
 * Bất biến: Không có thao tác UPDATE/DELETE vật lý
 */
@Entity
@Table(name = "part_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transaction_code", nullable = false, unique = true, length = 30)
    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private ServiceCenter serviceCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(name = "transaction_type", nullable = false, length = 10)
    private String transactionType; // "IMPORT" hoặc "EXPORT"

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
