package com.mekongmobile.inventory.controller;

import com.mekongmobile.inventory.dto.request.ExportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.request.ImportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO.MetaDTO;
import com.mekongmobile.inventory.dto.response.TicketPartDTO;
import com.mekongmobile.inventory.dto.response.TransactionResponseDTO;
import com.mekongmobile.inventory.service.PartTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller xử lý các giao dịch nhập / xuất kho linh kiện
 * Khớp 100% với api-contract.md và Wireframe M2, M3
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final PartTransactionService transactionService;

    /**
     * 1. Ghi nhận nhập kho linh kiện từ nhà cung cấp / điều chuyển (UC-02, US-02)
     * Endpoint: POST http://localhost:8080/api/v1/transactions/import
     * Headers: X-Center-Id: 1, Content-Type: application/json
     * Body mẫu:
     * {
     *   "partCode": "PIN-IP-15",
     *   "quantity": 50,
     *   "supplier": "Apple Vietnam Authorized Supplier",
     *   "note": "Nhập kho đợt 1 tháng 10/2026"
     * }
     */
    @PostMapping("/transactions/import")
    public ResponseEntity<ApiResponseDTO<TransactionResponseDTO>> importPart(
            @RequestHeader(value = "X-Center-Id", defaultValue = "1") Long centerId,
            @Valid @RequestBody ImportTransactionRequestDTO request
    ) {
        TransactionResponseDTO response = transactionService.importPart(centerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.ok(response));
    }

    /**
     * 2. Xuất linh kiện cho phiếu sửa chữa bảo hành (UC-03, UC-04, US-03, US-04)
     * Endpoint: POST http://localhost:8080/api/v1/transactions/export
     * Headers: X-Center-Id: 1, Content-Type: application/json
     * Body mẫu:
     * {
     *   "ticketCode": "BH-CT-2026-0001",
     *   "partCode": "MH-SS-01",
     *   "quantity": 1,
     *   "technicianId": 105,
     *   "note": "Thay thế màn hình bị vỡ cho khách hàng"
     * }
     */
    @PostMapping("/transactions/export")
    public ResponseEntity<ApiResponseDTO<TransactionResponseDTO>> exportPart(
            @RequestHeader(value = "X-Center-Id", defaultValue = "1") Long centerId,
            @Valid @RequestBody ExportTransactionRequestDTO request
    ) {
        TransactionResponseDTO response = transactionService.exportPart(centerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.ok(response));
    }

    /**
     * 3. Xem sổ cái lịch sử giao dịch kho có lọc và phân trang (UC-06, US-06)
     * Endpoint: GET http://localhost:8080/api/v1/transactions
     * Headers: X-Center-Id: 1
     * Query params: ?partId=MH-SS-01&type=EXPORT&page=1&limit=20
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponseDTO<List<TransactionResponseDTO>>> getTransactions(
            @RequestHeader(value = "X-Center-Id", defaultValue = "1") Long centerId,
            @RequestParam(value = "partId", required = false) String partId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        Pair<List<TransactionResponseDTO>, MetaDTO> result = transactionService.getTransactionHistory(
                centerId, partId, type, fromDate, toDate, page, limit
        );
        return ResponseEntity.ok(ApiResponseDTO.ok(result.getFirst(), result.getSecond()));
    }

    /**
     * 4. Xem danh sách linh kiện đã dùng theo phiếu bảo hành (UC-06, US-07)
     * Endpoint: GET http://localhost:8080/api/v1/tickets/{ticketId}/parts
     * Ví dụ: GET http://localhost:8080/api/v1/tickets/BH-CT-2026-0001/parts
     */
    @GetMapping("/tickets/{ticketId}/parts")
    public ResponseEntity<ApiResponseDTO<List<TicketPartDTO>>> getPartsByTicket(
            @PathVariable("ticketId") String ticketId
    ) {
        List<TicketPartDTO> list = transactionService.getPartsByTicket(ticketId);
        return ResponseEntity.ok(ApiResponseDTO.ok(list));
    }
}
