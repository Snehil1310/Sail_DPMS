package com.sail.dpms.repository;

import com.sail.dpms.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByDistributorId(Long distributorId);
    List<Inventory> findByUnitId(Long unitId);
    Optional<Inventory> findByDistributorIdAndProductCategory(Long distributorId, String productCategory);
    List<Inventory> findByQuantityLessThanEqual(BigDecimal threshold);
    
    List<Inventory> findByDistributorIsNull();
    List<Inventory> findByDistributorIsNotNull();
    Optional<Inventory> findByDistributorIsNullAndProductCategory(String productCategory);
    Optional<Inventory> findByDistributorIsNullAndUnitIdAndProductCategory(Long unitId, String productCategory);
}
