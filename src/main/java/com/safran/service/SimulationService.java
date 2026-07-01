package com.safran.service;

import com.safran.algorithm.OptimizationResult;
import com.safran.algorithm.SpaceOptimizationStrategy;
import com.safran.dto.SimulationDTO;
import com.safran.entity.*;
import com.safran.entity.UniteTemps;
import com.safran.entity.StatutCommande;
import com.safran.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UsineRepository usineRepository;
    private final ProcessusRepository processusRepository;
    private final PosteRepository posteRepository; // 💡 AJOUTÉ pour lier la logique de shifts

    @Qualifier("greedyOptimizer")
    private final SpaceOptimizationStrategy spaceOptimizer;

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

    @Transactional
    public List<SimulationDTO> lancerSimulationDynamiqueParProcessus(Long usineId, Long utilisateurId, int quantite, UniteTemps unite, Long processusId) {
        log.info("[SIMULATION] Analyse RH, Shifts & Espace pour Processus: {}", processusId);

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

        List<Commande> commandesPortefeuille = commandeRepository.findByUsineId(usineId).stream()
                .filter(c -> c.getStatut() == StatutCommande.EN_ATTENTE || c.getStatut() == StatutCommande.VALIDEE)
                .filter(c -> c.getDateLivraisonSouhaitee() != null &&
                        !c.getDateLivraisonSouhaitee().isBefore(debut) &&
                        !c.getDateLivraisonSouhaitee().isAfter(fin))
                .collect(Collectors.toList());

        List<BesoinCommande> besoinsDuProcessus = commandesPortefeuille.stream()
                .filter(c -> c.getBesoins() != null)
                .flatMap(c -> c.getBesoins().stream())
                .filter(b -> b.getProcessus() != null && b.getProcessus().getId().equals(processusId))
                .collect(Collectors.toList());

        if (besoinsDuProcessus.isEmpty()) {
            throw new IllegalStateException("Aucun besoin trouvé pour le processus [" + processus.getNom() + "] dans cette période.");
        }

        String programmesConcernes = commandesPortefeuille.stream()
                .filter(c -> c.getBesoins().stream().anyMatch(b -> b.getProcessus().getId().equals(processusId)))
                .map(Commande::getProgrammeAvion)
                .filter(p -> p != null)
                .map(Programme::getNom)
                .distinct()
                .collect(Collectors.joining(", "));

        float totalHeuresDemandees = (float) besoinsDuProcessus.stream()
                .mapToDouble(BesoinCommande::getHeuresDemandees)
                .sum();

        // 💡 MAJ LOGIQUE INDUSTRIELLE SHIFTS
        // On cherche le poste physique principal attaché à ce programme
        List<Poste> postesDuProcessus = posteRepository.findByUsineId(usineId).stream()
                .filter(p -> p.getProgramme() != null && processus.getProgrammes().contains(p.getProgramme()))
                .collect(Collectors.toList());

        int maxShiftsActuels = 1;
        int nombreOperateursParPoste = processus.getNombreOperateurs(); // Valeur secours si pas de poste
        int quantitePostesPhysiques = 1;

        if (!postesDuProcessus.isEmpty()) {
            Poste postePrincipal = postesDuProcessus.get(0);
            maxShiftsActuels = postePrincipal.getNombreShifts() <= 0 ? 1 : postePrincipal.getNombreShifts();
            nombreOperateursParPoste = postePrincipal.getNombreOperateurs();
            quantitePostesPhysiques = postePrincipal.getQuantite() <= 0 ? 1 : postePrincipal.getQuantite();
        }

        int totalOperateursActuelsTotaux = nombreOperateursParPoste * maxShiftsActuels * quantitePostesPhysiques;

        // Formule de capacité ajustée au nombre de shifts de production
        float hDispoParOperateur = 1840f * facteurTemps;
        float hDispoTotaleMoyensActuels = totalOperateursActuelsTotaux * hDispoParOperateur;

        int totalOperateursRequisTotaux = (int) Math.ceil(totalHeuresDemandees / hDispoParOperateur);

        int operateursAAjouter = 0;
        int operateursARetirer = 0;
        boolean faisable = true;
        String solution;

        if (totalHeuresDemandees > hDispoTotaleMoyensActuels) {
            operateursAAjouter = totalOperateursRequisTotaux - totalOperateursActuelsTotaux;
            faisable = false;

            // Calcul du nombre de shifts nécessaires pour absorber le flux sans construire de poste
            int shiftsRequis = (int) Math.ceil((double) totalOperateursRequisTotaux / (nombreOperateursParPoste * quantitePostesPhysiques));

            if (shiftsRequis <= 2 && maxShiftsActuels < 2) {
                // 💡 CAS IDÉAL : Le passage en Double Shift résout le problème sans impact au sol
                solution = String.format("REORGANISATION CAPACITAIRE [%s] : Programmes: %s. Charge de %.1f h. Le poste actuel est saturé en Single Shift. " +
                                "SOLUTION : Passer en DOUBLE SHIFT (Shifts requis: 2). Recrutement requis : +%d opérateur(s) (Staff total requis: %d). Aucun nouvel emplacement physique au sol requis.",
                        processus.getNom(), programmesConcernes, totalHeuresDemandees, operateursAAjouter, totalOperateursRequisTotaux);
            } else {
                // Si même le double/triple shift ne suffit pas, on doit ajouter de la surface au sol (Nouveau poste)
                Zone zonePhysique = processus.getZone();
                int postesPhysiquesAAjouter = (int) Math.ceil((double) operateursAAjouter / (nombreOperateursParPoste * (maxShiftsActuels == 1 ? 2 : maxShiftsActuels)));

                if (zonePhysique != null) {
                    OptimizationResult result = spaceOptimizer.optimiser(zonePhysique, processus);
                    float surfaceParPoste = zonePhysique.getSurfaceRequiseParPoste();
                    if (surfaceParPoste <= 0) {
                        throw new IllegalStateException("La métrique 'surface_requise_par_poste' n'est pas configurée pour la zone '" + zonePhysique.getNom() + "'.");
                    }

                    float surfaceTotaleRequisePourAjout = postesPhysiquesAAjouter * surfaceParPoste;
                    float surfaceTotaleZone = zonePhysique.getLongueur() * zonePhysique.getLargeur();

                    float surfaceMachines = 0f;
                    if (zonePhysique.getMachines() != null) {
                        for (Machine machine : zonePhysique.getMachines()) {
                            surfaceMachines += (machine.getLongueur() * machine.getLargeur());
                        }
                    }
                    float surfaceDisponibleDansZone = surfaceTotaleZone - surfaceMachines;

                    if (surfaceTotaleRequisePourAjout > surfaceDisponibleDansZone) {
                        solution = String.format("SATURATION PHYSIQUE SÉVÈRE [%s] : Programmes: %s. Les shifts actuels (%dx8h) sont au maximum. L'ajout de %d poste(s) physique(s) requiert %.1f m², mais la zone '%s' est pleine. Déploiement bloqué. %s",
                                processus.getNom(), programmesConcernes, maxShiftsActuels, postesPhysiquesAAjouter, surfaceTotaleRequisePourAjout, zonePhysique.getNom(), result.getMessage());
                    } else {
                        solution = String.format("SATURATION AVEC EXTENSION SOL [%s] : Programmes: %s. Shifts saturés. Il faut ajouter %d poste(s) physique(s) (Empreinte: %.1f m²). Place disponible dans la zone '%s'. Embauche de +%d opérateurs requise.",
                                processus.getNom(), programmesConcernes, postesPhysiquesAAjouter, surfaceTotaleRequisePourAjout, zonePhysique.getNom(), operateursAAjouter);
                    }
                } else {
                    solution = String.format("SATURATION CAPACITAIRE [%s] : Programmes: %s. Équipes saturées. Recrutement de +%d opérateur(s) requis. Aucune zone physique associée pour valider l'espace.",
                            processus.getNom(), programmesConcernes, operateursAAjouter);
                }
            }

        } else {
            faisable = true;
            operateursARetirer = totalOperateursActuelsTotaux - totalOperateursRequisTotaux;
            if (operateursARetirer > 0) {
                solution = String.format("OPTIMISATION FLUX [%s] : Programmes: %s. Charge cumulée de %.1f h. Vos équipes en %dx8h disposent d'un surplus. Option de réduction de charge ou redéploiement de %d opérateur(s).",
                        processus.getNom(), programmesConcernes, totalHeuresDemandees, operateursARetirer);
            } else {
                solution = String.format("ÉQUILIBRE INDUSTRIEL [%s] : Programmes: %s. Charge cumulée de %.1f h. Vos équipes actuelles (%dx8h) sont parfaitement dimensionnées.",
                        processus.getNom(), programmesConcernes, totalHeuresDemandees, maxShiftsActuels);
            }
        }

        float tauxCharge = hDispoTotaleMoyensActuels > 0 ? (totalHeuresDemandees / hDispoTotaleMoyensActuels) * 100 : 100f;

        Simulation simulation = Simulation.builder()
                .usine(usine)
                .processus(processus)
                .utilisateur(utilisateur)
                .dateSimulation(LocalDateTime.now())
                .heuresDemandees(totalHeuresDemandees)
                .heuresDisponiblesActuelles(hDispoTotaleMoyensActuels)
                .operateursActuels(totalOperateursActuelsTotaux)
                .operateursAAjouter(operateursAAjouter)
                .operateursARetirer(operateursARetirer)
                .tauxChargeProcessus(tauxCharge)
                .faisabilite(faisable)
                .solutionProposee(solution)
                .build();

        Simulation saved = simulationRepository.save(simulation);
        return Collections.singletonList(toDTO(saved));
    }

    @Transactional
    public void delete(Long id) {
        if (!simulationRepository.existsById(id)) {
            throw new RuntimeException("Simulation introuvable");
        }
        simulationRepository.deleteById(id);
    }

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