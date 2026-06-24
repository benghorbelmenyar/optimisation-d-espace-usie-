package com.safran.service;

import com.safran.dto.CommandeDTO;
import com.safran.entity.Commande;
import com.safran.entity.Poste;
import com.safran.entity.Usine;
import com.safran.enums.StatutCommande;
import com.safran.repository.CommandeRepository;
import com.safran.repository.PosteRepository;
import com.safran.repository.UsineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandeService {

    private static final double HEURES_PAR_EMPLOYE_AN = 1850.0;

    private final CommandeRepository commandeRepository;
    private final PosteRepository posteRepository;
    private final UsineRepository usineRepository;

    public List<CommandeDTO> findAll() {
        log.debug("Appel de findAll() dans CommandeService");
        return commandeRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CommandeDTO> findAllByUsine(Long usineId) {
        log.debug("Appel de findAllByUsine() pour l'usine ID: {}", usineId);
        return commandeRepository.findByUsineId(usineId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CommandeDTO findById(Long id) {
        return commandeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.error("Erreur : La commande avec l'ID {} n'existe pas en base", id);
                    return new RuntimeException("Commande non trouvée avec id : " + id);
                });
    }

    @Transactional
    public CommandeDTO create(CommandeDTO dto) {
        log.info("[VALIDATION] Vérification de l'existence de l'usine ID: {}", dto.getUsineId());

        // 🛡️ BLOCAGE STRICT : Vérification immédiate avant conversion ou persistance
        if (dto.getUsineId() == null || !usineRepository.existsById(dto.getUsineId())) {
            log.error("[BLOQUÉ] Impossible de créer la commande. L'usine ID {} n'existe pas.", dto.getUsineId());
            throw new IllegalArgumentException("Impossible de créer la commande : l'usine spécifiée avec l'ID " + dto.getUsineId() + " n'existe pas.");
        }

        Commande commande = toEntity(dto);
        commande.setDateCommande(LocalDate.now());
        commande.setStatut(StatutCommande.EN_ATTENTE);

        Commande savedCommande = commandeRepository.save(commande);
        log.info("[SUCCÈS] Commande ID {} créée pour l'usine ID {}", savedCommande.getId(), commande.getUsine().getId());
        return toDTO(savedCommande);
    }

    @Transactional
    public CommandeDTO update(Long id, CommandeDTO dto) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Échec modification : Commande ID {} introuvable", id);
                    return new RuntimeException("Commande non trouvée avec id : " + id);
                });

        log.info("Mise à jour de la commande ID {}.", id);

        // 🛡️ BLOCAGE STRICT SUR LE PUT : Vérification de la nouvelle usine s'il y a un changement
        Long currentUsineId = (commande.getUsine() != null) ? commande.getUsine().getId() : null;
        if (dto.getUsineId() != null && !dto.getUsineId().equals(currentUsineId)) {
            if (!usineRepository.existsById(dto.getUsineId())) {
                log.error("[BLOQUÉ] Impossible de modifier la commande. La nouvelle usine ID {} n'existe pas.", dto.getUsineId());
                throw new IllegalArgumentException("Impossible de modifier la commande : la nouvelle usine spécifiée avec l'ID " + dto.getUsineId() + " n'existe pas.");
            }
            Usine nouvelleUsine = usineRepository.findById(dto.getUsineId()).get();
            commande.setUsine(nouvelleUsine);
        }

        commande.setClient(dto.getClient());
        commande.setQuantiteDemandee(dto.getQuantiteDemandee());
        commande.setDateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee());

        if (dto.getStatut() != null) {
            commande.setStatut(dto.getStatut());
        }

        Commande updatedCommande = commandeRepository.save(commande);
        log.info("Commande ID {} modifiée avec succès.", id);
        return toDTO(updatedCommande);
    }

    public void delete(Long id) {
        if (!commandeRepository.existsById(id)) {
            log.error("Impossible de supprimer : la commande ID {} n'existe pas", id);
            throw new RuntimeException("Commande introuvable");
        }
        commandeRepository.deleteById(id);
        log.info("Commande ID {} supprimée définitivement.", id);
    }

    public double calculerCapaciteRequise(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        Long usineId = commande.getUsine() != null ? commande.getUsine().getId() : null;
        List<Poste> postes = posteRepository.findByUsineId(usineId);
        if (postes.isEmpty()) return 0;

        double cycleTimeMax = postes.stream()
                .mapToDouble(Poste::getCycleTime)
                .max().orElse(0);

        return (cycleTimeMax * commande.getQuantiteDemandee()) / 3600.0;
    }

    @Transactional
    public boolean verifierFaisabilite(Long commandeId) {
        double capaciteRequise = calculerCapaciteRequise(commandeId);
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        Long usineId = commande.getUsine() != null ? commande.getUsine().getId() : null;
        List<Poste> postes = posteRepository.findByUsineId(usineId);

        double capaciteDisponible = postes.stream()
                .mapToDouble(p -> (3600.0 / p.getCycleTime()) * p.getNombreOperateurs())
                .min().orElse(0);

        boolean faisable = capaciteDisponible >= capaciteRequise;
        commande.setStatut(faisable ? StatutCommande.VALIDEE : StatutCommande.REFUSEE);
        commandeRepository.save(commande);

        log.info("Algorithme de faisabilité appliqué pour la commande ID {}. Résultat : {}", commandeId, commande.getStatut());
        return faisable;
    }

    /** Formules RH du cahier des charges (1850h/employé/an) */
    public int calculerEmployesAAjouter(double heuresDemandees, int nombreEmployesActuels) {
        double heuresDisponibles = nombreEmployesActuels * HEURES_PAR_EMPLOYE_AN;
        double ecart = heuresDemandees - heuresDisponibles;
        if (ecart <= 0) return 0;
        return (int) Math.ceil(ecart / HEURES_PAR_EMPLOYE_AN);
    }

    public int calculerEmployesARetirer(double heuresDemandees, int nombreEmployesActuels) {
        double heuresDisponibles = nombreEmployesActuels * HEURES_PAR_EMPLOYE_AN;
        double surplus = heuresDisponibles - heuresDemandees;
        if (surplus <= 0) return 0;
        return (int) Math.floor(surplus / HEURES_PAR_EMPLOYE_AN);
    }

    // --- MAPPERS PRIVÉS ---

    private CommandeDTO toDTO(Commande c) {
        return CommandeDTO.builder()
                .id(c.getId())
                .usineId(c.getUsine() != null ? c.getUsine().getId() : null)
                .statut(c.getStatut())
                .client(c.getClient())
                .quantiteDemandee(c.getQuantiteDemandee())
                .dateCommande(c.getDateCommande())
                .dateLivraisonSouhaitee(c.getDateLivraisonSouhaitee())
                .build();
    }

    private Commande toEntity(CommandeDTO dto) {
        Usine usine = usineRepository.findById(dto.getUsineId())
                .orElseThrow(() -> new RuntimeException("Usine non trouvée avec l'id : " + dto.getUsineId()));

        return Commande.builder()
                .usine(usine)
                .client(dto.getClient())
                .quantiteDemandee(dto.getQuantiteDemandee())
                .dateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee())
                .build();
    }
}