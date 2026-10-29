package com.mekongmobile.inventory.repository;

import com.mekongmobile.inventory.entity.PartTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository lưu vết và truy xuất lịch sử giao dịch (Append-Only Log)
 * Phục vụ: US-06, US-07, NFR-01, NFR-04
 */
@Repository
public interface PartTransactionRepository extends JpaRepository<PartTransaction, Long> {

    /**
     * Tra cứu lịch sử xuất linh kiện theo Phiếu bảo hành (US-07)
     */
    @Query("SELECT pt FROM PartTransaction pt " +
           "JOIN FETCH pt.part p " +
           "JOIN FETCH pt.ticket t " +
           "WHERE t.ticketCode = :ticketCode AND pt.transactionType = 'EXPORT' " +
           "ORDER BY pt.createdAt DESC")
    List<PartTransaction> findExportsByTicketCode(@Param("ticketCode") String ticketCode);

    /**
     * Lọc lịch sử giao dịch linh kiện theo nhiều tiêu chí (US-06)
     */
    @Query(value = "SELECT pt FROM PartTransaction pt " +
           "JOIN FETCH pt.part p " +
           "LEFT JOIN FETCH pt.ticket t " +
           "JOIN FETCH pt.serviceCenter sc " +
           "WHERE sc.centerId = :centerId " +
           "AND (:partCode IS NULL OR p.partCode = :partCode) " +
           "AND (:type IS NULL OR pt.transactionType = :type) " +
           "AND (:fromDate IS NULL OR pt.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR pt.createdAt <= :toDate) " +
           "ORDER BY pt.createdAt DESC",
           countQuery = "SELECT count(pt) FROM PartTransaction pt " +
           "WHERE pt.serviceCenter.centerId = :centerId " +
           "AND (:partCode IS NULL OR pt.part.partCode = :partCode) " +
           "AND (:type IS NULL OR pt.transactionType = :type) " +
           "AND (:fromDate IS NULL OR pt.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR pt.createdAt <= :toDate)")
    Page<PartTransaction> findTransactionHistory(
            @Param("centerId") Long centerId,
            @Param("partCode") String partCode,
            @Param("type") String type,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    boolean existsByTransactionCode(String transactionCode);
}
