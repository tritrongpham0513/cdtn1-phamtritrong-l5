package com.mekongmobile.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mekongmobile.inventory.config.GlobalExceptionHandler;
import com.mekongmobile.inventory.dto.request.ExportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.request.ImportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.response.TransactionResponseDTO;
import com.mekongmobile.inventory.service.AuthTokenService;
import com.mekongmobile.inventory.service.PartTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * =============================================================================
 * BỘ KIỂM THỬ TẦNG REST API QUA MOCKMVC (THÀNH PHẦN 2 & TIÊU CHÍ S2, C7)
 * 
 * Kiểm tra trọn vẹn luồng HTTP từ Controller qua GlobalExceptionHandler:
 * 1. TC-API-01: POST /api/v1/transactions/export hợp lệ -> HTTP 201 Created.
 * 2. TC-API-02: Dữ liệu sai (quantity = 0 vi phạm @Min(1)) -> HTTP 422 Unprocessable Entity.
 * 3. TC-API-03: Bảo mật RBAC / OWASP A01 (KTV cố nhập kho) -> HTTP 403 Forbidden.
 * =============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử tầng REST API - TransactionController (MockMvc)")
public class TransactionControllerApiTest {

    private MockMvc mockMvc;

    @Mock
    private PartTransactionService transactionService;

    @Spy
    private AuthTokenService authTokenService = new AuthTokenService();

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String generateToken(Long userId, String username, String role) {
        String raw = userId + ":" + username + ":" + role + ":1:" + System.currentTimeMillis();
        return "Bearer mk_token_" + Base64.getEncoder().encodeToString(raw.getBytes());
    }

    @Test
    @DisplayName("TC-API-01 (Happy Path): Xuất linh kiện hợp lệ qua API -> Trả về HTTP 201 Created và envelope ApiResponseDTO")
    void testExportPart_ApiSuccess() throws Exception {
        String techToken = generateToken(101L, "ktv_trong", "TECHNICIAN");

        ExportTransactionRequestDTO request = ExportTransactionRequestDTO.builder()
                .ticketId("BH-000022/2025")
                .partId("MH-SS-01")
                .quantity(1)
                .performedBy("ktv_trong")
                .note("Thay màn hình theo phiếu")
                .build();

        TransactionResponseDTO mockResponse = TransactionResponseDTO.builder()
                .transactionId("GD-XK-20261109-001")
                .transactionType("EXPORT")
                .ticketId("BH-000022/2025")
                .partId("MH-SS-01")
                .partName("Màn hình Samsung Galaxy S22")
                .quantity(1)
                .remainingStock(9)
                .isLowStock(false)
                .performedBy("ktv_trong")
                .note("Thay màn hình theo phiếu")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionService.exportPart(eq(1L), any(ExportTransactionRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/transactions/export")
                        .header("Authorization", techToken)
                        .header("X-Center-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionType").value("EXPORT"))
                .andExpect(jsonPath("$.data.partId").value("MH-SS-01"))
                .andExpect(jsonPath("$.data.remainingStock").value(9));
    }

    @Test
    @DisplayName("TC-API-02 (Negative Data): Số lượng xuất = 0 vi phạm validation @Min(1) -> Trả về HTTP 422 VALIDATION_ERROR")
    void testExportPart_ValidationError_ZeroQuantity() throws Exception {
        String techToken = generateToken(101L, "ktv_trong", "TECHNICIAN");

        ExportTransactionRequestDTO invalidRequest = ExportTransactionRequestDTO.builder()
                .ticketId("BH-000001/2025")
                .partId("MH-SS-01")
                .quantity(0) // Vi phạm @Min(1)
                .performedBy("ktv_trong")
                .build();

        mockMvc.perform(post("/api/v1/transactions/export")
                        .header("Authorization", techToken)
                        .header("X-Center-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.quantity").exists());
    }

    @Test
    @DisplayName("TC-API-03 (Bảo mật RBAC - Tiêu chí S2 / OWASP A01): Kỹ thuật viên gọi API Nhập kho của Quản lý -> Trả về HTTP 403 Forbidden")
    void testImportPart_ForbiddenForTechnician() throws Exception {
        String techToken = generateToken(101L, "ktv_trong", "TECHNICIAN");

        ImportTransactionRequestDTO importRequest = ImportTransactionRequestDTO.builder()
                .partId("PIN-SS-01")
                .quantity(50)
                .performedBy("ktv_trong")
                .note("Nhập kho trái quyền hạn")
                .build();

        mockMvc.perform(post("/api/v1/transactions/import")
                        .header("Authorization", techToken)
                        .header("X-Center-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(importRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("403 Forbidden")));
    }
}

