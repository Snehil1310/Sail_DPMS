package com.sail.dpms.repository;

import com.sail.dpms.entity.SalesTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesTargetRepository extends JpaRepository<SalesTarget, Long> {
    List<SalesTarget> findByDistributorId(Long distributorId);
    List<SalesTarget> findByDistributorIdAndFiscalYear(Long distributorId, String fiscalYear);
}
