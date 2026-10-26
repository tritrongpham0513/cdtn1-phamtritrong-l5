package com.mekongmobile.inventory.repository;

import com.mekongmobile.inventory.entity.ServiceCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository danh mục trung tâm bảo hành Mekong Mobile
 */
@Repository
public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Long> {

    Optional<ServiceCenter> findByCenterCode(String centerCode);

    boolean existsByCenterCode(String centerCode);
}
