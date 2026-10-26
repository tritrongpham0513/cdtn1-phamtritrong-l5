package com.mekongmobile.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO yêu cầu khởi tạo/bổ sung bản ghi tồn kho cho một linh kiện tại trung tâm bảo hành (Tiêu chí S1 - CRUD)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStockRequestDTO {

    @NotBlank(message = "Mã linh kiện không được để trống")
    private String partCode;

    @NotNull(message = "Số lượng ban đầu không được để trống")
    @Min(value = 0, message = "Số lượng ban đầu phải >= 0")
    private Integer initialQuantity;

    @NotNull(message = "Ngưỡng tồn tối thiểu không được để trống")
    @Min(value = 0, message = "Ngưỡng tồn tối thiểu phải >= 0")
    private Integer minThreshold;
}
