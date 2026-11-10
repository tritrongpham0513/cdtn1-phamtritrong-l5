package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.dto.request.CreateStockRequestDTO;
import com.mekongmobile.inventory.dto.response.StockItemDTO;
import com.mekongmobile.inventory.entity.Part;
import com.mekongmobile.inventory.entity.PartStock;
import com.mekongmobile.inventory.entity.ServiceCenter;
import com.mekongmobile.inventory.exception.BusinessRuleException;
import com.mekongmobile.inventory.repository.PartRepository;
import com.mekongmobile.inventory.repository.PartStockRepository;
import com.mekongmobile.inventory.repository.ServiceCenterRepository;
import com.mekongmobile.inventory.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * =============================================================================
 * KIỂM THỬ ĐƠN VỊ CHO STOCK SERVICE (QUẢN LÝ TỒN KHO & ĐỊNH MỨC NGƯỠNG)
 * =============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử đơn vị StockService (Quản lý tồn kho & Ngưỡng an toàn)")
public class StockServiceTest {

    @Mock
    private PartStockRepository partStockRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private ServiceCenterRepository serviceCenterRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private ServiceCenter center;
    private Part part;
    private PartStock stock;

    @BeforeEach
    void setUp() {
        center = ServiceCenter.builder()
                .centerId(1L)
                .centerCode("TT01")
                .centerName("TTBH Quận 10")
                .build();

        part = Part.builder()
                .partId(1L)
                .partCode("MH-SS-01")
                .partName("Màn hình Samsung S22")
                .unit("Cái")
                .unitPrice(new BigDecimal("2500000"))
                .build();

        stock = PartStock.builder()
                .stockId(1L)
                .serviceCenter(center)
                .part(part)
                .quantity(10)
                .minThreshold(5)
                .build();
    }

    @Test
    @DisplayName("Lấy chi tiết tồn kho thành công theo mã linh kiện và trung tâm")
    void testGetStockItem_Success() {
        when(partStockRepository.findByCenterIdAndPartCode(1L, "MH-SS-01")).thenReturn(Optional.of(stock));

        StockItemDTO result = stockService.getStockItem(1L, "MH-SS-01");

        assertNotNull(result);
        assertEquals("MH-SS-01", result.getPartId());
        assertEquals(10, result.getQuantity());
        assertFalse(result.getIsLowStock(), "Tồn 10 > ngưỡng 5 không được bật cờ cảnh báo");
    }

    @Test
    @DisplayName("Khởi tạo tồn kho mới thành công cho linh kiện tại trung tâm")
    void testCreateStock_Success() {
        CreateStockRequestDTO request = CreateStockRequestDTO.builder()
                .partCode("MH-SS-01")
                .initialQuantity(15)
                .minThreshold(5)
                .build();

        when(serviceCenterRepository.findById(1L)).thenReturn(Optional.of(center));
        when(partRepository.findByPartCode("MH-SS-01")).thenReturn(Optional.of(part));
        when(partStockRepository.findByCenterIdAndPartCode(1L, "MH-SS-01")).thenReturn(Optional.empty());
        when(partStockRepository.save(any(PartStock.class))).thenAnswer(i -> i.getArgument(0));

        StockItemDTO result = stockService.createStock(1L, request);

        assertNotNull(result);
        assertEquals(15, result.getQuantity());
        assertEquals(5, result.getMinThreshold());
        verify(partStockRepository, times(1)).save(any(PartStock.class));
    }

    @Test
    @DisplayName("Từ chối tạo tồn kho trùng lặp cho cùng một linh kiện tại một trung tâm")
    void testCreateStock_Duplicate_ThrowsBusinessRuleException() {
        CreateStockRequestDTO request = CreateStockRequestDTO.builder()
                .partCode("MH-SS-01")
                .initialQuantity(15)
                .minThreshold(5)
                .build();

        when(serviceCenterRepository.findById(1L)).thenReturn(Optional.of(center));
        when(partRepository.findByPartCode("MH-SS-01")).thenReturn(Optional.of(part));
        when(partStockRepository.findByCenterIdAndPartCode(1L, "MH-SS-01")).thenReturn(Optional.of(stock));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            stockService.createStock(1L, request);
        });

        assertTrue(ex.getMessage().contains("đã tồn tại"));
        verify(partStockRepository, never()).save(any(PartStock.class));
    }

    @Test
    @DisplayName("Từ chối xóa bản ghi tồn kho khi số lượng tồn vẫn còn > 0")
    void testDeleteStock_QuantityGreaterThanZero_ThrowsException() {
        when(partStockRepository.findByCenterIdAndPartCode(1L, "MH-SS-01")).thenReturn(Optional.of(stock));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            stockService.deleteStock(1L, "MH-SS-01");
        });

        assertTrue(ex.getMessage().contains("Không thể xóa linh kiện còn tồn kho > 0"));
        verify(partStockRepository, never()).delete(any(PartStock.class));
    }
}

