package com.mekongmobile.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO yêu cầu đăng nhập cơ bản (Thành phần 3 - Tiêu chí Track SE)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
