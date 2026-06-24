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
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           SailUnitRepository sailUnitRepository,
                           DistributorRepository distributorRepository,
                           SalesTargetRepository salesTargetRepository,
                           SalesEntryRepository salesEntryRepository,
                           InventoryRepository inventoryRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sailUnitRepository = sailUnitRepository;
        this.distributorRepository = distributorRepository;
        this.salesTargetRepository = salesTargetRepository;
        this.salesEntryRepository = salesEntryRepository;
        this.inventoryRepository = inventoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // If users already exist, do not re-seed to avoid duplicates (unless DB was dropped)
        if (userRepository.count() > 0) {
            System.out.println("ℹ️ Database already contains users. Skipping seeding.");
            return;
        }

        // 1. Create Admin User
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("Admin@1234"));
        admin.setRole("ADMIN");
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        // 2. Create SAIL Units
        SailUnit bsp = new SailUnit();
        bsp.setName("Bhilai Steel Plant");
        bsp.setLocation("Bhilai, Chhattisgarh");
        bsp.setShortCode("BSP");
        bsp.setDescription("India's first and most productive integrated steel plant, known for producing world-class rails, heavy structurals, and plates.");
        bsp.setDailyCapacity("9,500 MT/Day");

        SailUnit bsl = new SailUnit();
        bsl.setName("Bokaro Steel Plant");
        bsl.setLocation("Bokaro, Jharkhand");
        bsl.setShortCode("BSL");
        bsl.setDescription("One of the largest steel plants in India, specializing in hot & cold rolled coils, sheets, and galvanised products.");
        bsl.setDailyCapacity("12,600 MT/Day");

        SailUnit rsp = new SailUnit();
        rsp.setName("Rourkela Steel Plant");
        rsp.setLocation("Rourkela, Odisha");
        rsp.setShortCode("RSP");
        rsp.setDescription("The first public sector steel plant featuring a dedicated Silicon Steel Mill and a state-of-the-art Plate Mill.");
        rsp.setDailyCapacity("6,000 MT/Day");

        SailUnit dsp = new SailUnit();
        dsp.setName("Durgapur Steel Plant");
        dsp.setLocation("Durgapur, West Bengal");
        dsp.setShortCode("DSP");
        dsp.setDescription("A key producer of structural steel, skelp, and specialised wheels & axles for Indian Railways.");
        dsp.setDailyCapacity("5,800 MT/Day");

        SailUnit isp = new SailUnit();
        isp.setName("IISCO Steel Plant");
        isp.setLocation("Burnpur, West Bengal");
        isp.setShortCode("ISP");
        isp.setDescription("One of the largest steel plants in India, recently modernised with cutting-edge technology and expanded capacity.");
        isp.setDailyCapacity("7,100 MT/Day");

        sailUnitRepository.saveAll(List.of(bsp, bsl, rsp, dsp, isp));

        // 3. Create Distributor 1: Bhilai (Rajesh Kumar Sharma)
        User userBhilai = new User();
        userBhilai.setUsername("dist_bhilai");
        userBhilai.setPassword(passwordEncoder.encode("dist123"));
        userBhilai.setRole("DISTRIBUTOR");
        userBhilai.setCreatedAt(LocalDateTime.now());
        userRepository.save(userBhilai);

        Distributor distBhilai = new Distributor();
        distBhilai.setUser(userBhilai);
        distBhilai.setUnit(bsp);
        distBhilai.setName("Rajesh Kumar Sharma");
        distBhilai.setContactEmail("rajesh@steeldist.in");
        distBhilai.setContactPhone("+91-9876543210");
        distBhilai.setRegion("Central India");
        distBhilai.setCreatedAt(LocalDateTime.now());
        distributorRepository.save(distBhilai);

        // 4. Create Distributor 2: Bokaro (Anita Devi Singh)
        User userBokaro = new User();
        userBokaro.setUsername("dist_bokaro");
        userBokaro.setPassword(passwordEncoder.encode("dist123"));
        userBokaro.setRole("DISTRIBUTOR");
        userBokaro.setCreatedAt(LocalDateTime.now());
        userRepository.save(userBokaro);

        Distributor distBokaro = new Distributor();
        distBokaro.setUser(userBokaro);
        distBokaro.setUnit(bsl);
        distBokaro.setName("Anita Devi Singh");
        distBokaro.setContactEmail("anita@irondist.co.in");
        distBokaro.setContactPhone("+91-9876543211");
        distBokaro.setRegion("Eastern India");
        distBokaro.setCreatedAt(LocalDateTime.now());
        distributorRepository.save(distBokaro);

        // 5. Create Distributor 3: Rourkela (Manoj Patel)
        User userRourkela = new User();
        userRourkela.setUsername("dist_rourkela");
        userRourkela.setPassword(passwordEncoder.encode("dist123"));
        userRourkela.setRole("DISTRIBUTOR");
        userRourkela.setCreatedAt(LocalDateTime.now());
        userRepository.save(userRourkela);

        Distributor distRourkela = new Distributor();
        distRourkela.setUser(userRourkela);
        distRourkela.setUnit(rsp);
        distRourkela.setName("Manoj Patel");
        distRourkela.setContactEmail("manoj@steelworld.in");
        distRourkela.setContactPhone("+91-9876543212");
        distRourkela.setRegion("Odisha Region");
        distRourkela.setCreatedAt(LocalDateTime.now());
        distributorRepository.save(distRourkela);

        // 6. Seed Central Plant Inventories (Admin stocks)
        // Bhilai (BSP)
        createInventory(null, bsp, "TMT Bars 8mm",  new BigDecimal("5000.00"), new BigDecimal("1000.00"), new BigDecimal("42000.00"));
        createInventory(null, bsp, "TMT Bars 10mm", new BigDecimal("4500.00"), new BigDecimal("1000.00"), new BigDecimal("43000.00"));
        createInventory(null, bsp, "TMT Bars 12mm", new BigDecimal("3200.00"), new BigDecimal("1000.00"), new BigDecimal("44000.00"));
        createInventory(null, bsp, "HR Coils",      new BigDecimal("8000.00"), new BigDecimal("2000.00"), new BigDecimal("48000.00"));
        createInventory(null, bsp, "Plates",        new BigDecimal("2100.00"), new BigDecimal("500.00"),  new BigDecimal("52000.00"));
        createInventory(null, bsp, "Rails",         new BigDecimal("15000.00"), new BigDecimal("5000.00"), new BigDecimal("55000.00"));

        // Bokaro (BSL)
        createInventory(null, bsl, "CR Sheets",     new BigDecimal("4000.00"), new BigDecimal("800.00"),  new BigDecimal("50000.00"));
        createInventory(null, bsl, "GP Sheets",     new BigDecimal("3500.00"), new BigDecimal("800.00"),  new BigDecimal("52000.00"));
        createInventory(null, bsl, "HR Coils",      new BigDecimal("9000.00"), new BigDecimal("2000.00"), new BigDecimal("47000.00"));
        createInventory(null, bsl, "TMT Bars 16mm", new BigDecimal("6000.00"), new BigDecimal("1500.00"), new BigDecimal("45000.00"));

        // Rourkela (RSP)
        createInventory(null, rsp, "Silicon Steel",    new BigDecimal("1200.00"), new BigDecimal("300.00"),  new BigDecimal("65000.00"));
        createInventory(null, rsp, "Plates",           new BigDecimal("4500.00"), new BigDecimal("1000.00"), new BigDecimal("53000.00"));
        createInventory(null, rsp, "Structural Steel", new BigDecimal("5600.00"), new BigDecimal("1200.00"), new BigDecimal("46000.00"));

        // Durgapur (DSP)
        createInventory(null, dsp, "Wheels",           new BigDecimal("8000.00"), new BigDecimal("1500.00"), new BigDecimal("60000.00"));
        createInventory(null, dsp, "Axles",            new BigDecimal("6500.00"), new BigDecimal("1200.00"), new BigDecimal("58000.00"));

        // IISCO (ISP)
        createInventory(null, isp, "Wire Rods",        new BigDecimal("5000.00"), new BigDecimal("1200.00"), new BigDecimal("42000.00"));

        // 7. Seed Distributor-level Inventories (including threshold warnings)
        // Bhilai Distributor Inventory:
        // - "TMT Bars 8mm": quantity = 30.00, threshold = 100.00 -> UNDER THRESHOLD (Triggers Alert)
        // - "HR Coils": quantity = 250.00, threshold = 150.00 -> Normal
        createInventory(distBhilai, bsp, "TMT Bars 8mm",  new BigDecimal("30.00"),  new BigDecimal("100.00"), new BigDecimal("45000.00"));
        createInventory(distBhilai, bsp, "HR Coils",      new BigDecimal("250.00"), new BigDecimal("150.00"), new BigDecimal("51000.00"));

        // Bokaro Distributor Inventory:
        // - "CR Sheets": quantity = 45.00, threshold = 80.00 -> UNDER THRESHOLD (Triggers Alert)
        createInventory(distBokaro, bsl, "CR Sheets",     new BigDecimal("45.00"),  new BigDecimal("80.00"),  new BigDecimal("53000.00"));

        // Rourkela Distributor Inventory:
        // - "Silicon Steel": quantity = 100.00, threshold = 50.00 -> Normal
        createInventory(distRourkela, rsp, "Silicon Steel", new BigDecimal("100.00"), new BigDecimal("50.00"),  new BigDecimal("68000.00"));

        // 8. Seed Sales Targets & Performance Entries (History)
        // Rajesh targets
        createTarget(distBhilai, new BigDecimal("2500.00"), "2025-26", "Q1");
        createTarget(distBhilai, new BigDecimal("3000.00"), "2025-26", "Q2");

        // Rajesh sales entries
        createSalesEntry(distBhilai, new BigDecimal("1200.00"), "TMT Bars 8mm", "April", "2025-26",
                LocalDate.of(2025, 4, 15), LocalDate.of(2025, 4, 18), "Initial batch dispatched");
        createSalesEntry(distBhilai, new BigDecimal("800.00"), "HR Coils", "May", "2025-26",
                LocalDate.of(2025, 5, 12), LocalDate.of(2025, 5, 15), "Urgent industrial order");

        // Anita targets
        createTarget(distBokaro, new BigDecimal("2000.00"), "2025-26", "Q1");

        // Anita sales entries
        createSalesEntry(distBokaro, new BigDecimal("900.00"), "CR Sheets", "April", "2025-26",
                LocalDate.of(2025, 4, 20), LocalDate.of(2025, 4, 22), "Regular sheet supply");

        System.out.println("✅ Database seeded successfully with users, plants, and threshold warnings.");
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

    private void createSalesEntry(Distributor distributor, BigDecimal volume, String category, String month, String fiscalYear,
                                  LocalDate dispatchDate, LocalDate paymentDate, String remarks) {
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
}
