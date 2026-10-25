package com.mekongmobile.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO yêu cầu cập nhật thông tin linh kiện trong danh mục (Tiêu chí S1 - CRUD)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePartRequestDTO {

    @NotBlank(message = "Tên linh kiện không được để trống")
    @Size(max = 150, message = "Tên linh kiện tối đa 150 ký tự")
    private String partName;

    @NotBlank(message = "Danh mục linh kiện không được để trống")
    @Size(max = 50, message = "Danh mục tối đa 50 ký tự")
    private String category;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 20, message = "Đơn vị tính tối đa 20 ký tự")
    private String unit;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá phải >= 0")
    private BigDecimal unitPrice;

    private Boolean isActive;
}
