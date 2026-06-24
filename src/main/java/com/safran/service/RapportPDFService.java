package com.safran.service;

import com.safran.dto.RapportPDFDTO;
import com.safran.entity.RapportPDF;
import com.safran.entity.Simulation;
import com.safran.repository.RapportPDFRepository;
import com.safran.repository.SimulationRepository; // 👈 Injection pour le contrôle de clé étrangère
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RapportPDFService {

    private final RapportPDFRepository rapportPDFRepository;
    private final SimulationRepository simulationRepository;

    public List<RapportPDFDTO> findAllBySimulation(Long simulationId) {
        log.debug("Recherche des rapports PDF pour la simulation ID: {}", simulationId);
        return rapportPDFRepository.findBySimulationId(simulationId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public RapportPDFDTO findById(Long id) {
        return rapportPDFRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Rapport PDF non trouvé avec id : " + id));
    }

    /**
     * Génère un rapport PDF après contrôle strict de l'existence de la simulation.
     */
    @Transactional
    public RapportPDFDTO generer(Long simulationId) {
        log.info("[VALIDATION] Demande de génération de rapport pour la simulation ID: {}", simulationId);

        // 🛡️ SÉCURITÉ STRICTE : On vérifie que la simulation parente existe bien
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Impossible de générer le rapport : la simulation avec l'ID " + simulationId + " n'existe pas."));

        String chemin = "rapports/simulation_" + simulationId + "_" + System.currentTimeMillis() + ".pdf";

        // TODO Sprint 4 : générer réellement le fichier PDF sur disque

        RapportPDF rapport = RapportPDF.builder()
                .simulation(simulation) // 🔗 Association d'objet relationnel
                .dateGeneration(LocalDateTime.now())
                .cheminFichier(chemin)
                .build();

        RapportPDF savedRapport = rapportPDFRepository.save(rapport);
        log.info("[SUCCÈS] Rapport PDF ID {} enregistré pour la simulation ID {}", savedRapport.getId(), simulationId);
        return toDTO(savedRapport);
    }

    public Resource telecharger(Long id) {
        RapportPDF rapport = rapportPDFRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rapport PDF non trouvé"));
        return new FileSystemResource(rapport.getCheminFichier());
    }

    // --- MAPPERS PRIVÉS ---

    private RapportPDFDTO toDTO(RapportPDF r) {
        return RapportPDFDTO.builder()
                .id(r.getId())
                .simulationId(r.getSimulation() != null ? r.getSimulation().getId() : null) // Extraction de l'ID
                .dateGeneration(r.getDateGeneration())
                .cheminFichier(r.getCheminFichier())
                .build();
    }
}