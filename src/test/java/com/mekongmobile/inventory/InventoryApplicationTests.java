package com.mekongmobile.inventory;

import com.mekongmobile.inventory.entity.PartStock;
import com.mekongmobile.inventory.entity.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị các quy tắc nghiệp vụ cốt lõi luồng L5
 */
class InventoryApplicationTests {

    @Test
    @DisplayName("Kiểm tra quy tắc QT-06: Phiếu BH chỉ được xuất khi DANG_XU_LY hoặc CHO_LINH_KIEN")
    void testTicketValidForExport() {
        Ticket ticketProcessing = Ticket.builder().status("DANG_XU_LY").build();
        Ticket ticketWaiting = Ticket.builder().status("CHO_LINH_KIEN").build();
        Ticket ticketClosed = Ticket.builder().status("DA_DONG").build();
        Ticket ticketAssigned = Ticket.builder().status("DA_PHAN_CONG").build();

        assertTrue(ticketProcessing.isValidForExport(), "Đang xử lý phải được phép xuất");
        assertTrue(ticketWaiting.isValidForExport(), "Chờ linh kiện phải được phép xuất");
        assertFalse(ticketClosed.isValidForExport(), "Đã đóng không được xuất");
        assertFalse(ticketAssigned.isValidForExport(), "Đã phân công chưa được xuất");
    }

    @Test
    @DisplayName("Kiểm tra quy tắc US-05: Cảnh báo tồn thấp khi quantity < min_threshold")
    void testPartStockLowStockAlert() {
        PartStock stockLow = PartStock.builder().quantity(3).minThreshold(5).build();
        PartStock stockEqual = PartStock.builder().quantity(5).minThreshold(5).build();
        PartStock stockHigh = PartStock.builder().quantity(10).minThreshold(5).build();

        assertTrue(stockLow.isLowStock(), "Tồn 3 < ngưỡng 5 phải kích hoạt cảnh báo");
        assertFalse(stockEqual.isLowStock(), "Tồn 5 = ngưỡng 5 không kích hoạt cảnh báo");
        assertFalse(stockHigh.isLowStock(), "Tồn 10 > ngưỡng 5 không kích hoạt cảnh báo");
    }
}
