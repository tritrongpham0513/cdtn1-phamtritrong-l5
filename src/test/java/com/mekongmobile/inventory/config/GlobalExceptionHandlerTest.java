package com.mekongmobile.inventory.config;

import com.mekongmobile.inventory.dto.response.ApiResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Kiểm thử hồi quy BUG-BT3-03: phản hồi 500 không được lộ chi tiết nội bộ
 * như tên bảng, chuỗi kết nối hay stack trace (tiêu chí S4).
 */
@DisplayName("Kiểm thử GlobalExceptionHandler — không lộ chi tiết nội bộ")
class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("BUG-BT3-03 (S4): Exception chung trả 500 với thông báo trung tính, không lộ tên bảng/chuỗi kết nối")
    void testGenericException_DoesNotLeakInternalDetails() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException internal = new RuntimeException(
                "Table 'part_stock' doesn't exist; jdbc:mysql://localhost:3306/inventory"
        );

        ResponseEntity<ApiResponseDTO<Void>> response = handler.handleGenericException(internal);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getError());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError().getCode());

        String message = response.getBody().getError().getMessage();
        assertFalse(message.contains("part_stock"), "Không được lộ tên bảng");
        assertFalse(message.contains("jdbc:mysql"), "Không được lộ chuỗi kết nối");
        assertFalse(message.contains("localhost"), "Không được lộ địa chỉ máy chủ");
    }
}
