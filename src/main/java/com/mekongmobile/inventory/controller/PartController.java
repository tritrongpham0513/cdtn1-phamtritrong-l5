package com.mekongmobile.inventory.controller;

import com.mekongmobile.inventory.dto.request.CreatePartRequestDTO;
import com.mekongmobile.inventory.dto.request.UpdatePartRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO.MetaDTO;
import com.mekongmobile.inventory.dto.response.PartResponseDTO;
import com.mekongmobile.inventory.service.PartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý danh mục linh kiện chuẩn hóa: /api/v1/parts
 * Hiện thực đầy đủ 4 thao tác CRUD theo Tiêu chí S1 (Track SE)
 */
@RestController
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    /**
     * 1. Thêm mới linh kiện vào danh mục (Create)
     * Endpoint: POST http://localhost:8080/api/v1/parts
     * Header: Content-Type: application/json
     * Body mẫu:
     * {
     *   "partCode": "PIN-IP-15",
     *   "partName": "Pin iPhone 15 Pro Max chính hãng",
     *   "category": "PIN",
     *   "unit": "Cái",
     *   "unitPrice": 1250000.00
     * }
     */
    @PostMapping("")
    public ResponseEntity<ApiResponseDTO<PartResponseDTO>> createPart(
            @Valid @RequestBody CreatePartRequestDTO request
    ) {
        PartResponseDTO response = partService.createPart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.ok(response));
    }

    /**
     * 2. Tra cứu danh sách linh kiện trong danh mục có lọc và phân trang (Read list)
     * Endpoint: GET http://localhost:8080/api/v1/parts
     * Query params: ?keyword=Apple&category=MAN_HINH&page=1&limit=20
     */
    @GetMapping("")
    public ResponseEntity<ApiResponseDTO<List<PartResponseDTO>>> getParts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        Pair<List<PartResponseDTO>, MetaDTO> result = partService.getParts(keyword, category, page, limit);
        return ResponseEntity.ok(ApiResponseDTO.ok(result.getFirst(), result.getSecond()));
    }

    /**
     * 3. Tra cứu chi tiết một linh kiện theo mã linh kiện (Read single)
     * Endpoint: GET http://localhost:8080/api/v1/parts/{partCode}
     * Ví dụ: GET http://localhost:8080/api/v1/parts/CAM-AP-01
     */
    @GetMapping("/{partCode}")
    public ResponseEntity<ApiResponseDTO<PartResponseDTO>> getPartByCode(
            @PathVariable("partCode") String partCode
    ) {
        PartResponseDTO response = partService.getPartByCode(partCode);
        return ResponseEntity.ok(ApiResponseDTO.ok(response));
    }

    /**
     * 4. Cập nhật thông tin linh kiện theo mã linh kiện (Update)
     * Endpoint: PUT http://localhost:8080/api/v1/parts/{partCode}
     * Ví dụ: PUT http://localhost:8080/api/v1/parts/CAM-AP-01
     * Body mẫu:
     * {
     *   "partName": "Camera sau Apple loại 1 (Cập nhật)",
     *   "category": "CAMERA",
     *   "unit": "Cái",
     *   "unitPrice": 450000.00,
     *   "isActive": true
     * }
     */
    @PutMapping("/{partCode}")
    public ResponseEntity<ApiResponseDTO<PartResponseDTO>> updatePart(
            @PathVariable("partCode") String partCode,
            @Valid @RequestBody UpdatePartRequestDTO request
    ) {
        PartResponseDTO response = partService.updatePart(partCode, request);
        return ResponseEntity.ok(ApiResponseDTO.ok(response));
    }

    /**
     * 5. Xóa linh kiện khỏi danh mục (Delete - Soft delete)
     * Endpoint: DELETE http://localhost:8080/api/v1/parts/{partCode}
     * Ví dụ: DELETE http://localhost:8080/api/v1/parts/CAM-AP-01
     */
    @DeleteMapping("/{partCode}")
    public ResponseEntity<ApiResponseDTO<String>> deletePart(
            @PathVariable("partCode") String partCode
    ) {
        partService.deletePart(partCode);
        return ResponseEntity.ok(ApiResponseDTO.ok("Xóa linh kiện " + partCode + " thành công"));
    }
}
