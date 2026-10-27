package com.mekongmobile.inventory.dto.response;

import lombok.*;

/**
 * DTO phản hồi đăng nhập cơ bản kèm token phiên làm việc (Thành phần 3 - Tiêu chí Track SE)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private Long userId;
    private String username;
    private String fullName;
    private String role; // TECHNICIAN, MANAGER
    private Long centerId;
    private String centerName;
    private String token;
}
