package com.mekongmobile.inventory.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dữ liệu tồn kho linh kiện trả về qua GET /api/v1/stock (api-contract.md)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockItemDTO {

    private String partId;         // part_code (ví dụ: LCD-IP15)
    private String partName;       // Tên linh kiện
    private String category;       // Phân loại linh kiện
    private String unit;           // Đơn vị tính (Cái)
    private BigDecimal unitPrice;  // Đơn giá tham chiếu
    private Integer quantity;      // Số lượng tồn kho hiện tại (>= 0)
    private Integer minThreshold;  // Ngưỡng tồn tối thiểu
    private Boolean isLowStock;    // Cảnh báo tồn thấp (quantity < minThreshold)
    private LocalDateTime updatedAt;
}
