package com.sail.dpms.repository;

import com.sail.dpms.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByDistributorId(Long distributorId);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionRef(String transactionRef);
}
