package com.mekongmobile.inventory.service;

import com.mekongmobile.inventory.dto.request.ExportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.request.ImportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO.MetaDTO;
import com.mekongmobile.inventory.dto.response.TicketPartDTO;
import com.mekongmobile.inventory.dto.response.TransactionResponseDTO;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.util.List;

/**
 * Service xử lý giao dịch kho (Nhập kho, Xuất kho, Tra cứu lịch sử)
 * Phục vụ: QT-06, QT-09, QT-13, NFR-02 (Pessimistic Lock), NFR-04
 */
public interface PartTransactionService {

    /**
     * Ghi nhận nhập kho linh kiện (UC-02, US-02, FR-02)
     */
    TransactionResponseDTO importPart(Long centerId, ImportTransactionRequestDTO request);

    /**
     * Xuất linh kiện cho phiếu bảo hành (UC-03, UC-04, US-03, US-04, FR-03, FR-04)
     * Áp dụng khóa bi quan SELECT FOR UPDATE để đảm bảo NFR-02 và QT-L5-03 (không âm kho)
     */
    TransactionResponseDTO exportPart(Long centerId, ExportTransactionRequestDTO request);

    /**
     * Tra cứu danh sách linh kiện đã dùng cho một phiếu bảo hành (UC-06, US-07, FR-06)
     */
    List<TicketPartDTO> getPartsByTicket(String ticketId);

    /**
     * Xem lịch sử giao dịch kho có lọc và phân trang (UC-06, US-06)
     */
    Pair<List<TransactionResponseDTO>, MetaDTO> getTransactionHistory(
            Long centerId,
            String partCode,
            String type,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int limit
    );
}
