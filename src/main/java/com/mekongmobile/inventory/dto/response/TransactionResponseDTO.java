package com.mekongmobile.inventory.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO kết quả giao dịch kho trả về theo api-contract.md
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {

    private String transactionId;   // Mã giao dịch (GD-NK-..., GD-XK-...)
    private String transactionType; // IMPORT hoặc EXPORT
    private String partId;          // Mã linh kiện (LCD-IP15)
    private String partName;        // Tên linh kiện
    private String ticketId;        // Mã phiếu BH (nếu là EXPORT)
    private Integer quantity;       // Số lượng giao dịch
    private Integer remainingStock; // Số tồn sau khi giao dịch
    private Boolean isLowStock;     // Trạng thái cảnh báo tồn thấp sau giao dịch
    private String performedBy;     // Người thực hiện
    private String note;            // Ghi chú
    private LocalDateTime createdAt;// Thời điểm giao dịch
}
