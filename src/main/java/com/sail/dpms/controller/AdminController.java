package com.sail.dpms.controller;

import com.sail.dpms.entity.*;
import com.sail.dpms.repository.*;
import com.sail.dpms.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DistributorRepository distributorRepository;
    private final SailUnitRepository sailUnitRepository;
    private final SalesTargetRepository salesTargetRepository;
    private final SalesEntryRepository salesEntryRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final LedgerEntryRepository ledgerEntryRepository;

    public AdminController(DistributorRepository distributorRepository,
                           SailUnitRepository sailUnitRepository,
                           SalesTargetRepository salesTargetRepository,
                           SalesEntryRepository salesEntryRepository,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           InventoryService inventoryService,
                           OrderService orderService,
                           PaymentService paymentService,
                           LedgerEntryRepository ledgerEntryRepository) {
        this.distributorRepository = distributorRepository;
        this.sailUnitRepository = sailUnitRepository;
        this.salesTargetRepository = salesTargetRepository;
        this.salesEntryRepository = salesEntryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        long totalDistributors = distributorRepository.count();
        dashboard.put("totalDistributors", totalDistributors);

        // Calculate total sales volume in MT
        List<SalesEntry> allSales = salesEntryRepository.findAll();
        BigDecimal totalSales = allSales.stream()
                .map(s -> s.getSalesVolume() != null ? s.getSalesVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.put("totalSales", totalSales);

        // Calculate average performance and find the best distributor
        List<Distributor> distributors = distributorRepository.findAll();
        double totalPerformance = 0.0;
        int distributorCount = 0;

        Distributor bestDistributor = null;
        double bestScore = -1.0;

        for (Distributor dist : distributors) {
            List<SalesEntry> distSales = salesEntryRepository.findByDistributorId(dist.getId());
            List<SalesTarget> distTargets = salesTargetRepository.findByDistributorId(dist.getId());

            BigDecimal salesSum = distSales.stream()
                    .map(s -> s.getSalesVolume() != null ? s.getSalesVolume() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal targetSum = distTargets.stream()
                    .map(t -> t.getTargetVolume() != null ? t.getTargetVolume() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (targetSum.compareTo(BigDecimal.ZERO) > 0) {
                double score = salesSum.divide(targetSum, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).doubleValue();
                totalPerformance += score;
                distributorCount++;

                if (score > bestScore) {
                    bestScore = score;
                    bestDistributor = dist;
                }
            }
        }

        double avgPerformance = distributorCount > 0 ? totalPerformance / distributorCount : 0.0;
        dashboard.put("avgPerformance", Math.round(avgPerformance * 100.0) / 100.0);

        if (bestDistributor != null) {
            dashboard.put("bestDistributorName", bestDistributor.getName());
            dashboard.put("bestDistributorUnit", bestDistributor.getUnit() != null ? bestDistributor.getUnit().getShortCode() : "SAIL");
            dashboard.put("bestDistributorScore", Math.round(bestScore * 100.0) / 100.0);
        } else {
            dashboard.put("bestDistributorName", "N/A");
            dashboard.put("bestDistributorUnit", "SAIL");
            dashboard.put("bestDistributorScore", 0.0);
        }

        // Add pending orders count and low-stock alerts count
        List<Order> pendingOrders = orderService.getPendingOrders();
        dashboard.put("pendingOrdersCount", pendingOrders.size());

        List<Inventory> lowStock = inventoryService.getLowStockAlerts();
        dashboard.put("lowStockAlertsCount", lowStock.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/distributors")
    public ResponseEntity<List<Map<String, Object>>> getDistributors() {
        List<Distributor> distributors = distributorRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Distributor dist : distributors) {
            Map<String, Object> distMap = new HashMap<>();
            distMap.put("id", dist.getId());
            distMap.put("name", dist.getName());
            distMap.put("contactEmail", dist.getContactEmail());
            distMap.put("contactPhone", dist.getContactPhone());
            distMap.put("region", dist.getRegion());

            if (dist.getUnit() != null) {
                Map<String, Object> unitMap = new HashMap<>();
                unitMap.put("id", dist.getUnit().getId());
                unitMap.put("name", dist.getUnit().getName());
                unitMap.put("shortCode", dist.getUnit().getShortCode());
                unitMap.put("location", dist.getUnit().getLocation());
                distMap.put("unit", unitMap);
            }

            List<SalesEntry> distSales = salesEntryRepository.findByDistributorId(dist.getId());
            List<SalesTarget> distTargets = salesTargetRepository.findByDistributorId(dist.getId());

            BigDecimal totalSalesAmt = distSales.stream()
                    .map(s -> s.getSalesVolume() != null ? s.getSalesVolume() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalTarget = distTargets.stream()
                    .map(t -> t.getTargetVolume() != null ? t.getTargetVolume() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double performanceScore = 0.0;
            if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
                performanceScore = totalSalesAmt.divide(totalTarget, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).doubleValue();
                performanceScore = Math.min(performanceScore, 100.0);
            }

            distMap.put("totalSales", totalSalesAmt);
            distMap.put("totalTarget", totalTarget);
            distMap.put("performanceScore", Math.round(performanceScore * 100.0) / 100.0);

            if (dist.getUser() != null) {
                distMap.put("userId", dist.getUser().getId());
            }

            result.add(distMap);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/units")
    public ResponseEntity<List<SailUnit>> getUnits() {
        return ResponseEntity.ok(sailUnitRepository.findAll());
    }

    @PostMapping("/distributors")
    public ResponseEntity<Map<String, Object>> createDistributor(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");

        if (password == null || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Password must be at least 8 characters long, and include at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&)."));
        }

        String name = (String) body.get("name");
        Long unitId = Long.valueOf(body.get("unitId").toString());
        String contactEmail = (String) body.get("contactEmail");
        String contactPhone = (String) body.get("contactPhone");
        String region = (String) body.get("region");

        // Create user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("DISTRIBUTOR");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Create distributor
        SailUnit unit = sailUnitRepository.findById(unitId).orElse(null);
        Distributor distributor = new Distributor();
        distributor.setUser(user);
        distributor.setUnit(unit);
        distributor.setName(name);
        distributor.setContactEmail(contactEmail);
        distributor.setContactPhone(contactPhone);
        distributor.setRegion(region);
        distributor.setCreatedAt(LocalDateTime.now());
        distributorRepository.save(distributor);

        Map<String, Object> distMap = new HashMap<>();
        distMap.put("id", distributor.getId());
        distMap.put("name", distributor.getName());
        distMap.put("contactEmail", distributor.getContactEmail());
        distMap.put("contactPhone", distributor.getContactPhone());
        distMap.put("region", distributor.getRegion());
        distMap.put("userId", user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("distributor", distMap);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/plant-inventory")
    public ResponseEntity<List<Map<String, Object>>> getPlantInventory() {
        List<Inventory> inventoryList = inventoryService.getPlantInventory();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Inventory inv : inventoryList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", inv.getId());
            if (inv.getUnit() != null) {
                map.put("unitName", inv.getUnit().getName());
                map.put("unitCode", inv.getUnit().getShortCode());
                map.put("dailyCapacity", inv.getUnit().getDailyCapacity());
            }
            map.put("productCategory", inv.getProductCategory());
            map.put("quantity", inv.getQuantity());
            map.put("pricePerMt", inv.getPricePerMt());
            map.put("threshold", inv.getThreshold());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/plant-inventory/{id}")
    public ResponseEntity<?> updatePlantInventory(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Inventory inv = inventoryService.findById(id).orElse(null);
        if (inv == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Inventory item not found"));
        }
        if (updates.containsKey("quantity")) {
            inv.setQuantity(new BigDecimal(updates.get("quantity").toString()));
        }
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryService.save(inv);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/plant-inventory")
    public ResponseEntity<?> createPlantInventory(@RequestBody Map<String, Object> body) {
        String unitCode = (String) body.get("unitCode");
        String productCategory = (String) body.get("productCategory");
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());

        SailUnit unit = sailUnitRepository.findAll().stream()
                .filter(u -> u.getShortCode().equals(unitCode))
                .findFirst().orElse(null);

        if (unit == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Unit not found"));
        }

        Inventory inv = new Inventory();
        inv.setUnit(unit);
        inv.setProductCategory(productCategory);
        inv.setQuantity(quantity);
        inv.setThreshold(new BigDecimal("1000.00")); // default
        inv.setPricePerMt(new BigDecimal("45000.00")); // default
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryService.save(inv);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/targets")
    public ResponseEntity<Map<String, Object>> createTarget(@RequestBody Map<String, Object> body) {
        Long distributorId = Long.valueOf(body.get("distributorId").toString());
        BigDecimal targetVolume = BigDecimal.ZERO;
        if (body.get("targetVolume") != null) {
            targetVolume = new BigDecimal(body.get("targetVolume").toString());
        } else if (body.get("targetAmount") != null) {
            targetVolume = new BigDecimal(body.get("targetAmount").toString());
        }
        String fiscalYear = (String) body.get("fiscalYear");
        String quarter = (String) body.get("quarter");

        Distributor distributor = distributorRepository.findById(distributorId).orElse(null);

        SalesTarget target = new SalesTarget();
        target.setDistributor(distributor);
        target.setTargetVolume(targetVolume);
        target.setFiscalYear(fiscalYear);
        target.setQuarter(quarter);
        target.setAssignedAt(LocalDateTime.now());
        SalesTarget saved = salesTargetRepository.save(target);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("targetId", saved.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/distributors/{id}/analysis")
    public ResponseEntity<?> getDistributorAnalysis(@PathVariable Long id) {
        Optional<Distributor> distOpt = distributorRepository.findById(id);
        if (distOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Distributor not found");
            return ResponseEntity.status(404).body(error);
        }
        Distributor distributor = distOpt.get();

        List<SalesEntry> sales = salesEntryRepository.findByDistributorId(id);
        List<SalesTarget> targets = salesTargetRepository.findByDistributorId(id);

        BigDecimal totalSales = sales.stream()
                .map(s -> s.getSalesVolume() != null ? s.getSalesVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTarget = targets.stream()
                .map(t -> t.getTargetVolume() != null ? t.getTargetVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double performanceScore = 0.0;
        if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
            performanceScore = totalSales.divide(totalTarget, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        // Calculate monthly sales achievement (summed by month)
        Map<String, BigDecimal> monthlySales = new LinkedHashMap<>();
        String[] months = {"April", "May", "June", "July", "August", "September", "October", "November", "December", "January", "February", "March"};
        for (String m : months) {
            monthlySales.put(m, BigDecimal.ZERO);
        }

        for (SalesEntry entry : sales) {
            String m = entry.getMonth();
            if (m != null && !m.trim().isEmpty()) {
                String normMonth = m.substring(0, 1).toUpperCase() + m.substring(1).toLowerCase();
                if (monthlySales.containsKey(normMonth)) {
                    monthlySales.put(normMonth, monthlySales.get(normMonth).add(entry.getSalesVolume()));
                } else {
                    monthlySales.merge(normMonth, entry.getSalesVolume(), BigDecimal::add);
                }
            }
        }

        Map<String, Object> distInfo = new HashMap<>();
        distInfo.put("id", distributor.getId());
        distInfo.put("name", distributor.getName());
        distInfo.put("contactEmail", distributor.getContactEmail());
        distInfo.put("contactPhone", distributor.getContactPhone());
        distInfo.put("region", distributor.getRegion());
        distInfo.put("createdAt", distributor.getCreatedAt());
        if (distributor.getUnit() != null) {
            Map<String, Object> unitInfo = new HashMap<>();
            unitInfo.put("id", distributor.getUnit().getId());
            unitInfo.put("name", distributor.getUnit().getName());
            unitInfo.put("shortCode", distributor.getUnit().getShortCode());
            unitInfo.put("location", distributor.getUnit().getLocation());
            distInfo.put("unit", unitInfo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("distributor", distInfo);
        result.put("totalSales", totalSales);
        result.put("totalTarget", totalTarget);
        result.put("performanceScore", Math.round(performanceScore * 100.0) / 100.0);
        result.put("monthlySales", monthlySales);

        List<Map<String, Object>> ledger = sales.stream()
                .sorted(Comparator.comparing(SalesEntry::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(s -> {
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
                }).toList();
        result.put("salesEntries", ledger);

        List<Map<String, Object>> targetList = targets.stream()
                .sorted(Comparator.comparing(SalesTarget::getFiscalYear).thenComparing(SalesTarget::getQuarter))
                .map(t -> {
                    Map<String, Object> tMap = new HashMap<>();
                    tMap.put("id", t.getId());
                    tMap.put("targetVolume", t.getTargetVolume());
                    tMap.put("fiscalYear", t.getFiscalYear());
                    tMap.put("quarter", t.getQuarter());
                    tMap.put("assignedAt", t.getAssignedAt());
                    return tMap;
                }).toList();
        result.put("targets", targetList);

        return ResponseEntity.ok(result);
    }

    /* ================================================================
     *  NEW: INVENTORY MANAGEMENT ENDPOINTS
     * ================================================================ */

    @GetMapping("/inventory")
    public ResponseEntity<List<Map<String, Object>>> getAllInventory() {
        List<Inventory> items = inventoryService.getAll();
        return ResponseEntity.ok(mapInventoryList(items));
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<Map<String, Object>>> getLowStockAlerts() {
        List<Inventory> items = inventoryService.getLowStockAlerts();
        return ResponseEntity.ok(mapInventoryList(items));
    }

    @PostMapping("/inventory")
    public ResponseEntity<Map<String, Object>> upsertInventory(@RequestBody Map<String, Object> body) {
        Long distributorId = Long.valueOf(body.get("distributorId").toString());
        String productCategory = (String) body.get("productCategory");
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        BigDecimal threshold = new BigDecimal(body.get("threshold").toString());
        BigDecimal pricePerMt = new BigDecimal(body.get("pricePerMt").toString());

        Distributor dist = distributorRepository.findById(distributorId).orElse(null);
        if (dist == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Distributor not found"));
        }

        // Check if inventory already exists for this distributor + category
        var existing = inventoryService.getByDistributor(distributorId).stream()
                .filter(i -> i.getProductCategory().equals(productCategory))
                .findFirst();

        Inventory inv;
        if (existing.isPresent()) {
            inv = existing.get();
            inv.setQuantity(quantity);
            inv.setThreshold(threshold);
            inv.setPricePerMt(pricePerMt);
        } else {
            inv = new Inventory();
            inv.setDistributor(dist);
            inv.setUnit(dist.getUnit());
            inv.setProductCategory(productCategory);
            inv.setQuantity(quantity);
            inv.setThreshold(threshold);
            inv.setPricePerMt(pricePerMt);
        }

        Inventory saved = inventoryService.save(inv);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("inventory", mapInventory(saved));
        return ResponseEntity.ok(response);
    }

    /* ================================================================
     *  NEW: ORDER MANAGEMENT ENDPOINTS
     * ================================================================ */

    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        List<Order> orders = orderService.getAll();
        return ResponseEntity.ok(mapOrderList(orders));
    }

    @GetMapping("/orders/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingOrders() {
        List<Order> orders = orderService.getPendingOrders();
        return ResponseEntity.ok(mapOrderList(orders));
    }

    @PostMapping("/orders/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveOrder(@PathVariable Long id) {
        try {
            Order order = orderService.approveOrder(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("order", mapOrder(order));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "Rejected by admin");
            Order order = orderService.rejectOrder(id, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("order", mapOrder(order));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* ================================================================
     *  NEW: LEDGER OVERVIEW ENDPOINTS
     * ================================================================ */

    @GetMapping("/ledger/{distributorId}")
    public ResponseEntity<Map<String, Object>> getDistributorLedger(@PathVariable Long distributorId) {
        List<LedgerEntry> entries = ledgerEntryRepository.findByDistributorIdOrderByTransactionDateDesc(distributorId);
        BigDecimal balance = ledgerEntryRepository.calculateBalanceByDistributorId(distributorId);

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

    /* ================================================================
     *  HELPER METHODS for JSON Mapping
     * ================================================================ */

    private List<Map<String, Object>> mapInventoryList(List<Inventory> items) {
        return items.stream().map(this::mapInventory).toList();
    }

    private Map<String, Object> mapInventory(Inventory inv) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", inv.getId());
        m.put("productCategory", inv.getProductCategory());
        m.put("quantity", inv.getQuantity());
        m.put("threshold", inv.getThreshold());
        m.put("pricePerMt", inv.getPricePerMt());
        m.put("updatedAt", inv.getUpdatedAt());
        m.put("isLowStock", inv.getQuantity().compareTo(inv.getThreshold()) <= 0);
        if (inv.getDistributor() != null) {
            m.put("distributorId", inv.getDistributor().getId());
            m.put("distributorName", inv.getDistributor().getName());
        }
        if (inv.getUnit() != null) {
            m.put("unitId", inv.getUnit().getId());
            m.put("unitShortCode", inv.getUnit().getShortCode());
            m.put("unitName", inv.getUnit().getName());
        }
        return m;
    }

    private List<Map<String, Object>> mapOrderList(List<Order> orders) {
        return orders.stream().map(this::mapOrder).toList();
    }

    private Map<String, Object> mapOrder(Order o) {
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
        if (o.getDistributor() != null) {
            m.put("distributorId", o.getDistributor().getId());
            m.put("distributorName", o.getDistributor().getName());
        }
        if (o.getUnit() != null) {
            m.put("unitShortCode", o.getUnit().getShortCode());
        }
        return m;
    }
}
