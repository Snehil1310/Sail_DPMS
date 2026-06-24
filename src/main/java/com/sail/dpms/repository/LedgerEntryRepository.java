package com.sail.dpms.repository;

import com.sail.dpms.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByDistributorIdOrderByTransactionDateDesc(Long distributorId);

    @Query("SELECT COALESCE(SUM(e.credit) - SUM(e.debit), 0) FROM LedgerEntry e WHERE e.distributor.id = :distributorId")
    BigDecimal calculateBalanceByDistributorId(Long distributorId);
}
