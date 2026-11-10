package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.dto.request.ExportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.request.ImportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.response.TransactionResponseDTO;
import com.mekongmobile.inventory.entity.Part;
import com.mekongmobile.inventory.entity.PartStock;
import com.mekongmobile.inventory.entity.ServiceCenter;
import com.mekongmobile.inventory.entity.Ticket;
import com.mekongmobile.inventory.repository.PartRepository;
import com.mekongmobile.inventory.repository.PartStockRepository;
import com.mekongmobile.inventory.repository.PartTransactionRepository;
import com.mekongmobile.inventory.repository.ServiceCenterRepository;
import com.mekongmobile.inventory.repository.TicketReadOnlyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * =============================================================================
 * TC-INT-02 — KIỂM THỬ END-TO-END TẦNG SERVICE + REPOSITORY + H2
 *
 * Không mock repository. Test dựng dữ liệu thật trong H2, gọi service export
 * rồi import, và kiểm tra:
 *   - Tồn kho thực tế thay đổi đúng.
 *   - Sổ cái part_transactions ghi nhận đủ EXPORT và IMPORT.
 * =============================================================================
 */
@SpringBootTest
@Transactional
@DisplayName("TC-INT-02 (Luồng đúng): Xuất + nhập kho end-to-end trên H2, tồn kho và sổ cái cập nhật đúng")
class PartTransactionIntegrationTest {

    @Autowired
    private PartTransactionService transactionService;

    @Autowired
    private ServiceCenterRepository serviceCenterRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private PartStockRepository partStockRepository;

    @Autowired
    private TicketReadOnlyRepository ticketReadOnlyRepository;

    @Autowired
    private PartTransactionRepository partTransactionRepository;

    @Test
    void testExportThenImport_EndToEnd_PersistsStockAndLedger() {
        ServiceCenter center = serviceCenterRepository.save(ServiceCenter.builder()
                .centerCode("TT-INT-01")
                .centerName("TTBH Integration Test")
                .address("Quận 1, TP.HCM")
                .phone("028.3999.0001")
                .isActive(true)
                .build());

        Part part = partRepository.save(Part.builder()
                .partCode("PIN-INT-01")
                .partName("Pin Integration Test")
                .category("PIN")
                .unit("Cái")
                .unitPrice(new BigDecimal("150000"))
                .isActive(true)
                .build());

        PartStock stock = partStockRepository.save(PartStock.builder()
                .serviceCenter(center)
                .part(part)
                .quantity(10)
                .minThreshold(5)
                .build());

        Ticket ticket = ticketReadOnlyRepository.save(Ticket.builder()
                .ticketCode("BH-INT-0001/2026")
                .serviceCenter(center)
                .status("DANG_XU_LY")
                .customerName("Khách hàng Integration")
                .deviceModel("iPhone 15")
                .build());

        ExportTransactionRequestDTO exportRequest = ExportTransactionRequestDTO.builder()
                .ticketId(ticket.getTicketCode())
                .partId(part.getPartCode())
                .quantity(4)
                .performedBy("ktv_integration")
                .note("Xuất kho kiểm thử integration")
                .build();

        TransactionResponseDTO exportResponse = transactionService.exportPart(center.getCenterId(), exportRequest);

        assertNotNull(exportResponse);
        assertEquals("EXPORT", exportResponse.getTransactionType());
        assertEquals(6, exportResponse.getRemainingStock());
        assertEquals(6, stock.getQuantity(), "Tồn kho phải giảm từ 10 xuống 6");
        assertEquals(1, partTransactionRepository.findExportsByTicketCode(ticket.getTicketCode()).size(),
                "Phải có đúng một bản ghi EXPORT trong sổ cái");

        ImportTransactionRequestDTO importRequest = ImportTransactionRequestDTO.builder()
                .partId(part.getPartCode())
                .quantity(20)
                .performedBy("quanly_integration")
                .note("Nhập kho kiểm thử integration")
                .build();

        TransactionResponseDTO importResponse = transactionService.importPart(center.getCenterId(), importRequest);

        assertNotNull(importResponse);
        assertEquals("IMPORT", importResponse.getTransactionType());
        assertEquals(26, importResponse.getRemainingStock());
        assertEquals(26, stock.getQuantity(), "Tồn kho phải tăng từ 6 lên 26");
        assertEquals(2, partTransactionRepository.count(),
                "Sổ cái phải có đủ bản ghi EXPORT và IMPORT");
    }
}
