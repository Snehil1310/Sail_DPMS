package com.sail.dpms.repository;

import com.sail.dpms.entity.Distributor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    Optional<Distributor> findByUserId(Long userId);
    List<Distributor> findByUnitId(Long unitId);
}
