package com.safran.repository;

import com.safran.entity.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SimulationRepository extends JpaRepository<Simulation, Long> {
    List<Simulation> findByCommandeId(Long commandeId);
    List<Simulation> findByUtilisateurId(Long utilisateurId);
}