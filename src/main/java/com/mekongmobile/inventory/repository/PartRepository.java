package com.mekongmobile.inventory.repository;

import com.mekongmobile.inventory.entity.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository danh mục linh kiện chuẩn hóa dùng chung toàn công ty
 */
@Repository
public interface PartRepository extends JpaRepository<Part, Long> {

    Optional<Part> findByPartCode(String partCode);

    boolean existsByPartCode(String partCode);

    @Query("SELECT p FROM Part p WHERE " +
           "(:keyword IS NULL OR LOWER(p.partCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(p.partName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR p.category = :category)")
    Page<Part> searchParts(@Param("keyword") String keyword, @Param("category") String category, Pageable pageable);
}
