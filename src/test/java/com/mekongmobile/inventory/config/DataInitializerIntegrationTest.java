package com.mekongmobile.inventory.config;

import com.mekongmobile.inventory.repository.PartRepository;
import com.mekongmobile.inventory.repository.PartStockRepository;
import com.mekongmobile.inventory.repository.PartTransactionRepository;
import com.mekongmobile.inventory.repository.ServiceCenterRepository;
import com.mekongmobile.inventory.repository.TicketReadOnlyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * =============================================================================
 * TC-INT-01 — KIỂM THỬ TÍNH TÁI LẬP (C8) Ở MỨC INTEGRATION
 *
 * Khác với test unit cũ (mock repository), test này nạp Spring context thật,
 * dùng H2 in-memory và chạy DataInitializer hai lần:
 *   - Lần 1: context startup tự động nạp dữ liệu từ seeds/.
 *   - Lần 2: gọi trực tiếp dataInitializer.run().
 *
 * Kỳ vọng: không ném lỗi và số bản ghi của cả 5 bảng không đổi.
 * =============================================================================
 */
@SpringBootTest(properties = {
        "app.seed.enabled=true",
        "app.seed.path=seeds"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("TC-INT-01 (Tính tái lập C8): Seeder chạy lại lần 2 trên H2 không nhân đôi dữ liệu")
class DataInitializerIntegrationTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private ServiceCenterRepository serviceCenterRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private TicketReadOnlyRepository ticketReadOnlyRepository;

    @Autowired
    private PartStockRepository partStockRepository;

    @Autowired
    private PartTransactionRepository partTransactionRepository;

    @Test
    void testSeeder_RunTwice_DoesNotDuplicateData() {
        long centersAfterFirstRun = serviceCenterRepository.count();
        long partsAfterFirstRun = partRepository.count();
        long ticketsAfterFirstRun = ticketReadOnlyRepository.count();
        long stocksAfterFirstRun = partStockRepository.count();
        long transactionsAfterFirstRun = partTransactionRepository.count();

        assertTrue(centersAfterFirstRun > 0, "Seeder phải nạp ít nhất một trung tâm");
        assertTrue(partsAfterFirstRun > 0, "Seeder phải nạp ít nhất một linh kiện");
        assertTrue(ticketsAfterFirstRun > 0, "Seeder phải nạp ít nhất một phiếu bảo hành");

        assertDoesNotThrow(() -> dataInitializer.run(),
                "Chạy lại seeder lần 2 không được ném ngoại lệ");

        assertEquals(centersAfterFirstRun, serviceCenterRepository.count(),
                "service_centers không được nhân đôi");
        assertEquals(partsAfterFirstRun, partRepository.count(),
                "parts không được nhân đôi");
        assertEquals(ticketsAfterFirstRun, ticketReadOnlyRepository.count(),
                "tickets không được nhân đôi");
        assertEquals(stocksAfterFirstRun, partStockRepository.count(),
                "part_stock không được nhân đôi");
        assertEquals(transactionsAfterFirstRun, partTransactionRepository.count(),
                "part_transactions không được nhân đôi");
    }
}
