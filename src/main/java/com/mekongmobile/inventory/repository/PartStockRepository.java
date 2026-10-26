package com.mekongmobile.inventory.repository;

import com.mekongmobile.inventory.entity.PartStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository truy xuất dữ liệu tồn kho linh kiện
 * Phục vụ: NFR-01 (tra cứu nhanh), NFR-02 (SELECT FOR UPDATE chặn xuất âm)
 */
@Repository
public interface PartStockRepository extends JpaRepository<PartStock, Long> {

    /**
     * Tra cứu thông tin tồn kho theo trung tâm và linh kiện (Không khóa)
     */
    @Query("SELECT ps FROM PartStock ps " +
           "JOIN FETCH ps.part p " +
           "JOIN FETCH ps.serviceCenter sc " +
           "WHERE sc.centerId = :centerId AND p.partCode = :partCode")
    Optional<PartStock> findByCenterIdAndPartCode(@Param("centerId") Long centerId, @Param("partCode") String partCode);

    /**
     * Tra cứu thông tin tồn kho kèm Khóa bi quan (Pessimistic Lock - SELECT FOR UPDATE)
     * Đảm bảo đúng NFR-02: 50 luồng xuất đồng thời tồn = 10 -> đúng 10 thành công, 0 lần âm kho
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM PartStock ps " +
           "JOIN FETCH ps.part p " +
           "JOIN FETCH ps.serviceCenter sc " +
           "WHERE sc.centerId = :centerId AND p.partCode = :partCode")
    Optional<PartStock> findByCenterIdAndPartCodeForUpdate(@Param("centerId") Long centerId, @Param("partCode") String partCode);

    /**
     * Tra cứu danh sách tồn kho theo trung tâm có phân trang (NFR-01 <= 500ms)
     */
    @Query("SELECT ps FROM PartStock ps " +
           "JOIN FETCH ps.part p " +
           "JOIN FETCH ps.serviceCenter sc " +
           "WHERE sc.centerId = :centerId " +
           "AND (:keyword IS NULL OR LOWER(p.partCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.partName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:lowStockOnly = false OR ps.quantity < ps.minThreshold)")
    Page<PartStock> findStockList(
            @Param("centerId") Long centerId,
            @Param("keyword") String keyword,
            @Param("lowStockOnly") boolean lowStockOnly,
            Pageable pageable
    );

    boolean existsByServiceCenterCenterIdAndPartPartId(Long centerId, Long partId);
}
