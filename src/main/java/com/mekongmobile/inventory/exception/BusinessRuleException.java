package com.mekongmobile.inventory.exception;

/**
 * Ngoại lệ khi vi phạm quy tắc nghiệp vụ kho linh kiện (HTTP 409 hoặc 400)
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
