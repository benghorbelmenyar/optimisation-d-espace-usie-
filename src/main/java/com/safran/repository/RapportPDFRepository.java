package com.safran.repository;

import com.safran.entity.RapportPDF;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RapportPDFRepository extends JpaRepository<RapportPDF, Long> {
    List<RapportPDF> findBySimulationId(Long simulationId);
}