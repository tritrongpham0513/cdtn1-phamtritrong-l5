package com.mekongmobile.inventory.exception;

/**
 * Ngoại lệ từ chối truy cập (HTTP 403 Forbidden)
 * Tuân thủ quy tắc phân quyền RBAC (api-contract.md Mục 2.2 và QT-14)
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
