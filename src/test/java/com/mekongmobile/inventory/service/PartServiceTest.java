package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.dto.request.CreatePartRequestDTO;
import com.mekongmobile.inventory.dto.request.UpdatePartRequestDTO;
import com.mekongmobile.inventory.dto.response.PartResponseDTO;
import com.mekongmobile.inventory.entity.Part;
import com.mekongmobile.inventory.exception.BusinessRuleException;
import com.mekongmobile.inventory.exception.ResourceNotFoundException;
import com.mekongmobile.inventory.repository.PartRepository;
import com.mekongmobile.inventory.service.impl.PartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử đơn vị cho PartService (Tiêu chí S1 - CRUD)
 */
@ExtendWith(MockitoExtension.class)
class PartServiceTest {

    @Mock
    private PartRepository partRepository;

    @InjectMocks
    private PartServiceImpl partService;

    private Part samplePart;

    @BeforeEach
    void setUp() {
        samplePart = Part.builder()
                .partId(1L)
                .partCode("IP13-SCR-OLED")
                .partName("Màn hình OLED iPhone 13 Original")
                .category("Màn hình")
                .unit("Cái")
                .unitPrice(new BigDecimal("3500000.00"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("[CRUD-Part] Thêm mới linh kiện thành công khi mã chưa tồn tại")
    void createPart_Success() {
        CreatePartRequestDTO request = CreatePartRequestDTO.builder()
                .partCode("SAM-S24-BAT")
                .partName("Pin Samsung Galaxy S24 4000mAh")
                .category("Pin")
                .unit("Cái")
                .unitPrice(new BigDecimal("850000.00"))
                .build();

        when(partRepository.existsByPartCode("SAM-S24-BAT")).thenReturn(false);
        when(partRepository.save(any(Part.class))).thenAnswer(invocation -> {
            Part p = invocation.getArgument(0);
            p.setPartId(2L);
            return p;
        });

        PartResponseDTO result = partService.createPart(request);

        assertNotNull(result);
        assertEquals("SAM-S24-BAT", result.getPartCode());
        assertEquals("Pin", result.getCategory());
        assertTrue(result.getIsActive());
        verify(partRepository).save(any(Part.class));
    }

    @Test
    @DisplayName("[CRUD-Part] Từ chối thêm mới khi mã linh kiện đã tồn tại")
    void createPart_DuplicateCode_ThrowsException() {
        CreatePartRequestDTO request = CreatePartRequestDTO.builder()
                .partCode("IP13-SCR-OLED")
                .partName("Màn hình iPhone 13 trùng mã")
                .category("Màn hình")
                .unit("Cái")
                .unitPrice(new BigDecimal("3500000.00"))
                .build();

        when(partRepository.existsByPartCode("IP13-SCR-OLED")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> partService.createPart(request));
        assertTrue(ex.getMessage().contains("đã tồn tại"));
        verify(partRepository, never()).save(any(Part.class));
    }

    @Test
    @DisplayName("[CRUD-Part] Đọc chi tiết linh kiện theo mã thành công")
    void getPartByCode_Success() {
        when(partRepository.findByPartCode("IP13-SCR-OLED")).thenReturn(Optional.of(samplePart));

        PartResponseDTO result = partService.getPartByCode("IP13-SCR-OLED");

        assertNotNull(result);
        assertEquals("IP13-SCR-OLED", result.getPartCode());
        assertEquals(new BigDecimal("3500000.00"), result.getUnitPrice());
    }

    @Test
    @DisplayName("[CRUD-Part] Cập nhật thông tin linh kiện thành công")
    void updatePart_Success() {
        UpdatePartRequestDTO request = UpdatePartRequestDTO.builder()
                .partName("Màn hình OLED iPhone 13 Zin Đẹp")
                .category("Màn hình Zin")
                .unit("Bộ")
                .unitPrice(new BigDecimal("3600000.00"))
                .isActive(true)
                .build();

        when(partRepository.findByPartCode("IP13-SCR-OLED")).thenReturn(Optional.of(samplePart));
        when(partRepository.save(any(Part.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PartResponseDTO updated = partService.updatePart("IP13-SCR-OLED", request);

        assertNotNull(updated);
        assertEquals("Màn hình OLED iPhone 13 Zin Đẹp", updated.getPartName());
        assertEquals("Màn hình Zin", updated.getCategory());
        assertEquals("Bộ", updated.getUnit());
    }

    @Test
    @DisplayName("[CRUD-Part] Xóa linh kiện (Soft delete chuyển isActive = false)")
    void deletePart_SoftDelete_Success() {
        when(partRepository.findByPartCode("IP13-SCR-OLED")).thenReturn(Optional.of(samplePart));
        when(partRepository.save(any(Part.class))).thenAnswer(invocation -> invocation.getArgument(0));

        partService.deletePart("IP13-SCR-OLED");

        assertFalse(samplePart.getIsActive());
        verify(partRepository).save(samplePart);
    }
}
