package com.mekongmobile.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO yêu cầu ghi nhận nhập kho: POST /api/v1/transactions/import (api-contract.md)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportTransactionRequestDTO {

    @NotBlank(message = "Mã linh kiện không được để trống")
    private String partId; // part_code

    @NotNull(message = "Số lượng nhập không được để trống")
    @Min(value = 1, message = "Số lượng nhập phải là số nguyên dương > 0")
    private Integer quantity;

    @NotBlank(message = "Người thực hiện không được để trống")
    private String performedBy;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;
}
