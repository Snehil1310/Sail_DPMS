package com.sail.dpms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_entries")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SalesEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributor_id")
    @JsonIgnoreProperties({"user"})
    private Distributor distributor;

    @Column(name = "sales_volume")
    private BigDecimal salesVolume;

    @Column(name = "product_category")
    private String productCategory;

    private String month;

    @Column(name = "fiscal_year")
    private String fiscalYear;

    private String remarks;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    public SalesEntry() {
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

    public BigDecimal getSalesVolume() {
        return salesVolume;
    }

    public void setSalesVolume(BigDecimal salesVolume) {
        this.salesVolume = salesVolume;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(String fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    @Column(name = "dispatch_date")
    private java.time.LocalDate dispatchDate;

    @Column(name = "payment_date")
    private java.time.LocalDate paymentDate;

    public java.time.LocalDate getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(java.time.LocalDate dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public java.time.LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(java.time.LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
}
