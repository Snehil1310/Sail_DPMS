package com.sail.dpms.service;

import com.sail.dpms.entity.*;
import com.sail.dpms.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          LedgerEntryRepository ledgerEntryRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    public List<Payment> getByDistributor(Long distributorId) {
        return paymentRepository.findByDistributorId(distributorId);
    }

    /**
     * Simulated payment processing.
     * Creates a payment record, marks the order as PAID, and creates a PAYMENT_RECEIVED ledger entry.
     */
    public Payment processPayment(Long orderId, String paymentMethod, String cardLastFour) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"APPROVED".equals(order.getStatus())) {
            throw new RuntimeException("Order must be in APPROVED status to pay. Current: " + order.getStatus());
        }

        // Check if already paid
        Optional<Payment> existing = paymentRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            throw new RuntimeException("Payment already exists for this order");
        }

        // Generate a mock transaction reference
        String txnRef = "SAIL-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setDistributor(order.getDistributor());
        payment.setAmount(order.getTotalPrice());
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionRef(txnRef);
        payment.setCardLastFour(cardLastFour);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update order status
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        // Create PAYMENT_RECEIVED ledger entry (credit = payment received)
        BigDecimal currentBalance = ledgerEntryRepository.calculateBalanceByDistributorId(order.getDistributor().getId());
        if (currentBalance == null) currentBalance = BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(order.getTotalPrice());

        LedgerEntry ledger = new LedgerEntry();
        ledger.setDistributor(order.getDistributor());
        ledger.setOrder(order);
        ledger.setEntryType("PAYMENT_RECEIVED");
        ledger.setProductCategory(order.getProductCategory());
        ledger.setQuantityMt(order.getQuantity());
        ledger.setDebit(BigDecimal.ZERO);
        ledger.setCredit(order.getTotalPrice());
        ledger.setBalance(newBalance);
        ledger.setTransactionDate(LocalDateTime.now());
        ledger.setRemarks("Payment received via " + paymentMethod + " | Ref: " + txnRef);
        ledgerEntryRepository.save(ledger);

        return payment;
    }
}
