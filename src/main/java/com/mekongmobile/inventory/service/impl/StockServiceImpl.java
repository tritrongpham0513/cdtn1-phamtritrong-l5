package com.mekongmobile.inventory.service.impl;

import com.mekongmobile.inventory.dto.request.CreateStockRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO.MetaDTO;
import com.mekongmobile.inventory.dto.response.StockItemDTO;
import com.mekongmobile.inventory.entity.Part;
import com.mekongmobile.inventory.entity.PartStock;
import com.mekongmobile.inventory.entity.ServiceCenter;
import com.mekongmobile.inventory.exception.BusinessRuleException;
import com.mekongmobile.inventory.exception.ResourceNotFoundException;
import com.mekongmobile.inventory.repository.PartRepository;
import com.mekongmobile.inventory.repository.PartStockRepository;
import com.mekongmobile.inventory.repository.ServiceCenterRepository;
import com.mekongmobile.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hiện thực nghiệp vụ quản lý tồn kho (CRUD PartStock)
 * Tuân thủ S2: Tuyệt đối không chứa dấu vết HTTP
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final PartStockRepository partStockRepository;
    private final PartRepository partRepository;
    private final ServiceCenterRepository serviceCenterRepository;

    @Override
    public Pair<List<StockItemDTO>, MetaDTO> getStockList(
            Long centerId,
            String keyword,
            boolean lowStockOnly,
            int page,
            int limit
    ) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("stockId").ascending());
        Page<PartStock> stockPage = partStockRepository.findStockList(centerId, keyword, lowStockOnly, pageable);

        List<StockItemDTO> items = stockPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        MetaDTO meta = MetaDTO.builder()
                .page(page)
                .limit(limit)
                .total(stockPage.getTotalElements())
                .totalPages(stockPage.getTotalPages())
                .build();

        return Pair.of(items, meta);
    }

    @Override
    public StockItemDTO getStockItem(Long centerId, String partCode) {
        return partStockRepository.findByCenterIdAndPartCode(centerId, partCode)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public StockItemDTO createStock(Long centerId, CreateStockRequestDTO request) {
        ServiceCenter center = serviceCenterRepository.findById(centerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trung tâm bảo hành ID: " + centerId));

        Part part = partRepository.findByPartCode(request.getPartCode().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy linh kiện với mã: " + request.getPartCode()));

        if (partStockRepository.findByCenterIdAndPartCode(centerId, part.getPartCode()).isPresent()) {
            throw new BusinessRuleException("Linh kiện '" + part.getPartCode() + "' đã tồn tại trong kho trung tâm " + centerId);
        }

        PartStock stock = PartStock.builder()
                .serviceCenter(center)
                .part(part)
                .quantity(request.getInitialQuantity())
                .minThreshold(request.getMinThreshold())
                .updatedAt(LocalDateTime.now())
                .build();

        PartStock saved = partStockRepository.save(stock);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteStock(Long centerId, String partCode) {
        PartStock stock = partStockRepository.findByCenterIdAndPartCode(centerId, partCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho linh kiện " + partCode + " tại trung tâm " + centerId));

        if (stock.getQuantity() > 0) {
            throw new BusinessRuleException("Không thể xóa linh kiện còn tồn kho > 0 (Hiện tồn: " + stock.getQuantity() + "). Vui lòng xuất hết trước khi xóa.");
        }

        partStockRepository.delete(stock);
    }

    private StockItemDTO mapToDTO(PartStock stock) {
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
