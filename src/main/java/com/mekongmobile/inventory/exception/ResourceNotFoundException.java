package com.mekongmobile.inventory.exception;

/**
 * Ngoại lệ khi không tìm thấy tài nguyên theo ID hoặc mã (HTTP 404)
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
