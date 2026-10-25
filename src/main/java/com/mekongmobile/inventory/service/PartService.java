package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.dto.request.CreatePartRequestDTO;
import com.mekongmobile.inventory.dto.request.UpdatePartRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO.MetaDTO;
import com.mekongmobile.inventory.dto.response.PartResponseDTO;
import org.springframework.data.util.Pair;

import java.util.List;

/**
 * Service quản lý danh mục linh kiện chuẩn hóa (Tiêu chí S1 - CRUD)
 * Tuân thủ S2: Thuần túy nghiệp vụ, cách ly hoàn toàn khỏi tầng giao vận HTTP
 */
public interface PartService {

    /**
     * Thêm mới một linh kiện vào danh mục chuẩn hóa (Create)
     */
    PartResponseDTO createPart(CreatePartRequestDTO request);

    /**
     * Tra cứu chi tiết linh kiện theo mã partCode (Read)
     */
    PartResponseDTO getPartByCode(String partCode);

    /**
     * Tra cứu danh sách linh kiện có lọc từ khóa, danh mục và phân trang (Read)
     */
    Pair<List<PartResponseDTO>, MetaDTO> getParts(String keyword, String category, int page, int limit);

    /**
     * Cập nhật thông tin linh kiện theo mã partCode (Update)
     */
    PartResponseDTO updatePart(String partCode, UpdatePartRequestDTO request);

    /**
     * Xóa / ngưng hoạt động linh kiện (Delete - Soft delete)
     */
    void deletePart(String partCode);
}
