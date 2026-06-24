package com.safran.repository;

import com.safran.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByUsineId(Long usineId);
    List<Machine> findByZoneId(Long zoneId);
}