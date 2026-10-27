package com.mekongmobile.inventory.controller;

import com.mekongmobile.inventory.dto.request.CreateStockRequestDTO;
import com.mekongmobile.inventory.dto.request.UpdateThresholdRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO.MetaDTO;
import com.mekongmobile.inventory.dto.response.StockItemDTO;
import com.mekongmobile.inventory.exception.ResourceNotFoundException;
import com.mekongmobile.inventory.service.StockAlertService;
import com.mekongmobile.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý tồn kho linh kiện: /api/v1/stock
 * Đảm bảo đủ 4 thao tác CRUD theo Tiêu chí S1 (Track SE)
 */
@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockAlertService stockAlertService;
    private final com.mekongmobile.inventory.service.AuthTokenService authTokenService;

    /**
     * 1. Tra cứu danh sách tồn kho linh kiện theo trung tâm bảo hành (Read list - UC-01, UC-05)
     * Endpoint: GET http://localhost:8080/api/v1/stock
     * Headers: X-Center-Id: 1 (1: TTBH Cần Thơ, 2: TTBH Vĩnh Long,...)
     * Query params: ?keyword=Samsung&isLowStock=false&page=1&limit=20
     */
    @GetMapping("")
    public ResponseEntity<ApiResponseDTO<List<StockItemDTO>>> getStock(
            @RequestHeader(value = "X-Center-Id", defaultValue = "1") Long centerId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "isLowStock", defaultValue = "false") boolean isLowStock,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        Pair<List<StockItemDTO>, MetaDTO> result = stockService.getStockList(centerId, keyword, isLowStock, page, limit);
        return ResponseEntity.ok(ApiResponseDTO.ok(result.getFirst(), result.getSecond()));
    }

    /**
     * 2. Tra cứu chi tiết tồn kho một linh kiện tại trung tâm (Read single)
     * Endpoint: GET http://localhost:8080/api/v1/stock/{partCode}
     * Headers: X-Center-Id: 1
     * Ví dụ: GET http://localhost:8080/api/v1/stock/MH-SS-01
     */
    @GetMapping("/{partCode}")
    public ResponseEntity<ApiResponseDTO<StockItemDTO>> getStockItem(
            @RequestHeader(value = "X-Center-Id", defaultValue = "1") Long centerId,
            @PathVariable("partCode") String partCode
    ) {
        StockItemDTO item = stockService.getStockItem(centerId, partCode);
        if (item == null) {
            throw new ResourceNotFoundException("Không tìm thấy linh kiện '" + partCode + "' trong kho trung tâm " + centerId);
        }
        return ResponseEntity.ok(ApiResponseDTO.ok(item));
    }

    /**
     * 3. Khởi tạo bản ghi tồn kho cho linh kiện tại trung tâm (Create)
     * Endpoint: POST http://localhost:8080/api/v1/stock
     * Headers: Authorization: Bearer <token>, X-Center-Id: 1, Content-Type: application/json
     * Body mẫu:
     * {
     *   "partCode": "PIN-IP-15",
     *   "initialQuantity": 20,
     *   "minThreshold": 5
     * }
     */
    @PostMapping("")
    public ResponseEntity<ApiResponseDTO<StockItemDTO>> createStock(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Center-Id", defaultValue = "1") Long centerId,
            @Valid @RequestBody CreateStockRequestDTO request
    ) {
        authTokenService.requireRole(authHeader, "MANAGER", "Khởi tạo bản ghi tồn kho mới");
        StockItemDTO created = stockService.createStock(centerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.ok(created));
    }

    /**
     * 4. Thiết lập / điều chỉnh ngưỡng tồn tối thiểu (Update - UC-07, US-08)
     * Endpoint: PUT http://localhost:8080/api/v1/stock/threshold
     * Headers: Authorization: Bearer <token>, X-Center-Id: 1, Content-Type: application/json
     * Body mẫu:
     * {
     *   "partCode": "MH-SS-01",
     *   "minThreshold": 10
     * }
     */
    @PutMapping("/threshold")
    public ResponseEntity<ApiResponseDTO<StockItemDTO>> updateThreshold(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Center-Id", defaultValue = "1") Long centerId,
            @Valid @RequestBody UpdateThresholdRequestDTO request
    ) {
        authTokenService.requireRole(authHeader, "MANAGER", "Điều chỉnh ngưỡng định mức tồn tối thiểu");
        StockItemDTO updated = stockAlertService.updateMinThreshold(centerId, request);
        return ResponseEntity.ok(ApiResponseDTO.ok(updated));
    }

    /**
     * 5. Xóa bản ghi tồn kho khi số lượng tồn bằng 0 (Delete)
     * Endpoint: DELETE http://localhost:8080/api/v1/stock/{partCode}
     * Headers: Authorization: Bearer <token>, X-Center-Id: 1
     * Ví dụ: DELETE http://localhost:8080/api/v1/stock/MH-SS-01
     */
    @DeleteMapping("/{partCode}")
    public ResponseEntity<ApiResponseDTO<String>> deleteStock(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Center-Id", defaultValue = "1") Long centerId,
            @PathVariable("partCode") String partCode
    ) {
        authTokenService.requireRole(authHeader, "MANAGER", "Xóa bản ghi tồn kho");
        stockService.deleteStock(centerId, partCode);
        return ResponseEntity.ok(ApiResponseDTO.ok("Xóa bản ghi tồn kho " + partCode + " thành công"));
    }
}
