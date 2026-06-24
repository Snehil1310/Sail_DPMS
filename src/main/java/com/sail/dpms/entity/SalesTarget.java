package com.sail.dpms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_targets")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SalesTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributor_id")
    @JsonIgnoreProperties({"user"})
    private Distributor distributor;

    @Column(name = "target_volume")
    private BigDecimal targetVolume;

    @Column(name = "fiscal_year")
    private String fiscalYear;

    private String quarter;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    public SalesTarget() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Distributor getDistributor() {
        return distributor;
    }

    public void setDistributor(Distributor distributor) {
        this.distributor = distributor;
    }

    public BigDecimal getTargetVolume() {
        return targetVolume;
    }

    public void setTargetVolume(BigDecimal targetVolume) {
        this.targetVolume = targetVolume;
    }

    public String getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(String fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
