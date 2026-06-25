package com.safran.service;

import com.safran.dto.SimulationDTO;
import com.safran.entity.*;
import com.safran.enums.UniteTemps;
import com.safran.enums.StatutCommande;
import com.safran.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UsineRepository usineRepository;
    private final ProcessusRepository processusRepository; // <-- À AJOUTER

    @Transactional(readOnly = true)
    public List<SimulationDTO> findAllByUsine(Long usineId) {
        return simulationRepository.findByUsineId(usineId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SimulationDTO findById(Long id) {
        return simulationRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée avec id : " + id));
    }

    /**
     * Lance la simulation globale dynamique basée sur le portefeuille de commandes de l'usine.
     */
    @Transactional
    public List<SimulationDTO> lancerSimulationDynamiqueParProcessus(Long usineId, Long utilisateurId, int quantite, UniteTemps unite, Long processusId) {
        log.info("[SIMULATION] Analyse Processus: {} pour l'Usine: {}", processusId, usineId);

        Usine usine = usineRepository.findById(usineId)
                .orElseThrow(() -> new IllegalArgumentException("L'usine spécifiée n'existe pas."));
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("L'utilisateur spécifié n'existe pas."));
        Processus processus = processusRepository.findById(processusId)
                .orElseThrow(() -> new IllegalArgumentException("Le processus spécifié n'existe pas."));

        LocalDate debut = LocalDate.now();
        LocalDate fin;
        float facteurTemps;

        switch (unite) {
            case JOUR:
                fin = debut.plusDays(quantite);
                facteurTemps = (float) quantite / 365f;
                break;
            case MOIS:
                fin = debut.plusMonths(quantite);
                facteurTemps = (float) quantite / 12f;
                break;
            case ANNEE:
            default:
                fin = debut.plusYears(quantite);
                facteurTemps = (float) quantite;
                break;
        }

        // 1. Récupérer les commandes de l'usine sur la bonne période
        List<Commande> commandesPortefeuille = commandeRepository.findByUsineId(usineId).stream()
                .filter(c -> c.getStatut() == StatutCommande.EN_ATTENTE || c.getStatut() == StatutCommande.VALIDEE)
                .filter(c -> c.getDateLivraisonSouhaitee() != null &&
                        !c.getDateLivraisonSouhaitee().isBefore(debut) &&
                        !c.getDateLivraisonSouhaitee().isAfter(fin))
                .collect(Collectors.toList());

        // 2. Filtrer les lignes de besoins qui concernent uniquement le processus demandé
        List<BesoinCommande> besoinsDuProcessus = commandesPortefeuille.stream()
                .filter(c -> c.getBesoins() != null)
                .flatMap(c -> c.getBesoins().stream())
                .filter(b -> b.getProcessus() != null && b.getProcessus().getId().equals(processusId))
                .collect(Collectors.toList());

        if (besoinsDuProcessus.isEmpty()) {
            throw new IllegalStateException("Aucun besoin trouvé pour le processus [" + processus.getNom() + "] dans cette période.");
        }

        // 3. Récupérer la liste des programmes avions impactés (A320, A350...)
        // Note : On remonte à la commande depuis le besoin. Si ton entité BesoinCommande possède un champ 'commande', assure-toi qu'elle a le @Getter de Lombok.
        // 3. Récupérer la liste des programmes avions impactés (A320, A350...)
        String programmesConcernes = commandesPortefeuille.stream()
                .filter(c -> c.getBesoins().stream().anyMatch(b -> b.getProcessus().getId().equals(processusId)))
                .map(Commande::getProgrammeAvion)
                .filter(p -> p != null) // Sécurité anti-NullPointerException
                .map(Programme::getNom) // <-- Extraction du nom du programme (String)
                .distinct()
                .collect(Collectors.joining(", "));

        // 4. Cumul des charges horaires demandées
        float totalHeuresDemandees = (float) besoinsDuProcessus.stream()
                .mapToDouble(BesoinCommande::getHeuresDemandees)
                .sum();

        // 5. Calcul capacitaire (Postes / Opérateurs)
        int postesActuels = processus.getNombreOperateurs();
        float hDispoParPoste = 1840f * facteurTemps;
        float hDispoTotale = postesActuels * hDispoParPoste;

        // Combien de postes théoriques requis pour absorber la charge
        int postesRequis = (int) Math.ceil(totalHeuresDemandees / hDispoParPoste);

        int postesAAjouter = 0;
        int postesARetirer = 0;
        boolean faisable = true;
        String solution;

        if (totalHeuresDemandees > hDispoTotale) {
            faisable = false;
            postesAAjouter = postesRequis - postesActuels;
            solution = String.format("SATURATION [%s] : Programmes: %s. Charge cumulée de %.1f h. Capacité actuelle: %.1f h. Il faut AJOUTER %d poste(s).",
                    processus.getNom(), programmesConcernes, totalHeuresDemandees, hDispoTotale, postesAAjouter);
        } else {
            postesARetirer = postesActuels - postesRequis;
            if (postesARetirer > 0) {
                solution = String.format("OPTIMISATION [%s] : Programmes: %s. Charge cumulée de %.1f h. Surplus constaté. Possibilité de RETIRER %d poste(s).",
                        processus.getNom(), programmesConcernes, totalHeuresDemandees, postesARetirer);
            } else {
                solution = String.format("ÉQUILIBRE [%s] : Programmes: %s. Charge cumulée de %.1f h. Vos %d postes actuels sont idéalement dimensionnés.",
                        processus.getNom(), programmesConcernes, totalHeuresDemandees, postesActuels);
            }
        }

        float tauxCharge = hDispoTotale > 0 ? (totalHeuresDemandees / hDispoTotale) * 100 : 100f;

        Simulation simulation = Simulation.builder()
                .usine(usine)
                .processus(processus)
                .utilisateur(utilisateur)
                .dateSimulation(LocalDateTime.now())
                .heuresDemandees(totalHeuresDemandees)
                .heuresDisponiblesActuelles(hDispoTotale)
                .operateursActuels(postesActuels)
                .operateursAAjouter(postesAAjouter)
                .operateursARetirer(postesARetirer)
                .tauxChargeProcessus(tauxCharge)
                .faisabilite(faisable)
                .solutionProposee(solution)
                .build();

        Simulation saved = simulationRepository.save(simulation);

        // FIX JAVA 8 : Remplacer List.of par java.util.Arrays.asList
        return java.util.Arrays.asList(toDTO(saved));
    }

    @Transactional
    public void delete(Long id) {
        if (!simulationRepository.existsById(id)) {
            throw new RuntimeException("Simulation introuvable");
        }
        simulationRepository.deleteById(id);
    }

    // --- MAPPER PRIVÉ ---
    private SimulationDTO toDTO(Simulation s) {
        if (s == null) return null;

        return SimulationDTO.builder()
                .id(s.getId())
                .usineId(s.getUsine() != null ? s.getUsine().getId() : null)
                .processusId(s.getProcessus() != null ? s.getProcessus().getId() : null)
                .processusNom(s.getProcessus() != null ? s.getProcessus().getNom() : null)
                .utilisateurId(s.getUtilisateur() != null ? s.getUtilisateur().getId() : null)
                .dateSimulation(s.getDateSimulation())
                .heuresDemandees(s.getHeuresDemandees())
                .heuresDisponiblesActuelles(s.getHeuresDisponiblesActuelles())
                .operateursActuels(s.getOperateursActuels())
                .operateursAAjouter(s.getOperateursAAjouter())
                .operateursARetirer(s.getOperateursARetirer())
                .tauxChargeProcessus(s.getTauxChargeProcessus())
                .faisabilite(s.isFaisabilite())
                .solutionProposee(s.getSolutionProposee())
                .build();
    }
}