package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.dto.request.CreateStockRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO.MetaDTO;
import com.mekongmobile.inventory.dto.response.StockItemDTO;
import org.springframework.data.util.Pair;

import java.util.List;

/**
 * Service quản lý tồn kho linh kiện theo trung tâm bảo hành (CRUD PartStock)
 * Phục vụ: QT-14 (cô lập dữ liệu trung tâm), US-01, NFR-01 (<= 500ms)
 * Tuân thủ S2: Tuyệt đối không chứa dấu vết HTTP
 */
public interface StockService {

    /**
     * Tra cứu danh sách tồn kho linh kiện có lọc và phân trang (Read)
     */
    Pair<List<StockItemDTO>, MetaDTO> getStockList(
            Long centerId,
            String keyword,
            boolean lowStockOnly,
            int page,
            int limit
    );

    /**
     * Lấy thông tin tồn kho chi tiết của một linh kiện tại một trung tâm (Read)
     */
    StockItemDTO getStockItem(Long centerId, String partCode);

    /**
     * Khởi tạo / bổ sung bản ghi tồn kho cho một linh kiện tại trung tâm (Create)
     */
    StockItemDTO createStock(Long centerId, CreateStockRequestDTO request);

    /**
     * Xóa bản ghi tồn kho khi số lượng tồn bằng 0 (Delete)
     */
    void deleteStock(Long centerId, String partCode);
}
