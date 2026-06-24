package com.sail.dpms.controller;

import com.sail.dpms.entity.*;
import com.sail.dpms.repository.*;
import com.sail.dpms.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/distributor")
public class DistributorController {

    private final DistributorRepository distributorRepository;
    private final SalesTargetRepository salesTargetRepository;
    private final SalesEntryRepository salesEntryRepository;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final LedgerEntryRepository ledgerEntryRepository;

    public DistributorController(DistributorRepository distributorRepository,
                                  SalesTargetRepository salesTargetRepository,
                                  SalesEntryRepository salesEntryRepository,
                                  InventoryService inventoryService,
                                  OrderService orderService,
                                  PaymentService paymentService,
                                  LedgerEntryRepository ledgerEntryRepository) {
        this.distributorRepository = distributorRepository;
        this.salesTargetRepository = salesTargetRepository;
        this.salesEntryRepository = salesEntryRepository;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Distributor not found");
            return ResponseEntity.status(404).body(error);
        }
        Distributor dist = distOpt.get();
        Map<String, Object> map = new HashMap<>();
        map.put("id", dist.getId());
        map.put("name", dist.getName());
        map.put("contactEmail", dist.getContactEmail());
        map.put("contactPhone", dist.getContactPhone());
        map.put("region", dist.getRegion());
        if (dist.getUnit() != null) {
            Map<String, Object> unitMap = new HashMap<>();
            unitMap.put("id", dist.getUnit().getId());
            unitMap.put("name", dist.getUnit().getName());
            unitMap.put("shortCode", dist.getUnit().getShortCode());
            unitMap.put("location", dist.getUnit().getLocation());
            map.put("unit", unitMap);
        }
        return ResponseEntity.ok(map);
    }

    @GetMapping("/{userId}/targets")
    public ResponseEntity<?> getTargets(@PathVariable Long userId) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Distributor not found");
            return ResponseEntity.status(404).body(error);
        }

        Distributor distributor = distOpt.get();
        List<SalesTarget> targets = salesTargetRepository.findByDistributorId(distributor.getId());
        List<SalesEntry> sales = salesEntryRepository.findByDistributorId(distributor.getId());

        BigDecimal totalTarget = targets.stream()
                .map(t -> t.getTargetVolume() != null ? t.getTargetVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAchieved = sales.stream()
                .map(s -> s.getSalesVolume() != null ? s.getSalesVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double performancePercent = 0.0;
        if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
            performancePercent = totalAchieved.divide(totalTarget, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        List<Map<String, Object>> targetMaps = targets.stream().map(t -> {
            Map<String, Object> tMap = new HashMap<>();
            tMap.put("id", t.getId());
            tMap.put("targetVolume", t.getTargetVolume());
            tMap.put("fiscalYear", t.getFiscalYear());
            tMap.put("quarter", t.getQuarter());
            tMap.put("assignedAt", t.getAssignedAt());
            return tMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("targets", targetMaps);
        result.put("totalTarget", totalTarget);
        result.put("totalAchieved", totalAchieved);
        result.put("performancePercent", Math.round(performancePercent * 100.0) / 100.0);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/sales")
    public ResponseEntity<?> getSales(@PathVariable Long userId) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Distributor not found");
            return ResponseEntity.status(404).body(error);
        }

        Distributor distributor = distOpt.get();
        List<SalesEntry> sales = salesEntryRepository.findByDistributorId(distributor.getId());

        // Sort by submittedAt descending
        List<SalesEntry> sorted = sales.stream()
                .sorted(Comparator.comparing(SalesEntry::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        List<Map<String, Object>> salesMaps = sorted.stream().map(s -> {
            Map<String, Object> sMap = new HashMap<>();
            sMap.put("id", s.getId());
            sMap.put("salesVolume", s.getSalesVolume());
            sMap.put("productCategory", s.getProductCategory());
            sMap.put("month", s.getMonth());
            sMap.put("fiscalYear", s.getFiscalYear());
            sMap.put("dispatchDate", s.getDispatchDate());
            sMap.put("paymentDate", s.getPaymentDate());
            sMap.put("remarks", s.getRemarks());
            sMap.put("submittedAt", s.getSubmittedAt());
            return sMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(salesMaps);
    }

    @PostMapping("/{userId}/sales")
    public ResponseEntity<Map<String, Object>> createSalesEntry(@PathVariable Long userId,
                                                                 @RequestBody Map<String, Object> body) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Distributor not found");
            return ResponseEntity.status(404).body(error);
        }

        Distributor distributor = distOpt.get();

        SalesEntry entry = new SalesEntry();
        entry.setDistributor(distributor);
        
        BigDecimal volume = BigDecimal.ZERO;
        if (body.get("salesVolume") != null) {
            volume = new BigDecimal(body.get("salesVolume").toString());
        } else if (body.get("salesAmount") != null) {
            volume = new BigDecimal(body.get("salesAmount").toString());
        }
        entry.setSalesVolume(volume);
        
        entry.setProductCategory((String) body.get("productCategory"));
        entry.setMonth((String) body.get("month"));
        entry.setFiscalYear((String) body.get("fiscalYear"));
        
        if (body.get("dispatchDate") != null && !body.get("dispatchDate").toString().trim().isEmpty()) {
            entry.setDispatchDate(java.time.LocalDate.parse(body.get("dispatchDate").toString()));
        }
        if (body.get("paymentDate") != null && !body.get("paymentDate").toString().trim().isEmpty()) {
            entry.setPaymentDate(java.time.LocalDate.parse(body.get("paymentDate").toString()));
        }

        entry.setRemarks((String) body.get("remarks"));
        entry.setSubmittedAt(LocalDateTime.now());

        SalesEntry saved = salesEntryRepository.save(entry);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> savedMap = new HashMap<>();
        savedMap.put("id", saved.getId());
        savedMap.put("salesVolume", saved.getSalesVolume());
        savedMap.put("productCategory", saved.getProductCategory());
        savedMap.put("month", saved.getMonth());
        savedMap.put("fiscalYear", saved.getFiscalYear());
        savedMap.put("dispatchDate", saved.getDispatchDate());
        savedMap.put("paymentDate", saved.getPaymentDate());
        savedMap.put("remarks", saved.getRemarks());
        savedMap.put("submittedAt", saved.getSubmittedAt());
        
        response.put("entry", savedMap);

        return ResponseEntity.ok(response);
    }

    /* ================================================================
     *  NEW: INVENTORY / LOW-STOCK ALERTS
     * ================================================================ */

    @GetMapping("/{userId}/inventory")
    public ResponseEntity<?> getInventory(@PathVariable Long userId) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Distributor not found"));
        }
        Distributor distributor = distOpt.get();
        List<Inventory> items = inventoryService.getByDistributor(distributor.getId());
        List<Map<String, Object>> result = items.stream().map(inv -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", inv.getId());
            m.put("productCategory", inv.getProductCategory());
            m.put("quantity", inv.getQuantity());
            m.put("threshold", inv.getThreshold());
            m.put("pricePerMt", inv.getPricePerMt());
            m.put("updatedAt", inv.getUpdatedAt());
            m.put("isLowStock", inv.getQuantity().compareTo(inv.getThreshold()) <= 0);
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/alerts")
    public ResponseEntity<?> getAlerts(@PathVariable Long userId) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Distributor not found"));
        }
        Distributor distributor = distOpt.get();
        List<Inventory> lowStock = inventoryService.getLowStockAlertsForDistributor(distributor.getId());
        List<Map<String, Object>> alerts = lowStock.stream().map(inv -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productCategory", inv.getProductCategory());
            m.put("currentStock", inv.getQuantity());
            m.put("threshold", inv.getThreshold());
            m.put("message", "Low stock alert: " + inv.getProductCategory() + " is at " + inv.getQuantity() + " MT (threshold: " + inv.getThreshold() + " MT)");
            return m;
        }).toList();
        return ResponseEntity.ok(alerts);
    }

    /* ================================================================
     *  NEW: ORDER PLACEMENT
     * ================================================================ */

    @GetMapping("/{userId}/orders")
    public ResponseEntity<?> getOrders(@PathVariable Long userId) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Distributor not found"));
        }
        Distributor distributor = distOpt.get();
        List<Order> orders = orderService.getByDistributor(distributor.getId());

        List<Map<String, Object>> orderMaps = orders.stream()
                .sorted(Comparator.comparing(Order::getPlacedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", o.getId());
                    m.put("productCategory", o.getProductCategory());
                    m.put("quantity", o.getQuantity());
                    m.put("pricePerMt", o.getPricePerMt());
                    m.put("totalPrice", o.getTotalPrice());
                    m.put("status", o.getStatus());
                    m.put("rejectReason", o.getRejectReason());
                    m.put("placedAt", o.getPlacedAt());
                    m.put("approvedAt", o.getApprovedAt());
                    m.put("paidAt", o.getPaidAt());
                    return m;
                }).toList();

        return ResponseEntity.ok(orderMaps);
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<?> placeOrder(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Distributor not found"));
        }
        Distributor distributor = distOpt.get();

        try {
            String productCategory = (String) body.get("productCategory");
            BigDecimal quantity = new BigDecimal(body.get("quantity").toString());

            Order order = orderService.placeOrder(distributor.getId(), productCategory, quantity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order placed successfully");
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("productCategory", order.getProductCategory());
            orderMap.put("quantity", order.getQuantity());
            orderMap.put("totalPrice", order.getTotalPrice());
            orderMap.put("status", order.getStatus());
            response.put("order", orderMap);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* ================================================================
     *  NEW: PAYMENT PROCESSING
     * ================================================================ */

    @PostMapping("/{userId}/payments")
    public ResponseEntity<?> makePayment(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Distributor not found"));
        }

        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            String paymentMethod = (String) body.get("paymentMethod");
            String cardLastFour = body.get("cardLastFour") != null ? body.get("cardLastFour").toString() : null;

            Payment payment = paymentService.processPayment(orderId, paymentMethod, cardLastFour);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment processed successfully");
            response.put("transactionRef", payment.getTransactionRef());
            response.put("amount", payment.getAmount());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* ================================================================
     *  NEW: LEDGER
     * ================================================================ */

    @GetMapping("/{userId}/ledger")
    public ResponseEntity<?> getLedger(@PathVariable Long userId) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Distributor not found"));
        }
        Distributor distributor = distOpt.get();

        List<LedgerEntry> entries = ledgerEntryRepository.findByDistributorIdOrderByTransactionDateDesc(distributor.getId());
        BigDecimal balance = ledgerEntryRepository.calculateBalanceByDistributorId(distributor.getId());

        List<Map<String, Object>> entryMaps = entries.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("entryType", e.getEntryType());
            m.put("productCategory", e.getProductCategory());
            m.put("quantityMt", e.getQuantityMt());
            m.put("debit", e.getDebit());
            m.put("credit", e.getCredit());
            m.put("balance", e.getBalance());
            m.put("transactionDate", e.getTransactionDate());
            m.put("remarks", e.getRemarks());
            return m;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("entries", entryMaps);
        result.put("currentBalance", balance != null ? balance : BigDecimal.ZERO);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{userId}/record-sale")
    public ResponseEntity<?> recordSale(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        Optional<Distributor> distOpt = distributorRepository.findByUserId(userId);
        if (distOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Distributor not found"));
        }
        Distributor distributor = distOpt.get();

        String productCategory = (String) body.get("productCategory");
        BigDecimal qty = new BigDecimal(body.get("quantity").toString());
        BigDecimal price = new BigDecimal(body.get("price").toString());

        boolean deducted = inventoryService.deductStock(distributor.getId(), productCategory, qty);
        if (!deducted) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Insufficient stock or item not found"));
        }

        BigDecimal currentBalance = ledgerEntryRepository.calculateBalanceByDistributorId(distributor.getId());
        if (currentBalance == null) currentBalance = BigDecimal.ZERO;

        LedgerEntry ledger = new LedgerEntry();
        ledger.setDistributor(distributor);
        ledger.setEntryType("SALE_OUT");
        ledger.setProductCategory(productCategory);
        ledger.setQuantityMt(qty);
        ledger.setDebit(BigDecimal.ZERO);
        ledger.setCredit(BigDecimal.ZERO);
        ledger.setBalance(currentBalance);
        ledger.setTransactionDate(LocalDateTime.now());
        ledger.setRemarks("Sale to customer: " + qty + " MT of " + productCategory + " @ " + price + "/MT");
        ledgerEntryRepository.save(ledger);

        return ResponseEntity.ok(Map.of("success", true, "message", "Sale recorded successfully"));
    }
}
