package com.mekongmobile.inventory.service.impl;

import com.mekongmobile.inventory.dto.request.ExportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.request.ImportTransactionRequestDTO;
import com.mekongmobile.inventory.dto.response.ApiResponseDTO.MetaDTO;
import com.mekongmobile.inventory.dto.response.TicketPartDTO;
import com.mekongmobile.inventory.dto.response.TransactionResponseDTO;
import com.mekongmobile.inventory.entity.*;
import com.mekongmobile.inventory.repository.*;
import com.mekongmobile.inventory.service.PartTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Hiện thực nghiệp vụ giao dịch kho (PartTransactionService)
 * Đảm bảo NFR-02 (Khóa bi quan), QT-06 (Vòng đời phiếu), QT-09 (Chặn xuất âm), QT-13 (Không xóa vật lý)
 */
@Service
@RequiredArgsConstructor
public class PartTransactionServiceImpl implements PartTransactionService {

    private final PartStockRepository partStockRepository;
    private final PartTransactionRepository partTransactionRepository;
    private final PartRepository partRepository;
    private final TicketReadOnlyRepository ticketReadOnlyRepository;
    private final ServiceCenterRepository serviceCenterRepository;

    @Override
    @Transactional
    public TransactionResponseDTO importPart(Long centerId, ImportTransactionRequestDTO request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải là số nguyên lớn hơn 0");
        }

        ServiceCenter center = serviceCenterRepository.findById(centerId)
                .orElseThrow(() -> new IllegalArgumentException("Trung tâm bảo hành không tồn tại: " + centerId));

        Part part = partRepository.findByPartCode(request.getPartId())
                .orElseThrow(() -> new IllegalArgumentException("Mã linh kiện không tồn tại trong danh mục: " + request.getPartId()));

        // Tìm hoặc khởi tạo bản ghi tồn kho nếu chưa có
        PartStock stock = partStockRepository.findByCenterIdAndPartCode(centerId, request.getPartId())
                .orElseGet(() -> PartStock.builder()
                        .serviceCenter(center)
                        .part(part)
                        .quantity(0)
                        .minThreshold(5)
                        .build());

        // Cập nhật số tồn
        int newQuantity = stock.getQuantity() + request.getQuantity();
        stock.setQuantity(newQuantity);
        stock.setUpdatedAt(LocalDateTime.now());
        partStockRepository.save(stock);

        // Sinh mã giao dịch (GD-NK-yyyyMM-xxx)
        String txCode = generateTxCode("GD-NK");

        // Ghi log bất biến (Append-Only Log)
        PartTransaction tx = PartTransaction.builder()
                .transactionCode(txCode)
                .serviceCenter(center)
                .part(part)
                .ticket(null) // Nhập kho không gắn phiếu BH
                .transactionType("IMPORT")
                .quantity(request.getQuantity())
                .performedBy(request.getPerformedBy())
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();
        partTransactionRepository.save(tx);

        return TransactionResponseDTO.builder()
                .transactionId(tx.getTransactionCode())
                .transactionType("IMPORT")
                .partId(part.getPartCode())
                .partName(part.getPartName())
                .ticketId(null)
                .quantity(request.getQuantity())
                .remainingStock(stock.getQuantity())
                .isLowStock(stock.isLowStock())
                .performedBy(tx.getPerformedBy())
                .note(tx.getNote())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public TransactionResponseDTO exportPart(Long centerId, ExportTransactionRequestDTO request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng linh kiện xuất phải là số nguyên lớn hơn 0");
        }

