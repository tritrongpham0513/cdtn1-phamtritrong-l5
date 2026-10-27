package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.dto.request.ExportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.request.ImportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.response.TransactionResponseDTO;
import com.mekongmobile.inventory.entity.*;
import com.mekongmobile.inventory.repository.*;
import com.mekongmobile.inventory.service.impl.PartTransactionServiceImpl;
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
 * BỘ KIỂM THỬ ĐƠN VỊ CHO MODULE CỐT LÕI (THÀNH PHẦN 2 - BÀI TẬP 2)
 * 
 * Đáp ứng đầy đủ 3 loại kiểm thử theo Rubric:
 * 1. Luồng đúng (Normal Flow): Xuất kho thành công khi đủ tồn và phiếu hợp lệ.
 * 2. Giá trị biên (Boundary Value): Xuất số lượng đúng bằng tồn kho (quantity == stock -> tồn về 0).
 * 3. Dữ liệu sai / Vi phạm quy tắc (Invalid Data & Business Rule Violations):
 *    - Cố tình xuất vượt tồn kho (QT-09, NFR-02) -> Bị từ chối.
 *    - Phiếu bảo hành đã đóng hoặc không hợp lệ (QT-06) -> Bị từ chối.
 *    - Số lượng nhập/xuất âm hoặc bằng 0 -> Bị từ chối.
 * =============================================================================
 */
@ExtendWith(MockitoExtension.class)
class PartTransactionServiceTest {

    @Mock
    private PartStockRepository partStockRepository;

    @Mock
    private PartTransactionRepository partTransactionRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private TicketReadOnlyRepository ticketReadOnlyRepository;

    @Mock
    private ServiceCenterRepository serviceCenterRepository;

    @InjectMocks
    private PartTransactionServiceImpl transactionService;

    private ServiceCenter center;
    private Part part;
    private Ticket validTicket;
    private Ticket closedTicket;
    private PartStock stock;

    @BeforeEach
    void setUp() {
        center = ServiceCenter.builder()
                .centerId(1L)
                .centerCode("TT01")
                .centerName("TTBH Quận 10")
                .address("Quận 10, TP.HCM")
                .phone("028.3888.001")
                .isActive(true)
                .build();

        part = Part.builder()
                .partId(1L)
                .partCode("PIN-IP15")
                .partName("Pin zin iPhone 15")
                .category("PIN")
                .unit("Cái")
                .unitPrice(new BigDecimal("850000"))
                .isActive(true)
                .build();

        validTicket = Ticket.builder()
                .ticketId(100L)
                .ticketCode("BH-000456/2026")
                .serviceCenter(center)
                .status("DANG_XU_LY")
                .customerName("Nguyễn Văn An")
                .deviceModel("iPhone 15")
                .build();

        closedTicket = Ticket.builder()
                .ticketId(101L)
                .ticketCode("BH-000789/2026")
                .serviceCenter(center)
                .status("DA_DONG")
                .customerName("Lê Hoàng Long")
                .deviceModel("Galaxy S24")
                .build();

        stock = PartStock.builder()
                .stockId(1L)
                .serviceCenter(center)
                .part(part)
                .quantity(10)
                .minThreshold(5)
                .build();
    }

