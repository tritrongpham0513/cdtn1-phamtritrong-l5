package com.mekongmobile.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO yêu cầu cập nhật ngưỡng tối thiểu: PUT /api/v1/stock/threshold (api-contract.md)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateThresholdRequestDTO {

    @NotBlank(message = "Mã linh kiện không được để trống")
    private String partId; // part_code

    @NotNull(message = "Ngưỡng tối thiểu không được để trống")
    @Min(value = 0, message = "Ngưỡng tối thiểu phải là số nguyên không âm (>= 0)")
    private Integer minThreshold;

    @Size(max = 255, message = "Lý do điều chỉnh không được vượt quá 255 ký tự")
    private String reason;
}
