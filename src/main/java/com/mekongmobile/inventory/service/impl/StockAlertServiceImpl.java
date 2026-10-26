package com.mekongmobile.inventory.service.impl;

import com.mekongmobile.inventory.dto.request.UpdateThresholdRequestDTO;
import com.mekongmobile.inventory.dto.response.StockItemDTO;
import com.mekongmobile.inventory.entity.PartStock;
import com.mekongmobile.inventory.repository.PartStockRepository;
import com.mekongmobile.inventory.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Hiện thực nghiệp vụ quản lý cảnh báo và ngưỡng tồn (StockAlertService)
 */
@Service
@RequiredArgsConstructor
public class StockAlertServiceImpl implements StockAlertService {

    private final PartStockRepository partStockRepository;

    @Override
    @Transactional
    public StockItemDTO updateMinThreshold(Long centerId, UpdateThresholdRequestDTO request) {
        PartStock stock = partStockRepository.findByCenterIdAndPartCode(centerId, request.getPartId())
                .orElseThrow(() -> new IllegalArgumentException("Linh kiện " + request.getPartId() + " không tồn tại trong kho"));

        if (request.getMinThreshold() < 0) {
            throw new IllegalArgumentException("Ngưỡng tối thiểu phải là số nguyên không âm (>= 0)");
        }

        stock.setMinThreshold(request.getMinThreshold());
        stock.setUpdatedAt(LocalDateTime.now());
        partStockRepository.save(stock);

        return StockItemDTO.builder()
                .partId(stock.getPart().getPartCode())
                .partName(stock.getPart().getPartName())
                .category(stock.getPart().getCategory())
                .unit(stock.getPart().getUnit())
                .unitPrice(stock.getPart().getUnitPrice())
                .quantity(stock.getQuantity())
                .minThreshold(stock.getMinThreshold())
                .isLowStock(stock.isLowStock())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}
