package com.mekongmobile.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO yêu cầu xuất linh kiện cho phiếu BH: POST /api/v1/transactions/export (api-contract.md)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportTransactionRequestDTO {

    @NotBlank(message = "Mã phiếu bảo hành không được để trống")
    private String ticketId; // ticket_code

    @NotBlank(message = "Mã linh kiện không được để trống")
    private String partId; // part_code

    @NotNull(message = "Số lượng xuất không được để trống")
    @Min(value = 1, message = "Số lượng xuất phải là số nguyên dương > 0")
    private Integer quantity;

    @NotBlank(message = "Người thực hiện không được để trống")
    private String performedBy;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;
}
