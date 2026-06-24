package com.safran.service;

import com.safran.dto.SimulationDTO;
import com.safran.entity.Commande;
import com.safran.entity.Poste;
import com.safran.entity.Simulation;
import com.safran.entity.Utilisateur;
import com.safran.repository.CommandeRepository;
import com.safran.repository.PosteRepository;
import com.safran.repository.SimulationRepository;
import com.safran.repository.UtilisateurRepository; // 👈 Pour valider l'utilisateur
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final CommandeRepository commandeRepository;
    private final PosteRepository posteRepository;
    private final UtilisateurRepository utilisateurRepository; // 👈 Injection
    private final CommandeService commandeService;

    public List<SimulationDTO> findAllByCommande(Long commandeId) {
        return simulationRepository.findByCommandeId(commandeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public SimulationDTO findById(Long id) {
        return simulationRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée avec id : " + id));
    }

    /**
     * Lance une simulation complète après contrôle d'intégrité des clés étrangères.
     */
    @Transactional
    public SimulationDTO lancer(Long commandeId, Long utilisateurId) {
        log.info("[VALIDATION] Lancement simulation. Commande ID: {}, Utilisateur ID: {}", commandeId, utilisateurId);

        // 🛡️ SÉCURITÉ STRICTE : Vérification de la commande
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Impossible de simuler : la commande avec l'ID " + commandeId + " n'existe pas."));

        // 🛡️ SÉCURITÉ STRICTE : Vérification de l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Impossible de simuler : l'utilisateur avec l'ID " + utilisateurId + " n'existe pas."));

        boolean faisable = commandeService.verifierFaisabilite(commandeId);
        double capaciteRequise = commandeService.calculerCapaciteRequise(commandeId);

        Long usineId = (commande.getUsine() != null) ? commande.getUsine().getId() : null;
        if (usineId == null) {
            log.error("Impossible de simuler : la commande ID {} n'est rattachée à aucune usine valide.", commandeId);
            throw new IllegalStateException("La commande spécifiée n'est liée à aucune usine.");
        }

        List<Poste> postes = posteRepository.findByUsineId(usineId);
        Optional<Poste> posteGoulot = postes.stream()
                .min(Comparator.comparingDouble(p -> (3600.0 / p.getCycleTime()) * p.getNombreOperateurs()));

        Simulation simulation = Simulation.builder()
                .commande(commande)      // 🔗 Association d'objet
                .utilisateur(utilisateur) // 🔗 Association d'objet
                .dateSimulation(LocalDateTime.now())
                .capaciteCalculee((float) capaciteRequise)
                .faisabilite(faisable)
                .posteGoulot(posteGoulot.map(Poste::getNom).orElse(null))
                .solutionProposee(faisable ? null : proposerSolution(posteGoulot.orElse(null)))
                .build();

        Simulation savedSimulation = simulationRepository.save(simulation);
        log.info("[SUCCÈS] Simulation ID {} enregistrée.", savedSimulation.getId());
        return toDTO(savedSimulation);
    }

    public String proposerSolution(Poste posteGoulot) {
        if (posteGoulot == null) return "Aucune solution disponible : aucun poste identifié.";
        return "Solutions proposées : ajouter un opérateur au poste '" + posteGoulot.getNom()
                + "', recourir aux heures supplémentaires, ou étaler la commande sur une période plus longue.";
    }

    public void delete(Long id) {
        if (!simulationRepository.existsById(id)) {
            throw new RuntimeException("Simulation introuvable");
        }
        simulationRepository.deleteById(id);
    }

    private SimulationDTO toDTO(Simulation s) {
        return SimulationDTO.builder()
                .id(s.getId())
                // Extraction sécurisée des clés primaires pour le DTO
                .commandeId(s.getCommande() != null ? s.getCommande().getId() : null)
                .utilisateurId(s.getUtilisateur() != null ? s.getUtilisateur().getId() : null)
                .dateSimulation(s.getDateSimulation())
                .capaciteCalculee(s.getCapaciteCalculee())
                .faisabilite(s.isFaisabilite())
                .posteGoulot(s.getPosteGoulot())
                .solutionProposee(s.getSolutionProposee())
                .build();
    }
}