package com.sail.dpms.config;

import com.sail.dpms.entity.*;
import com.sail.dpms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SailUnitRepository sailUnitRepository;
    private final DistributorRepository distributorRepository;
    private final SalesTargetRepository salesTargetRepository;
    private final SalesEntryRepository salesEntryRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           SailUnitRepository sailUnitRepository,
                           DistributorRepository distributorRepository,
                           SalesTargetRepository salesTargetRepository,
                           SalesEntryRepository salesEntryRepository,
                           InventoryRepository inventoryRepository,
                           OrderRepository orderRepository,
                           PaymentRepository paymentRepository,
                           LedgerEntryRepository ledgerEntryRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sailUnitRepository = sailUnitRepository;
        this.distributorRepository = distributorRepository;
        this.salesTargetRepository = salesTargetRepository;
        this.salesEntryRepository = salesEntryRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("ℹ️ Database already seeded. Skipping initialization.");
            return;
        }

        // ═══════════════════════════════════════════════════════
        // 1. ADMIN USER
        // ═══════════════════════════════════════════════════════
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("Admin@1234"));
        admin.setRole("ADMIN");
        admin.setCreatedAt(LocalDateTime.of(2024, 1, 15, 9, 0));
        userRepository.save(admin);

        // ═══════════════════════════════════════════════════════
        // 2. SAIL INTEGRATED STEEL PLANTS
        // ═══════════════════════════════════════════════════════
        SailUnit bsp = createUnit("Bhilai Steel Plant", "Bhilai, Chhattisgarh", "BSP",
                "India's flagship integrated steel plant producing world-class rails, heavy structurals, plates, and wire rods. Bhilai is the sole supplier of rails to Indian Railways.",
                "9,500 MT/Day");
        SailUnit bsl = createUnit("Bokaro Steel Plant", "Bokaro, Jharkhand", "BSL",
                "One of the largest integrated steel plants specializing in hot-rolled and cold-rolled coils, galvanised products, and tin plates for automotive and consumer sectors.",
                "12,600 MT/Day");
        SailUnit rsp = createUnit("Rourkela Steel Plant", "Rourkela, Odisha", "RSP",
                "India's first public-sector steel plant with a dedicated Silicon Steel Mill and state-of-the-art Plate Mill. Major producer of electrical steel and API-grade plates.",
                "6,000 MT/Day");
        SailUnit dsp = createUnit("Durgapur Steel Plant", "Durgapur, West Bengal", "DSP",
                "A key producer of structural steel, skelp, and specialised wheels & axles for Indian Railways, with a modernised bar mill and medium merchant mill.",
                "5,800 MT/Day");
        SailUnit isp = createUnit("IISCO Steel Plant", "Burnpur, West Bengal", "ISP",
                "Recently modernised with a new 2.5 MTPA blast furnace, coke oven battery, and state-of-the-art Universal Rail Mill producing 260m long rails.",
                "7,100 MT/Day");
        sailUnitRepository.saveAll(List.of(bsp, bsl, rsp, dsp, isp));

        // ═══════════════════════════════════════════════════════
        // 3. DISTRIBUTOR 1: Rajesh Kumar Sharma (Bhilai Region)
        //    Login: dist_1 / Rahul@1234
        // ═══════════════════════════════════════════════════════
        User user1 = createUser("dist_1", "Rahul@1234", "DISTRIBUTOR", LocalDateTime.of(2024, 3, 10, 10, 30));
        Distributor dist1 = createDistributor(user1, bsp, "Rajesh Kumar Sharma",
                "rajesh.sharma@sailsteel.co.in", "+91-78691-43210", "Chhattisgarh & Central India",
                LocalDateTime.of(2024, 3, 10, 10, 30));

        // ═══════════════════════════════════════════════════════
        // 4. DISTRIBUTOR 2: Anita Devi Singh (Bokaro Region)
        //    Login: dist_2 / Anita@5678
        // ═══════════════════════════════════════════════════════
        User user2 = createUser("dist_2", "Anita@5678", "DISTRIBUTOR", LocalDateTime.of(2024, 4, 5, 11, 0));
        Distributor dist2 = createDistributor(user2, bsl, "Anita Devi Singh",
                "anita.singh@sailsteel.co.in", "+91-94315-67890", "Jharkhand & Eastern India",
                LocalDateTime.of(2024, 4, 5, 11, 0));

        // ═══════════════════════════════════════════════════════
        // 5. DISTRIBUTOR 3: Manoj Kumar Patel (Rourkela Region)
        //    Login: dist_3 / Manoj@9012
        // ═══════════════════════════════════════════════════════
        User user3 = createUser("dist_3", "Manoj@9012", "DISTRIBUTOR", LocalDateTime.of(2024, 5, 20, 14, 15));
        Distributor dist3 = createDistributor(user3, rsp, "Manoj Kumar Patel",
                "manoj.patel@sailsteel.co.in", "+91-87654-32109", "Odisha & Southern Region",
                LocalDateTime.of(2024, 5, 20, 14, 15));

        // ═══════════════════════════════════════════════════════
        // 6. CENTRAL PLANT INVENTORIES (Admin/Factory Stock)
        // ═══════════════════════════════════════════════════════

        // Bhilai Steel Plant (BSP) — Rails, TMT, HR Coils, Plates
        createInventory(null, bsp, "TMT Bars Fe-500D 8mm",   bd("5200.00"), bd("1000.00"), bd("42500.00"));
        createInventory(null, bsp, "TMT Bars Fe-500D 10mm",  bd("4750.00"), bd("1000.00"), bd("43200.00"));
        createInventory(null, bsp, "TMT Bars Fe-500D 12mm",  bd("3100.00"), bd("800.00"),  bd("44000.00"));
        createInventory(null, bsp, "TMT Bars Fe-500D 16mm",  bd("2800.00"), bd("700.00"),  bd("44500.00"));
        createInventory(null, bsp, "HR Coils IS:2062",       bd("8500.00"), bd("2000.00"), bd("48200.00"));
        createInventory(null, bsp, "Plates IS:2062 E250",    bd("2100.00"), bd("500.00"),  bd("52400.00"));
        createInventory(null, bsp, "Rails 60Kg/m UIC",       bd("15000.00"), bd("5000.00"), bd("55800.00"));
        createInventory(null, bsp, "Wire Rods 5.5mm",        bd("3400.00"), bd("800.00"),  bd("43800.00"));

        // Bokaro Steel Plant (BSL) — CR, GP, HR, Tin Plates
        createInventory(null, bsl, "CR Sheets IS:513",       bd("4200.00"), bd("800.00"),  bd("50500.00"));
        createInventory(null, bsl, "GP Sheets IS:277",       bd("3600.00"), bd("800.00"),  bd("52200.00"));
        createInventory(null, bsl, "HR Coils IS:2062",       bd("9200.00"), bd("2000.00"), bd("47500.00"));
        createInventory(null, bsl, "Tin Plates IS:1993",     bd("1800.00"), bd("400.00"),  bd("68000.00"));
        createInventory(null, bsl, "TMT Bars Fe-500D 16mm",  bd("6200.00"), bd("1500.00"), bd("45200.00"));

        // Rourkela Steel Plant (RSP) — Silicon Steel, Plates, Structurals
        createInventory(null, rsp, "CRGO Silicon Steel",     bd("1250.00"), bd("300.00"),  bd("85000.00"));
        createInventory(null, rsp, "Plates API 5L X-65",     bd("4600.00"), bd("1000.00"), bd("58500.00"));
        createInventory(null, rsp, "Structural Steel IS:2062", bd("5800.00"), bd("1200.00"), bd("46800.00"));
        createInventory(null, rsp, "HR Coils IS:2062",       bd("3200.00"), bd("800.00"),  bd("48000.00"));

        // Durgapur Steel Plant (DSP) — Wheels, Axles, Structurals
        createInventory(null, dsp, "Wheels R-19 (IRS)",      bd("8200.00"), bd("1500.00"), bd("62000.00"));
        createInventory(null, dsp, "Axles AAR-M (IRS)",      bd("6800.00"), bd("1200.00"), bd("58500.00"));
        createInventory(null, dsp, "Medium Structurals",     bd("4500.00"), bd("1000.00"), bd("44200.00"));

        // IISCO Steel Plant (ISP) — Wire Rods, Bars
        createInventory(null, isp, "Wire Rods 5.5mm SAE1008", bd("5200.00"), bd("1200.00"), bd("42800.00"));
        createInventory(null, isp, "TMT Bars Fe-500D 12mm",   bd("3800.00"), bd("900.00"),  bd("44500.00"));

        // ═══════════════════════════════════════════════════════
        // 7. DISTRIBUTOR-LEVEL INVENTORIES (with threshold alerts)
        // ═══════════════════════════════════════════════════════

        // Dist 1 (Rajesh/Bhilai): TMT 8mm BELOW threshold → triggers alert
        createInventory(dist1, bsp, "TMT Bars Fe-500D 8mm",  bd("28.50"),  bd("100.00"), bd("45200.00"));
        createInventory(dist1, bsp, "TMT Bars Fe-500D 12mm", bd("180.00"), bd("100.00"), bd("46500.00"));
        createInventory(dist1, bsp, "HR Coils IS:2062",      bd("245.00"), bd("150.00"), bd("51000.00"));

        // Dist 2 (Anita/Bokaro): CR Sheets BELOW threshold → triggers alert
        createInventory(dist2, bsl, "CR Sheets IS:513",      bd("42.00"),  bd("80.00"),  bd("53500.00"));
        createInventory(dist2, bsl, "GP Sheets IS:277",      bd("125.00"), bd("80.00"),  bd("55000.00"));

        // Dist 3 (Manoj/Rourkela): All stock healthy
        createInventory(dist3, rsp, "CRGO Silicon Steel",    bd("95.00"),  bd("50.00"),  bd("88000.00"));
        createInventory(dist3, rsp, "Plates API 5L X-65",    bd("310.00"), bd("150.00"), bd("61500.00"));

        // ═══════════════════════════════════════════════════════
        // 8. SALES TARGETS (FY 2025-26)
        // ═══════════════════════════════════════════════════════

        // Rajesh Kumar Sharma (dist_1)
        createTarget(dist1, bd("2500.00"), "2025-26", "Q1");
        createTarget(dist1, bd("3200.00"), "2025-26", "Q2");
        createTarget(dist1, bd("2800.00"), "2025-26", "Q3");
        createTarget(dist1, bd("3500.00"), "2025-26", "Q4");

        // Anita Devi Singh (dist_2)
        createTarget(dist2, bd("2000.00"), "2025-26", "Q1");
        createTarget(dist2, bd("2400.00"), "2025-26", "Q2");
        createTarget(dist2, bd("2200.00"), "2025-26", "Q3");

        // Manoj Kumar Patel (dist_3)
        createTarget(dist3, bd("1800.00"), "2025-26", "Q1");
        createTarget(dist3, bd("2100.00"), "2025-26", "Q2");

        // ═══════════════════════════════════════════════════════
        // 9. SALES ENTRIES (Monthly performance records)
        // ═══════════════════════════════════════════════════════

        // Rajesh Kumar Sharma — April to September 2025
        createSalesEntry(dist1, bd("820.00"),  "TMT Bars Fe-500D 8mm",  "April",     "2025-26",
                LocalDate.of(2025, 4, 8),  LocalDate.of(2025, 4, 22),  "First dispatch post-monsoon restocking");
        createSalesEntry(dist1, bd("450.00"),  "HR Coils IS:2062",      "April",     "2025-26",
                LocalDate.of(2025, 4, 15), LocalDate.of(2025, 4, 28),  "Construction season demand — Raipur Metro project");
        createSalesEntry(dist1, bd("680.00"),  "TMT Bars Fe-500D 12mm", "May",       "2025-26",
                LocalDate.of(2025, 5, 5),  LocalDate.of(2025, 5, 18),  "Bulk order for Godrej Properties — Bilaspur site");
        createSalesEntry(dist1, bd("920.00"),  "TMT Bars Fe-500D 8mm",  "June",      "2025-26",
                LocalDate.of(2025, 6, 3),  LocalDate.of(2025, 6, 15),  "Monsoon pre-stocking by dealers in Durg & Rajnandgaon");
        createSalesEntry(dist1, bd("310.00"),  "Plates IS:2062 E250",   "June",      "2025-26",
                LocalDate.of(2025, 6, 12), LocalDate.of(2025, 6, 25),  "Bridge fabrication order — NH-130 widening project");
        createSalesEntry(dist1, bd("780.00"),  "TMT Bars Fe-500D 10mm", "July",      "2025-26",
                LocalDate.of(2025, 7, 2),  LocalDate.of(2025, 7, 14),  "Government housing scheme — PMAY Chhattisgarh");
        createSalesEntry(dist1, bd("550.00"),  "HR Coils IS:2062",      "August",    "2025-26",
                LocalDate.of(2025, 8, 8),  LocalDate.of(2025, 8, 20),  "Auto ancillary demand — Bhilai Industrial Area");
        createSalesEntry(dist1, bd("1050.00"), "TMT Bars Fe-500D 8mm",  "September", "2025-26",
                LocalDate.of(2025, 9, 1),  LocalDate.of(2025, 9, 12),  "Post-monsoon construction surge — festival season demand");

        // Anita Devi Singh — April to August 2025
        createSalesEntry(dist2, bd("620.00"),  "CR Sheets IS:513",      "April",     "2025-26",
                LocalDate.of(2025, 4, 10), LocalDate.of(2025, 4, 23),  "Tata Motors vendor supply — Jamshedpur plant");
        createSalesEntry(dist2, bd("480.00"),  "GP Sheets IS:277",      "May",       "2025-26",
                LocalDate.of(2025, 5, 7),  LocalDate.of(2025, 5, 19),  "Roofing sheet manufacturer orders — Dhanbad & Giridih");
        createSalesEntry(dist2, bd("750.00"),  "HR Coils IS:2062",      "June",      "2025-26",
                LocalDate.of(2025, 6, 4),  LocalDate.of(2025, 6, 16),  "Industrial tube manufacturers — Adityapur MSME cluster");
        createSalesEntry(dist2, bd("520.00"),  "CR Sheets IS:513",      "July",      "2025-26",
                LocalDate.of(2025, 7, 8),  LocalDate.of(2025, 7, 20),  "White goods panel supply — Ranchi consumer electronics hub");
        createSalesEntry(dist2, bd("380.00"),  "TMT Bars Fe-500D 16mm", "August",    "2025-26",
                LocalDate.of(2025, 8, 5),  LocalDate.of(2025, 8, 17),  "Flyover construction — Bokaro-Dhanbad expressway");

        // Manoj Kumar Patel — April to July 2025
        createSalesEntry(dist3, bd("420.00"),  "CRGO Silicon Steel",    "April",     "2025-26",
                LocalDate.of(2025, 4, 12), LocalDate.of(2025, 4, 25),  "Transformer core supply — OPTCL grid expansion");
        createSalesEntry(dist3, bd("680.00"),  "Plates API 5L X-65",    "May",       "2025-26",
                LocalDate.of(2025, 5, 9),  LocalDate.of(2025, 5, 22),  "Pipeline fabrication — Indian Oil Paradip refinery");
        createSalesEntry(dist3, bd("350.00"),  "Structural Steel IS:2062", "June",   "2025-26",
                LocalDate.of(2025, 6, 6),  LocalDate.of(2025, 6, 18),  "Industrial shed construction — Kalinganagar industrial park");
        createSalesEntry(dist3, bd("510.00"),  "Plates API 5L X-65",    "July",      "2025-26",
                LocalDate.of(2025, 7, 3),  LocalDate.of(2025, 7, 15),  "GAIL pipeline extension — Angul to Dhamra corridor");

        // ═══════════════════════════════════════════════════════
        // 10. ORDERS (Full lifecycle: PENDING, APPROVED, PAID, REJECTED)
        // ═══════════════════════════════════════════════════════

        // --- Rajesh Kumar Sharma Orders ---

        // Order 1: PAID — TMT Bars 8mm, placed April 2025
        Order o1 = createOrder(dist1, bsp, "TMT Bars Fe-500D 8mm", bd("350.00"), bd("45200.00"),
                "PAID", LocalDateTime.of(2025, 4, 5, 10, 30), LocalDateTime.of(2025, 4, 6, 14, 0),
                LocalDateTime.of(2025, 4, 8, 11, 15), null);

        // Order 2: PAID — HR Coils, placed May 2025
        Order o2 = createOrder(dist1, bsp, "HR Coils IS:2062", bd("200.00"), bd("51000.00"),
                "PAID", LocalDateTime.of(2025, 5, 10, 9, 45), LocalDateTime.of(2025, 5, 11, 10, 30),
                LocalDateTime.of(2025, 5, 13, 16, 0), null);

        // Order 3: APPROVED (awaiting payment) — TMT Bars 12mm, placed June 2025
        Order o3 = createOrder(dist1, bsp, "TMT Bars Fe-500D 12mm", bd("420.00"), bd("46500.00"),
                "APPROVED", LocalDateTime.of(2025, 6, 18, 11, 0), LocalDateTime.of(2025, 6, 19, 9, 30),
                null, null);

        // Order 4: PENDING — Plates, placed recently
        createOrder(dist1, bsp, "Plates IS:2062 E250", bd("150.00"), bd("55000.00"),
                "PENDING", LocalDateTime.of(2025, 9, 15, 14, 30), null, null, null);

        // --- Anita Devi Singh Orders ---

        // Order 5: PAID — CR Sheets, placed April 2025
        Order o5 = createOrder(dist2, bsl, "CR Sheets IS:513", bd("280.00"), bd("53500.00"),
                "PAID", LocalDateTime.of(2025, 4, 8, 10, 0), LocalDateTime.of(2025, 4, 9, 11, 0),
                LocalDateTime.of(2025, 4, 11, 15, 30), null);

        // Order 6: APPROVED — GP Sheets, placed July 2025
        Order o6 = createOrder(dist2, bsl, "GP Sheets IS:277", bd("180.00"), bd("55000.00"),
                "APPROVED", LocalDateTime.of(2025, 7, 14, 9, 0), LocalDateTime.of(2025, 7, 15, 10, 45),
                null, null);

        // Order 7: REJECTED — TMT Bars 16mm (exceeded credit limit)
        createOrder(dist2, bsl, "TMT Bars Fe-500D 16mm", bd("500.00"), bd("45200.00"),
                "REJECTED", LocalDateTime.of(2025, 8, 2, 11, 30), null, null,
                "Order exceeds distributor credit limit of ₹1.5 Cr. Outstanding balance: ₹1.62 Cr.");

        // --- Manoj Kumar Patel Orders ---

        // Order 8: PAID — Silicon Steel, placed May 2025
        Order o8 = createOrder(dist3, rsp, "CRGO Silicon Steel", bd("120.00"), bd("88000.00"),
                "PAID", LocalDateTime.of(2025, 5, 6, 10, 15), LocalDateTime.of(2025, 5, 7, 11, 30),
                LocalDateTime.of(2025, 5, 9, 14, 0), null);

        // Order 9: PENDING — Plates, recent order
        createOrder(dist3, rsp, "Plates API 5L X-65", bd("250.00"), bd("61500.00"),
                "PENDING", LocalDateTime.of(2025, 9, 10, 13, 0), null, null, null);

        // ═══════════════════════════════════════════════════════
        // 11. PAYMENTS (For all PAID orders)
        // ═══════════════════════════════════════════════════════

        createPayment(o1, dist1, o1.getTotalPrice(), "NET_BANKING",
                "SAIL-TXN-20250408-BH001", null, LocalDateTime.of(2025, 4, 8, 11, 15));

        createPayment(o2, dist1, o2.getTotalPrice(), "UPI",
                "SAIL-TXN-20250513-BH002", null, LocalDateTime.of(2025, 5, 13, 16, 0));

        createPayment(o5, dist2, o5.getTotalPrice(), "NET_BANKING",
                "SAIL-TXN-20250411-BK001", null, LocalDateTime.of(2025, 4, 11, 15, 30));

        createPayment(o8, dist3, o8.getTotalPrice(), "CREDIT_CARD",
                "SAIL-TXN-20250509-RK001", "4532", LocalDateTime.of(2025, 5, 9, 14, 0));

        // ═══════════════════════════════════════════════════════
        // 12. LEDGER ENTRIES (Financial trail for each distributor)
        // ═══════════════════════════════════════════════════════

        // --- Rajesh Kumar Sharma Ledger ---
        BigDecimal rajeshBal = BigDecimal.ZERO;

        // Material sent for Order 1
        rajeshBal = rajeshBal.add(o1.getTotalPrice());
        createLedger(dist1, o1, "MATERIAL_SENT", "TMT Bars Fe-500D 8mm", bd("350.00"),
                o1.getTotalPrice(), BigDecimal.ZERO, rajeshBal,
                LocalDateTime.of(2025, 4, 7, 9, 0),
                "Dispatched 350 MT TMT 8mm via Rail Rake RR-BSP-04221");

        // Payment received for Order 1
        rajeshBal = rajeshBal.subtract(o1.getTotalPrice());
        createLedger(dist1, o1, "PAYMENT_RECEIVED", "TMT Bars Fe-500D 8mm", bd("350.00"),
                BigDecimal.ZERO, o1.getTotalPrice(), rajeshBal,
                LocalDateTime.of(2025, 4, 8, 11, 15),
                "Full payment received — Ref: SAIL-TXN-20250408-BH001");

        // Material sent for Order 2
        rajeshBal = rajeshBal.add(o2.getTotalPrice());
        createLedger(dist1, o2, "MATERIAL_SENT", "HR Coils IS:2062", bd("200.00"),
                o2.getTotalPrice(), BigDecimal.ZERO, rajeshBal,
                LocalDateTime.of(2025, 5, 12, 8, 30),
                "Dispatched 200 MT HR Coils via Road — Transporter: Bhilai Carriers");

        // Payment received for Order 2
        rajeshBal = rajeshBal.subtract(o2.getTotalPrice());
        createLedger(dist1, o2, "PAYMENT_RECEIVED", "HR Coils IS:2062", bd("200.00"),
                BigDecimal.ZERO, o2.getTotalPrice(), rajeshBal,
                LocalDateTime.of(2025, 5, 13, 16, 0),
                "UPI payment received — Ref: SAIL-TXN-20250513-BH002");

        // Material sent for Order 3 (APPROVED, not yet paid)
        rajeshBal = rajeshBal.add(o3.getTotalPrice());
        createLedger(dist1, o3, "MATERIAL_SENT", "TMT Bars Fe-500D 12mm", bd("420.00"),
                o3.getTotalPrice(), BigDecimal.ZERO, rajeshBal,
                LocalDateTime.of(2025, 6, 20, 10, 0),
                "Dispatched 420 MT TMT 12mm — Payment pending (30-day credit)");

        // --- Anita Devi Singh Ledger ---
        BigDecimal anitaBal = BigDecimal.ZERO;

        // Material sent for Order 5
        anitaBal = anitaBal.add(o5.getTotalPrice());
        createLedger(dist2, o5, "MATERIAL_SENT", "CR Sheets IS:513", bd("280.00"),
                o5.getTotalPrice(), BigDecimal.ZERO, anitaBal,
                LocalDateTime.of(2025, 4, 10, 7, 45),
                "Dispatched 280 MT CR Sheets via Rail — Bokaro Goods Terminal");

        // Payment received for Order 5
        anitaBal = anitaBal.subtract(o5.getTotalPrice());
        createLedger(dist2, o5, "PAYMENT_RECEIVED", "CR Sheets IS:513", bd("280.00"),
                BigDecimal.ZERO, o5.getTotalPrice(), anitaBal,
                LocalDateTime.of(2025, 4, 11, 15, 30),
                "NEFT payment received — Ref: SAIL-TXN-20250411-BK001");

        // Material sent for Order 6 (APPROVED, awaiting payment)
        anitaBal = anitaBal.add(o6.getTotalPrice());
        createLedger(dist2, o6, "MATERIAL_SENT", "GP Sheets IS:277", bd("180.00"),
                o6.getTotalPrice(), BigDecimal.ZERO, anitaBal,
                LocalDateTime.of(2025, 7, 16, 9, 0),
                "Dispatched 180 MT GP Sheets — Payment due by 15-Aug-2025");

        // --- Manoj Kumar Patel Ledger ---
        BigDecimal manojBal = BigDecimal.ZERO;

        // Material sent for Order 8
        manojBal = manojBal.add(o8.getTotalPrice());
        createLedger(dist3, o8, "MATERIAL_SENT", "CRGO Silicon Steel", bd("120.00"),
                o8.getTotalPrice(), BigDecimal.ZERO, manojBal,
                LocalDateTime.of(2025, 5, 8, 8, 0),
                "Dispatched 120 MT CRGO — Special handling (magnetic core grade)");

        // Payment received for Order 8
        manojBal = manojBal.subtract(o8.getTotalPrice());
        createLedger(dist3, o8, "PAYMENT_RECEIVED", "CRGO Silicon Steel", bd("120.00"),
                BigDecimal.ZERO, o8.getTotalPrice(), manojBal,
                LocalDateTime.of(2025, 5, 9, 14, 0),
                "Credit card payment received — Ref: SAIL-TXN-20250509-RK001 (Card ending 4532)");

        System.out.println("✅ Database seeded with realistic SAIL production data.");
        System.out.println("   → Admin: admin / Admin@1234");
        System.out.println("   → Distributor 1 (Rajesh): dist_1 / Rahul@1234");
        System.out.println("   → Distributor 2 (Anita):  dist_2 / Anita@5678");
        System.out.println("   → Distributor 3 (Manoj):  dist_3 / Manoj@9012");
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    private SailUnit createUnit(String name, String location, String shortCode, String desc, String capacity) {
        SailUnit u = new SailUnit();
        u.setName(name);
        u.setLocation(location);
        u.setShortCode(shortCode);
        u.setDescription(desc);
        u.setDailyCapacity(capacity);
        return u;
    }

    private User createUser(String username, String rawPassword, String role, LocalDateTime createdAt) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setCreatedAt(createdAt);
        return userRepository.save(u);
    }

    private Distributor createDistributor(User user, SailUnit unit, String name, String email,
                                          String phone, String region, LocalDateTime createdAt) {
        Distributor d = new Distributor();
        d.setUser(user);
        d.setUnit(unit);
        d.setName(name);
        d.setContactEmail(email);
        d.setContactPhone(phone);
        d.setRegion(region);
        d.setCreatedAt(createdAt);
        return distributorRepository.save(d);
    }

    private void createTarget(Distributor distributor, BigDecimal volume, String fiscalYear, String quarter) {
        SalesTarget target = new SalesTarget();
        target.setDistributor(distributor);
        target.setTargetVolume(volume);
        target.setFiscalYear(fiscalYear);
        target.setQuarter(quarter);
        target.setAssignedAt(LocalDateTime.now());
        salesTargetRepository.save(target);
    }

    private void createSalesEntry(Distributor distributor, BigDecimal volume, String category, String month,
                                  String fiscalYear, LocalDate dispatchDate, LocalDate paymentDate, String remarks) {
        SalesEntry entry = new SalesEntry();
        entry.setDistributor(distributor);
        entry.setSalesVolume(volume);
        entry.setProductCategory(category);
        entry.setMonth(month);
        entry.setFiscalYear(fiscalYear);
        entry.setDispatchDate(dispatchDate);
        entry.setPaymentDate(paymentDate);
        entry.setRemarks(remarks);
        entry.setSubmittedAt(LocalDateTime.now());
        salesEntryRepository.save(entry);
    }

    private void createInventory(Distributor distributor, SailUnit unit, String productCategory,
                                 BigDecimal quantity, BigDecimal threshold, BigDecimal pricePerMt) {
        Inventory inv = new Inventory();
        inv.setDistributor(distributor);
        inv.setUnit(unit);
        inv.setProductCategory(productCategory);
        inv.setQuantity(quantity);
        inv.setThreshold(threshold);
        inv.setPricePerMt(pricePerMt);
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inv);
    }

    private Order createOrder(Distributor distributor, SailUnit unit, String product, BigDecimal qty,
                              BigDecimal pricePerMt, String status, LocalDateTime placedAt,
                              LocalDateTime approvedAt, LocalDateTime paidAt, String rejectReason) {
        Order o = new Order();
        o.setDistributor(distributor);
        o.setUnit(unit);
        o.setProductCategory(product);
        o.setQuantity(qty);
        o.setPricePerMt(pricePerMt);
        o.setTotalPrice(qty.multiply(pricePerMt));
        o.setStatus(status);
        o.setPlacedAt(placedAt);
        o.setApprovedAt(approvedAt);
        o.setPaidAt(paidAt);
        o.setRejectReason(rejectReason);
        return orderRepository.save(o);
    }

    private void createPayment(Order order, Distributor distributor, BigDecimal amount,
                               String method, String txnRef, String cardLastFour, LocalDateTime paidAt) {
        Payment p = new Payment();
        p.setOrder(order);
        p.setDistributor(distributor);
        p.setAmount(amount);
        p.setPaymentMethod(method);
        p.setTransactionRef(txnRef);
        p.setCardLastFour(cardLastFour);
        p.setPaidAt(paidAt);
        paymentRepository.save(p);
    }

    private void createLedger(Distributor distributor, Order order, String entryType, String product,
                              BigDecimal quantityMt, BigDecimal debit, BigDecimal credit,
                              BigDecimal balance, LocalDateTime txnDate, String remarks) {
        LedgerEntry le = new LedgerEntry();
        le.setDistributor(distributor);
        le.setOrder(order);
        le.setEntryType(entryType);
        le.setProductCategory(product);
        le.setQuantityMt(quantityMt);
        le.setDebit(debit);
        le.setCredit(credit);
        le.setBalance(balance);
        le.setTransactionDate(txnDate);
        le.setRemarks(remarks);
        ledgerEntryRepository.save(le);
    }
}
