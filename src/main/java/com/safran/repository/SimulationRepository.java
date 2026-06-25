package com.safran.repository;

import com.safran.entity.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {

    // 🏢 Nouveau : Permet de récupérer l'historique des simulations d'une usine spécifique
    List<Simulation> findByUsineId(Long usineId);

    // 👤 Permet de voir l'historique des simulations lancées par un utilisateur précis
    List<Simulation> findByUtilisateurId(Long utilisateurId);
}