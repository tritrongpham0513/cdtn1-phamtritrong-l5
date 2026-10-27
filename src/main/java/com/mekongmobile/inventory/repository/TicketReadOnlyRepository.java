package com.mekongmobile.inventory.repository;

import com.mekongmobile.inventory.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository đọc dữ liệu phiếu bảo hành (Read-Only từ L2 theo RB-03)
 * Luồng L5 chỉ đọc trạng thái phiếu, không thực hiện UPDATE/DELETE trạng thái phiếu
 */
@Repository
public interface TicketReadOnlyRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t " +
           "JOIN FETCH t.serviceCenter sc " +
           "WHERE t.ticketCode = :ticketCode")
    Optional<Ticket> findByTicketCode(@Param("ticketCode") String ticketCode);

    boolean existsByTicketCode(String ticketCode);
}