        // 1. Kiểm tra phiếu bảo hành (QT-06 & QT-14)
        Ticket ticket = ticketReadOnlyRepository.findByTicketCode(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Phiếu bảo hành không tồn tại: " + request.getTicketId()));

        if (!ticket.getServiceCenter().getCenterId().equals(centerId)) {
            throw new IllegalArgumentException("Phiếu bảo hành không thuộc quyền quản lý của trung tâm bạn");
        }

        if (!ticket.isValidForExport()) {
            throw new IllegalStateException("Không thể xuất linh kiện: Phiếu bảo hành đang ở trạng thái '" 
                    + ticket.getStatus() + "', chỉ được xuất khi 'DANG_XU_LY' hoặc 'CHO_LINH_KIEN'");
        }

        // 2. KHÓA BI QUAN (Pessimistic Lock - SELECT FOR UPDATE) trên part_stock theo NFR-02
        PartStock stock = partStockRepository.findByCenterIdAndPartCodeForUpdate(centerId, request.getPartId())
                .orElseThrow(() -> new IllegalArgumentException("Linh kiện " + request.getPartId() + " chưa được cấu hình tại trung tâm"));

        // 3. Kiểm tra số tồn (QT-09, US-04, QT-L5-03)
        if (stock.getQuantity() < request.getQuantity()) {
            throw new IllegalStateException("Số lượng xuất (" + request.getQuantity() + ") vượt quá số tồn hiện tại (" 
                    + stock.getQuantity() + "). Giao dịch bị hủy.");
        }

        // 4. Trừ tồn kho
        stock.setQuantity(stock.getQuantity() - request.getQuantity());
        stock.setUpdatedAt(LocalDateTime.now());
        partStockRepository.save(stock);

        // 5. Ghi nhận giao dịch xuất kho bất biến (Append-Only Log)
        String txCode = generateTxCode("GD-XK");
        PartTransaction tx = PartTransaction.builder()
                .transactionCode(txCode)
                .serviceCenter(stock.getServiceCenter())
                .part(stock.getPart())
                .ticket(ticket)
                .transactionType("EXPORT")
                .quantity(request.getQuantity())
                .performedBy(request.getPerformedBy())
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();
        partTransactionRepository.save(tx);

        return TransactionResponseDTO.builder()
                .transactionId(tx.getTransactionCode())
                .transactionType("EXPORT")
                .partId(stock.getPart().getPartCode())
                .partName(stock.getPart().getPartName())
                .ticketId(ticket.getTicketCode())
                .quantity(request.getQuantity())
                .remainingStock(stock.getQuantity())
                .isLowStock(stock.isLowStock())
                .performedBy(tx.getPerformedBy())
                .note(tx.getNote())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketPartDTO> getPartsByTicket(String ticketId) {
        List<PartTransaction> list = partTransactionRepository.findExportsByTicketCode(ticketId);
        return list.stream()
                .map(tx -> TicketPartDTO.builder()
                        .transactionId(tx.getTransactionCode())
                        .partId(tx.getPart().getPartCode())
                        .partName(tx.getPart().getPartName())
                        .quantity(tx.getQuantity())
                        .performedBy(tx.getPerformedBy())
                        .exportDate(tx.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TransactionResponseDTO>, MetaDTO> getTransactionHistory(
            Long centerId,
            String partCode,
            String type,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int limit
    ) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.atTime(23, 59, 59) : null;
        String cleanPart = (partCode != null && !partCode.trim().isEmpty()) ? partCode.trim() : null;
        String cleanType = (type != null && !type.trim().isEmpty()) ? type.trim().toUpperCase() : null;

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<PartTransaction> txPage = partTransactionRepository.findTransactionHistory(
                centerId, cleanPart, cleanType, start, end, pageable
        );

        List<TransactionResponseDTO> items = txPage.getContent().stream()
                .map(tx -> TransactionResponseDTO.builder()
                        .transactionId(tx.getTransactionCode())
                        .transactionType(tx.getTransactionType())
                        .partId(tx.getPart().getPartCode())
                        .partName(tx.getPart().getPartName())
                        .ticketId(tx.getTicket() != null ? tx.getTicket().getTicketCode() : null)
                        .quantity(tx.getQuantity())
                        .remainingStock(null)
                        .isLowStock(null)
                        .performedBy(tx.getPerformedBy())
                        .note(tx.getNote())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        MetaDTO meta = MetaDTO.builder()
                .page(page)
                .limit(limit)
                .total(txPage.getTotalElements())
                .totalPages(txPage.getTotalPages())
                .build();

        return Pair.of(items, meta);
    }

    private String generateTxCode(String prefix) {
        String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String shortUid = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("%s-%s-%s", prefix, ym, shortUid);
    }
}
