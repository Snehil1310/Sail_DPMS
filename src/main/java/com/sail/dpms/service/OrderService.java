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
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final DistributorRepository distributorRepository;
    private final SailUnitRepository sailUnitRepository;

    public OrderService(OrderRepository orderRepository,
                        InventoryRepository inventoryRepository,
                        LedgerEntryRepository ledgerEntryRepository,
                        DistributorRepository distributorRepository,
                        SailUnitRepository sailUnitRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.distributorRepository = distributorRepository;
        this.sailUnitRepository = sailUnitRepository;
    }

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public List<Order> getByDistributor(Long distributorId) {
        return orderRepository.findByDistributorId(distributorId);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus("PENDING");
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Place a new order from a distributor.
     * Looks up current price from inventory to compute total.
     */
    public Order placeOrder(Long distributorId, String productCategory, BigDecimal quantity) {
        Distributor dist = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new RuntimeException("Distributor not found"));

        // Find inventory to get price
        Optional<Inventory> invOpt = inventoryRepository.findByDistributorIdAndProductCategory(distributorId, productCategory);
        BigDecimal pricePerMt;
        if (invOpt.isPresent()) {
            pricePerMt = invOpt.get().getPricePerMt();
        } else {
            // Default price if no inventory record exists
            pricePerMt = new BigDecimal("45000.00");
        }

        BigDecimal totalPrice = pricePerMt.multiply(quantity);

        Order order = new Order();
        order.setDistributor(dist);
        order.setUnit(dist.getUnit());
        order.setProductCategory(productCategory);
        order.setQuantity(quantity);
        order.setPricePerMt(pricePerMt);
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");
        order.setPlacedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    /**
     * Approve an order: deduct from inventory, create a MATERIAL_SENT ledger entry.
     */
    public Order approveOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Order is not in PENDING status");
        }

        // Deduct from Plant Inventory
        Optional<Inventory> plantInvOpt = inventoryRepository.findByDistributorIsNullAndUnitIdAndProductCategory(
                order.getUnit().getId(), order.getProductCategory());
        if (plantInvOpt.isPresent()) {
            Inventory plantInv = plantInvOpt.get();
            BigDecimal newQty = plantInv.getQuantity().subtract(order.getQuantity());
            if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Insufficient plant inventory stock. Available: " + plantInv.getQuantity() + " MT");
            }
            plantInv.setQuantity(newQty);
            plantInv.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(plantInv);
        } else {
            throw new RuntimeException("No plant inventory record found for product: " + order.getProductCategory());
        }

        // Add to Distributor Inventory
        Optional<Inventory> distInvOpt = inventoryRepository.findByDistributorIdAndProductCategory(
                order.getDistributor().getId(), order.getProductCategory());
        if (distInvOpt.isPresent()) {
            Inventory distInv = distInvOpt.get();
            distInv.setQuantity(distInv.getQuantity().add(order.getQuantity()));
            distInv.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(distInv);
        } else {
            // Create new inventory record for distributor if it doesn't exist
            Inventory newDistInv = new Inventory();
            newDistInv.setDistributor(order.getDistributor());
            newDistInv.setUnit(order.getUnit());
            newDistInv.setProductCategory(order.getProductCategory());
            newDistInv.setQuantity(order.getQuantity());
            newDistInv.setPricePerMt(order.getPricePerMt());
            newDistInv.setThreshold(new BigDecimal("100")); // default threshold
            newDistInv.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(newDistInv);
        }

        order.setStatus("APPROVED");
        order.setApprovedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Create MATERIAL_SENT ledger entry (debit = amount owed by distributor)
        BigDecimal currentBalance = ledgerEntryRepository.calculateBalanceByDistributorId(order.getDistributor().getId());
        if (currentBalance == null) currentBalance = BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.subtract(order.getTotalPrice());

        LedgerEntry ledger = new LedgerEntry();
        ledger.setDistributor(order.getDistributor());
        ledger.setOrder(order);
        ledger.setEntryType("MATERIAL_SENT");
        ledger.setProductCategory(order.getProductCategory());
        ledger.setQuantityMt(order.getQuantity());
        ledger.setDebit(order.getTotalPrice());
        ledger.setCredit(BigDecimal.ZERO);
        ledger.setBalance(newBalance);
        ledger.setTransactionDate(LocalDateTime.now());
        ledger.setRemarks("Material dispatched: " + order.getQuantity() + " MT " + order.getProductCategory());
        ledgerEntryRepository.save(ledger);

        return order;
    }

    /**
     * Reject an order with a reason.
     */
    public Order rejectOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Order is not in PENDING status");
        }

        order.setStatus("REJECTED");
        order.setRejectReason(reason);
        return orderRepository.save(order);
    }
}
