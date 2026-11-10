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

    public record UserPrincipal(Long userId, String username, String role, Long centerId) {}

    /**
     * Bóc tách thông tin người dùng từ header Authorization.
     * Định dạng token hiện tại: userId:username:role:centerId:issuedAt
     * Token cũ 4 phần (userId:username:role:issuedAt) vẫn được đọc nhưng không có centerId.
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
                if (parts.length >= 5) {
                    return new UserPrincipal(
                            Long.parseLong(parts[0]),
                            parts[1],
                            parts[2],
                            Long.parseLong(parts[3])
                    );
                }
                if (parts.length >= 3) {
                    // Token cũ không chứa centerId; centerId = null sẽ bị từ chối ở requireRoleAndCenter.
                    return new UserPrincipal(Long.parseLong(parts[0]), parts[1], parts[2], null);
                }
            } catch (Exception ignored) {
                // Token không đúng định dạng
            }
        }
        return null;
    }

    /**
     * Bắt buộc người dùng phải có đúng vai trò yêu cầu và trả về principal.
     * Nếu không có token hoặc sai vai trò -> Ném ForbiddenException (HTTP 403)
     */
    public UserPrincipal requireUser(String authHeader, String requiredRole, String actionDescription) {
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

        return user;
    }

    /**
     * Bắt buộc vai trò và ràng buộc trung tâm thao tác với trung tâm ghi trong token.
     * Đây là lớp phòng thủ A01: client không thể đổi X-Center-Id để thao tác sang trung tâm khác.
     */
    public UserPrincipal requireRoleAndCenter(
            String authHeader,
            String requiredRole,
            Long requestedCenterId,
            String actionDescription
    ) {
        UserPrincipal user = requireUser(authHeader, requiredRole, actionDescription);

        if (user.centerId() == null) {
            throw new ForbiddenException(
                    "Từ chối truy cập (403 Forbidden): Token không chứa thông tin trung tâm. Vui lòng đăng nhập lại."
            );
        }

        if (requestedCenterId != null && !user.centerId().equals(requestedCenterId)) {
            throw new ForbiddenException(
                    String.format("Từ chối truy cập (403 Forbidden): X-Center-Id=%d không khớp với trung tâm %d của tài khoản '%s'.",
                            requestedCenterId, user.centerId(), user.username())
            );
        }

        return user;
    }

    /**
     * Bắt buộc người dùng phải có đúng vai trò yêu cầu
     * Nếu không có token hoặc sai vai trò -> Ném ForbiddenException (HTTP 403)
     */
    public void requireRole(String authHeader, String requiredRole, String actionDescription) {
        requireUser(authHeader, requiredRole, actionDescription);
    }
}
