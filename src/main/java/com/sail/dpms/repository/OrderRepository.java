package com.sail.dpms.repository;

import com.sail.dpms.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByDistributorId(Long distributorId);
    List<Order> findByStatus(String status);
    List<Order> findByDistributorIdAndStatus(Long distributorId, String status);
    List<Order> findByUnitId(Long unitId);
}
