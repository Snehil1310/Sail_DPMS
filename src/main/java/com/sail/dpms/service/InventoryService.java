package com.sail.dpms.service;

import com.sail.dpms.entity.Inventory;
import com.sail.dpms.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> getAll() {
        return inventoryRepository.findByDistributorIsNotNull();
    }

    public List<Inventory> getByDistributor(Long distributorId) {
        return inventoryRepository.findByDistributorId(distributorId);
    }

    public List<Inventory> getPlantInventory() {
        return inventoryRepository.findByDistributorIsNull();
    }

    public List<Inventory> getByUnit(Long unitId) {
        return inventoryRepository.findByUnitId(unitId);
    }

    public Optional<Inventory> findById(Long id) {
        return inventoryRepository.findById(id);
    }

    public Inventory save(Inventory inventory) {
        inventory.setUpdatedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    /**
     * Returns inventory items where current quantity is at or below the defined threshold.
     */
    public List<Inventory> getLowStockAlerts() {
        List<Inventory> all = inventoryRepository.findByDistributorIsNotNull();
        return all.stream()
                .filter(inv -> inv.getQuantity().compareTo(inv.getThreshold()) <= 0)
                .toList();
    }

    /**
     * Returns low-stock alerts specific to a distributor.
     */
    public List<Inventory> getLowStockAlertsForDistributor(Long distributorId) {
        List<Inventory> items = inventoryRepository.findByDistributorId(distributorId);
        return items.stream()
                .filter(inv -> inv.getQuantity().compareTo(inv.getThreshold()) <= 0)
                .toList();
    }

    /**
     * Deduct inventory quantity when an order is approved and material dispatched.
     */
    public boolean deductStock(Long distributorId, String productCategory, BigDecimal qty) {
        Optional<Inventory> opt = inventoryRepository.findByDistributorIdAndProductCategory(distributorId, productCategory);
        if (opt.isEmpty()) return false;

        Inventory inv = opt.get();
        BigDecimal newQty = inv.getQuantity().subtract(qty);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) return false;

        inv.setQuantity(newQty);
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inv);
        return true;
    }

    /**
     * Add stock back (e.g., on order rejection or restocking).
     */
    public void addStock(Long distributorId, String productCategory, BigDecimal qty) {
        Optional<Inventory> opt = inventoryRepository.findByDistributorIdAndProductCategory(distributorId, productCategory);
        if (opt.isPresent()) {
            Inventory inv = opt.get();
            inv.setQuantity(inv.getQuantity().add(qty));
            inv.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inv);
        }
    }
}
