package com.mekongmobile.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO yêu cầu thêm mới linh kiện vào danh mục chuẩn hóa (Tiêu chí S1 - CRUD)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePartRequestDTO {

    @NotBlank(message = "Mã linh kiện không được để trống")
    @Size(max = 30, message = "Mã linh kiện tối đa 30 ký tự")
    private String partCode;

    @NotBlank(message = "Tên linh kiện không được để trống")
    @Size(max = 150, message = "Tên linh kiện tối đa 150 ký tự")
    private String partName;

    @NotBlank(message = "Danh mục linh kiện không được để trống")
    @Size(max = 50, message = "Danh mục tối đa 50 ký tự")
    private String category;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 20, message = "Đơn vị tính tối đa 20 ký tự")
    @Builder.Default
    private String unit = "Cái";

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá phải >= 0")
    private BigDecimal unitPrice;
}
