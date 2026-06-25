package com.safran.service;

import com.safran.dto.CommandeDTO;
import com.safran.dto.BesoinCommandeDTO;
import com.safran.entity.Commande;
import com.safran.entity.BesoinCommande;
import com.safran.entity.Usine;
import com.safran.entity.Processus;
import com.safran.entity.Programme; // <-- Import de ton entité Programme
import com.safran.enums.StatutCommande;
import com.safran.repository.CommandeRepository;
import com.safran.repository.UsineRepository;
import com.safran.repository.ProcessusRepository;
import com.safran.repository.ProgrammeRepository; // <-- Import du Repository
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

    private final CommandeRepository commandeRepository;
    private final UsineRepository usineRepository;
    private final ProcessusRepository processusRepository;
    private final ProgrammeRepository programmeRepository; // <-- Injection obligatoire

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

        if (dto.getUsineId() == null || !usineRepository.existsById(dto.getUsineId())) {
            throw new IllegalArgumentException("Impossible de créer la commande : l'usine spécifiée n'existe pas.");
        }

        // Récupération sécurisée du Programme à partir de son nom ou de son ID
        // Si ton DTO utilise une String pour le nom (ex: "Programme LEAP Moteurs"), on le cherche par son nom
        Programme programme = programmeRepository.findByNom(dto.getProgrammeAvion())
                .orElseThrow(() -> new IllegalArgumentException("Le programme aéronautique '" + dto.getProgrammeAvion() + "' n'existe pas."));

        // 1. Convertir les informations globales de la commande
        Commande commande = Commande.builder()
                .usine(usineRepository.findById(dto.getUsineId()).get())
                .client(dto.getClient())
                .programmeAvion(programme) // <-- FIX : On passe l'entité Programme, plus une String !
                .dateCommande(LocalDate.now())
                .dateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee())
                .statut(StatutCommande.EN_ATTENTE)
                .build();

        // 2. Associer chaque ligne de besoin (Soudure, Cintrage...) passée dans le DTO
        if (dto.getBesoins() != null) {
            List<BesoinCommande> besoinsEntities = dto.getBesoins().stream().map(besoinDto -> {
                Processus proc = processusRepository.findById(besoinDto.getProcessusId())
                        .orElseThrow(() -> new IllegalArgumentException("Le processus ID " + besoinDto.getProcessusId() + " n'existe pas."));

                return BesoinCommande.builder()
                        .commande(commande)
                        .processus(proc)
                        .heuresDemandees(besoinDto.getHeuresDemandees())
                        .build();
            }).collect(Collectors.toList());

            commande.setBesoins(besoinsEntities);
        }

        Commande savedCommande = commandeRepository.save(commande);
        log.info("[SUCCÈS] Commande globale ID {} créée.", savedCommande.getId());
        return toDTO(savedCommande);
    }

    @Transactional
    public CommandeDTO update(Long id, CommandeDTO dto) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée avec id : " + id));

        log.info("Mise à jour de la commande ID {}.", id);

        if (dto.getUsineId() != null) {
            Usine nouvelleUsine = usineRepository.findById(dto.getUsineId())
                    .orElseThrow(() -> new IllegalArgumentException("L'usine spécifiée n'existe pas."));
            commande.setUsine(nouvelleUsine);
        }

        if (dto.getProgrammeAvion() != null) {
            Programme programme = programmeRepository.findByNom(dto.getProgrammeAvion())
                    .orElseThrow(() -> new IllegalArgumentException("Le programme spécifié n'existe pas."));
            commande.setProgrammeAvion(programme); // <-- FIX : Modification propre
        }

        commande.setClient(dto.getClient());
        commande.setDateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee());

        if (dto.getStatut() != null) {
            commande.setStatut(dto.getStatut());
        }

        if (dto.getBesoins() != null) {
            commande.getBesoins().clear();
            List<BesoinCommande> nouveauxBesoins = dto.getBesoins().stream().map(besoinDto -> {
                Processus proc = processusRepository.findById(besoinDto.getProcessusId())
                        .orElseThrow(() -> new IllegalArgumentException("Processus non trouvé"));
                return BesoinCommande.builder()
                        .commande(commande)
                        .processus(proc)
                        .heuresDemandees(besoinDto.getHeuresDemandees())
                        .build();
            }).collect(Collectors.toList());
            commande.getBesoins().addAll(nouveauxBesoins);
        }

        Commande updatedCommande = commandeRepository.save(commande);
        return toDTO(updatedCommande);
    }

    public void delete(Long id) {
        if (!commandeRepository.existsById(id)) {
            throw new RuntimeException("Commande introuvable");
        }
        commandeRepository.deleteById(id);
        log.info("Commande ID {} supprimée définitivement.", id);
    }

    private CommandeDTO toDTO(Commande c) {
        List<BesoinCommandeDTO> besoinsDtos = null;
        if (c.getBesoins() != null) {
            besoinsDtos = c.getBesoins().stream().map(b -> BesoinCommandeDTO.builder()
                    .processusId(b.getProcessus() != null ? b.getProcessus().getId() : null)
                    .heuresDemandees(b.getHeuresDemandees())
                    .build()
            ).collect(Collectors.toList());
        }

        // On extrait le nom textuel du programme pour que le DTO reste inchangé à l'extérieur
        String nomProgramme = c.getProgrammeAvion() != null ? c.getProgrammeAvion().getNom() : null;

        return CommandeDTO.builder()
                .id(c.getId())
                .usineId(c.getUsine() != null ? c.getUsine().getId() : null)
                .programmeAvion(nomProgramme) // <-- FIX : On extrait la string attendue par le builder du DTO
                .statut(c.getStatut())
                .client(c.getClient())
                .dateCommande(c.getDateCommande())
                .dateLivraisonSouhaitee(c.getDateLivraisonSouhaitee())
                .besoins(besoinsDtos)
                .build();
    }
}