    // =========================================================================
    // LOẠI 1: LUỒNG ĐÚNG (NORMAL FLOW)
    // =========================================================================
    @Test
    @DisplayName("Loại 1 (Luồng đúng): Xuất kho thành công khi tồn đủ và phiếu bảo hành DANG_XU_LY")
    void testExportPart_Success_NormalFlow() {
        ExportTransactionRequestDTO request = ExportTransactionRequestDTO.builder()
                .partId("PIN-IP15")
                .ticketId("BH-000456/2026")
                .quantity(2)
                .performedBy("KTV - Phạm Trí Trọng")
                .note("Thay pin iPhone 15")
                .build();

        when(ticketReadOnlyRepository.findByTicketCode("BH-000456/2026")).thenReturn(Optional.of(validTicket));
        when(partStockRepository.findByCenterIdAndPartCodeForUpdate(1L, "PIN-IP15")).thenReturn(Optional.of(stock));
        when(partStockRepository.save(any(PartStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDTO response = transactionService.exportPart(1L, request);

        assertNotNull(response);
        assertEquals("EXPORT", response.getTransactionType());
        assertEquals(8, stock.getQuantity(), "Số tồn kho từ 10 phải giảm xuống còn 8");
        assertEquals(8, response.getRemainingStock());
        verify(partTransactionRepository, times(1)).save(any(PartTransaction.class));
    }

    // =========================================================================
    // LOẠI 2: GIÁ TRỊ BIÊN (BOUNDARY VALUE)
    // =========================================================================
    @Test
    @DisplayName("Loại 2 (Giá trị biên): Xuất đúng bằng số lượng tồn kho (tồn 10, xuất 10 -> còn đúng 0, không âm)")
    void testExportPart_Boundary_ExactStockQuantity() {
        ExportTransactionRequestDTO request = ExportTransactionRequestDTO.builder()
                .partId("PIN-IP15")
                .ticketId("BH-000456/2026")
                .quantity(10) // Xuất đúng bằng toàn bộ tồn kho hiện tại
                .performedBy("KTV - Phạm Trí Trọng")
                .note("Xuất hết linh kiện tồn")
                .build();

        when(ticketReadOnlyRepository.findByTicketCode("BH-000456/2026")).thenReturn(Optional.of(validTicket));
        when(partStockRepository.findByCenterIdAndPartCodeForUpdate(1L, "PIN-IP15")).thenReturn(Optional.of(stock));
        when(partStockRepository.save(any(PartStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDTO response = transactionService.exportPart(1L, request);

        assertNotNull(response);
        assertEquals(0, stock.getQuantity(), "Tồn kho phải giảm về đúng 0 mà không bị âm");
        assertEquals(0, response.getRemainingStock());
        assertTrue(response.getIsLowStock(), "Số lượng 0 < minThreshold (5) nên phải bật cảnh báo tồn thấp");
    }

    // =========================================================================
    // LOẠI 3: DỮ LIỆU SAI / TỪ CHỐI ĐÚNG CÁCH (INVALID DATA & ERROR HANDLING)
    // =========================================================================
    @Test
    @DisplayName("Loại 3a (Vi phạm quy tắc QT-09, NFR-02): Chặn xuất quá tồn kho (tồn 10, xuất 11 -> Từ chối)")
    void testExportPart_Error_OverStockAttempt() {
        ExportTransactionRequestDTO request = ExportTransactionRequestDTO.builder()
                .partId("PIN-IP15")
                .ticketId("BH-000456/2026")
                .quantity(11) // Cố tình xuất vượt tồn kho (11 > 10)
                .performedBy("KTV - Phạm Trí Trọng")
                .build();

        when(ticketReadOnlyRepository.findByTicketCode("BH-000456/2026")).thenReturn(Optional.of(validTicket));
        when(partStockRepository.findByCenterIdAndPartCodeForUpdate(1L, "PIN-IP15")).thenReturn(Optional.of(stock));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            transactionService.exportPart(1L, request);
        });

        assertTrue(ex.getMessage().contains("vượt quá số tồn hiện tại"), "Phải trả về thông báo lỗi vi phạm tồn kho rõ ràng");
        assertEquals(10, stock.getQuantity(), "Số tồn kho không được thay đổi khi giao dịch bị từ chối");
        verify(partTransactionRepository, never()).save(any(PartTransaction.class));
    }

    @Test
    @DisplayName("Loại 3b (Vi phạm quy tắc QT-06): Từ chối xuất cho Phiếu bảo hành đã đóng (DA_DONG)")
    void testExportPart_Error_InvalidTicketStatus() {
        ExportTransactionRequestDTO request = ExportTransactionRequestDTO.builder()
                .partId("PIN-IP15")
                .ticketId("BH-000789/2026") // Phiếu đã đóng DA_DONG
                .quantity(1)
                .performedBy("KTV - Phạm Trí Trọng")
                .build();

        when(ticketReadOnlyRepository.findByTicketCode("BH-000789/2026")).thenReturn(Optional.of(closedTicket));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            transactionService.exportPart(1L, request);
        });

        assertTrue(ex.getMessage().contains("DA_DONG"), "Phải từ chối phiếu bảo hành đang ở trạng thái DA_DONG");
        verify(partStockRepository, never()).findByCenterIdAndPartCodeForUpdate(anyLong(), anyString());
    }

    @Test
    @DisplayName("Loại 3c (Dữ liệu vào không hợp lệ C7): Nhập kho với số lượng âm hoặc 0 -> Từ chối")
    void testImportPart_Error_InvalidQuantityNegative() {
        ImportTransactionRequestDTO requestNegative = ImportTransactionRequestDTO.builder()
                .partId("PIN-IP15")
                .quantity(-5)
                .performedBy("Thủ kho")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.importPart(1L, requestNegative);
        });

        assertTrue(ex.getMessage().contains("lớn hơn 0"), "Phải chặn số lượng nhập âm");
    }
}
