package com.mekongmobile.inventory.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin linh kiện chuẩn hóa trong danh mục
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartResponseDTO {

    private Long partId;
    private String partCode;
    private String partName;
    private String category;
    private String unit;
    private BigDecimal unitPrice;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
