package com.mekongmobile.inventory.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO trả về danh sách linh kiện đã dùng theo phiếu bảo hành (US-07)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketPartDTO {

    private String transactionId;
    private String partId;
    private String partName;
    private Integer quantity;
    private String performedBy;
    private LocalDateTime exportDate;
}
