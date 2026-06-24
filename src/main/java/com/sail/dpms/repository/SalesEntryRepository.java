package com.sail.dpms.repository;

import com.sail.dpms.entity.SalesEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesEntryRepository extends JpaRepository<SalesEntry, Long> {
    List<SalesEntry> findByDistributorId(Long distributorId);
    List<SalesEntry> findByDistributorIdAndFiscalYear(Long distributorId, String fiscalYear);
}
