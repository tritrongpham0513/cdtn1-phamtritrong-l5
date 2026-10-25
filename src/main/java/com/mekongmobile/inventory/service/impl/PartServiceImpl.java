package com.mekongmobile.inventory.service.impl;

import com.mekongmobile.inventory.dto.request.CreatePartRequestDTO;
import com.mekongmobile.inventory.dto.request.UpdatePartRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO.MetaDTO;
import com.mekongmobile.inventory.dto.response.PartResponseDTO;
import com.mekongmobile.inventory.entity.Part;
import com.mekongmobile.inventory.exception.BusinessRuleException;
import com.mekongmobile.inventory.exception.ResourceNotFoundException;
import com.mekongmobile.inventory.repository.PartRepository;
import com.mekongmobile.inventory.service.PartService;
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
 * Hiện thực nghiệp vụ quản lý danh mục linh kiện (Tiêu chí S1 - CRUD)
 * Tuân thủ S2: Không phụ thuộc vào bất kỳ thư viện hoặc đối tượng HTTP nào
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartServiceImpl implements PartService {

    private final PartRepository partRepository;

    @Override
    @Transactional
    public PartResponseDTO createPart(CreatePartRequestDTO request) {
        String cleanCode = request.getPartCode().trim().toUpperCase();
        if (partRepository.existsByPartCode(cleanCode)) {
            throw new BusinessRuleException("Mã linh kiện '" + cleanCode + "' đã tồn tại trong hệ thống");
        }

        Part part = Part.builder()
                .partCode(cleanCode)
                .partName(request.getPartName().trim())
                .category(request.getCategory().trim())
                .unit(request.getUnit() != null ? request.getUnit().trim() : "Cái")
                .unitPrice(request.getUnitPrice())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Part saved = partRepository.save(part);
        return mapToDTO(saved);
    }

    @Override
    public PartResponseDTO getPartByCode(String partCode) {
        Part part = partRepository.findByPartCode(partCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy linh kiện với mã: " + partCode));
        return mapToDTO(part);
    }

    @Override
    public Pair<List<PartResponseDTO>, MetaDTO> getParts(String keyword, String category, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("partCode").ascending());
        Page<Part> partPage = partRepository.searchParts(keyword, category, pageable);

        List<PartResponseDTO> dtos = partPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        MetaDTO meta = MetaDTO.builder()
                .page(page)
                .limit(limit)
                .total(partPage.getTotalElements())
                .totalPages(partPage.getTotalPages())
                .build();

        return Pair.of(dtos, meta);
    }

    @Override
    @Transactional
    public PartResponseDTO updatePart(String partCode, UpdatePartRequestDTO request) {
        Part part = partRepository.findByPartCode(partCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy linh kiện với mã: " + partCode));

        part.setPartName(request.getPartName().trim());
        part.setCategory(request.getCategory().trim());
        if (request.getUnit() != null) {
            part.setUnit(request.getUnit().trim());
        }
        part.setUnitPrice(request.getUnitPrice());
        if (request.getIsActive() != null) {
            part.setIsActive(request.getIsActive());
        }

        Part updated = partRepository.save(part);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deletePart(String partCode) {
        Part part = partRepository.findByPartCode(partCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy linh kiện với mã: " + partCode));

        // Soft delete để đảm bảo toàn vẹn dữ liệu lịch sử giao dịch (Enterprise Best Practice)
        part.setIsActive(false);
        partRepository.save(part);
    }

    private PartResponseDTO mapToDTO(Part part) {
        return PartResponseDTO.builder()
                .partId(part.getPartId())
                .partCode(part.getPartCode())
                .partName(part.getPartName())
                .category(part.getCategory())
                .unit(part.getUnit())
                .unitPrice(part.getUnitPrice())
                .isActive(part.getIsActive())
                .createdAt(part.getCreatedAt())
                .build();
    }
}
