package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Service trích xuất và kiểm tra quyền hạn (RBAC) từ Authorization Token
 * Tuân thủ mục 2.2 api-contract.md và quy tắc phân quyền QT-14
 */
@Service
public class AuthTokenService {

    public record UserPrincipal(Long userId, String username, String role) {}

    /**
     * Bóc tách thông tin người dùng từ header Authorization
     */
    public UserPrincipal extractUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7).trim();
        if (token.startsWith("mk_token_")) {
            try {
                String b64 = token.substring("mk_token_".length());
                String decoded = new String(Base64.getDecoder().decode(b64));
                String[] parts = decoded.split(":");
                if (parts.length >= 3) {
                    return new UserPrincipal(Long.parseLong(parts[0]), parts[1], parts[2]);
                }
            } catch (Exception ignored) {
                // Token không đúng định dạng
            }
        }
        return null;
    }

    /**
     * Bắt buộc người dùng phải có đúng vai trò yêu cầu
     * Nếu không có token hoặc sai vai trò -> Ném ForbiddenException (HTTP 403)
     */
    public void requireRole(String authHeader, String requiredRole, String actionDescription) {
        UserPrincipal user = extractUser(authHeader);

        if (user == null) {
            throw new ForbiddenException(
                    String.format("Từ chối truy cập (403 Forbidden): Thao tác '%s' yêu cầu đăng nhập với vai trò '%s'. Vui lòng đăng nhập trước khi thực hiện!",
                            actionDescription, requiredRole)
            );
        }

        if (!user.role().equalsIgnoreCase(requiredRole)) {
            String roleVi = "TECHNICIAN".equalsIgnoreCase(user.role()) ? "Kỹ thuật viên" : "Quản lý kho";
            String reqRoleVi = "TECHNICIAN".equalsIgnoreCase(requiredRole) ? "Kỹ thuật viên" : "Quản lý kho";

            throw new ForbiddenException(
                    String.format("Quyền bị từ chối (403 Forbidden): Tài khoản '%s' (%s) không được phép %s. Thao tác này chỉ dành riêng cho %s!",
                            user.username(), roleVi, actionDescription, reqRoleVi)
            );
        }
    }
}
