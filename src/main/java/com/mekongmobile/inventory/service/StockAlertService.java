package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.dto.request.UpdateThresholdRequestDTO;
import com.mekongmobile.inventory.dto.response.StockItemDTO;

/**
 * Service cảnh báo tồn thấp và cấu hình ngưỡng tối thiểu
 * Phục vụ: US-05, US-08, FR-07
 */
public interface StockAlertService {

    /**
     * Cập nhật ngưỡng tồn tối thiểu cho linh kiện (UC-07, US-08)
     */
    StockItemDTO updateMinThreshold(Long centerId, UpdateThresholdRequestDTO request);
}
