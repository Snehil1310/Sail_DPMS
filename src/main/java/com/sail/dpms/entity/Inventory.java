package com.sail.dpms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SailUnit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private Distributor distributor;

    @Column(name = "product_category", nullable = false)
    private String productCategory;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal threshold;

    @Column(name = "price_per_mt", nullable = false)
    private BigDecimal pricePerMt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Inventory() {}

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SailUnit getUnit() { return unit; }
    public void setUnit(SailUnit unit) { this.unit = unit; }

    public Distributor getDistributor() { return distributor; }
    public void setDistributor(Distributor distributor) { this.distributor = distributor; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }

    public BigDecimal getPricePerMt() { return pricePerMt; }
    public void setPricePerMt(BigDecimal pricePerMt) { this.pricePerMt = pricePerMt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
