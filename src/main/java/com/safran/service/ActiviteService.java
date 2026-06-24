package com.safran.service;

import com.safran.dto.ActiviteDTO;
import com.safran.dto.ProgrammeDTO;
import com.safran.entity.Activite;
import com.safran.entity.Usine;
import com.safran.repository.ActiviteRepository;
import com.safran.repository.ProgrammeRepository;
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
public class ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final ProgrammeRepository programmeRepository;
    private final UsineRepository usineRepository;

    public List<ActiviteDTO> findAll() {
        log.debug("Appel de findAll() dans ActiviteService");
        return activiteRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ActiviteDTO> findAllByUsine(Long usineId) {
        log.debug("Appel de findAllByUsine() pour l'usine ID: {}", usineId);
        return activiteRepository.findByUsineId(usineId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ActiviteDTO findById(Long id) {
        return activiteRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.error("Erreur : L'activité avec l'ID {} n'existe pas en base", id);
                    return new RuntimeException("Activité non trouvée avec id : " + id);
                });
    }

    @Transactional
    public ActiviteDTO create(ActiviteDTO dto) {
        log.info("Tentative de création d'une activité. Validation de l'usine ID: {}", dto.getUsineId());

        if (dto.getUsineId() == null || !usineRepository.existsById(dto.getUsineId())) {
            log.error("Échec création activité : L'usine avec l'ID {} n'existe pas.", dto.getUsineId());
            throw new IllegalArgumentException("Impossible de créer l'activité : l'usine spécifiée n'existe pas.");
        }

        log.info("Usine validée. Enregistrement de l'activité '{}'...", dto.getNom());
        Activite activite = toEntity(dto);
        activite.setDateCreation(LocalDate.now());

        Activite savedActivite = activiteRepository.save(activite);
        log.info("Activité créée avec succès. ID généré: {}", savedActivite.getId());
        return toDTO(savedActivite);
    }

    @Transactional
    public ActiviteDTO update(Long id, ActiviteDTO dto) {
        Activite activite = activiteRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Échec modification : Activité ID {} introuvable", id);
                    return new RuntimeException("Activité non trouvée avec id : " + id);
                });

        log.info("Mise à jour de l'activité ID {}. Ancien nom: '{}', Nouveau nom: '{}'", id, activite.getNom(), dto.getNom());

        Long currentUsineId = (activite.getUsine() != null) ? activite.getUsine().getId() : null;
        if (dto.getUsineId() != null && !dto.getUsineId().equals(currentUsineId)) {
            log.warn("Changement d'usine détecté pour l'activité ID {}. Validation de la nouvelle usine ID: {}", id, dto.getUsineId());

            Usine nouvelleUsine = usineRepository.findById(dto.getUsineId())
                    .orElseThrow(() -> {
                        log.error("Échec PUT : La nouvelle usine ID {} n'existe pas.", dto.getUsineId());
                        return new IllegalArgumentException("La nouvelle usine spécifiée n'existe pas.");
                    });

            log.info("Nouvelle usine validée. Affectation de l'activité à l'usine {}", dto.getUsineId());
            activite.setUsine(nouvelleUsine);
        }

        activite.setNom(dto.getNom());
        activite.setDescription(dto.getDescription());

        Activite updatedActivite = activiteRepository.save(activite);
        log.info("Activité ID {} mise à jour et enregistrée avec succès.", id);
        return toDTO(updatedActivite);
    }

    public void delete(Long id) {
        if (!activiteRepository.existsById(id)) {
            log.error("Impossible de supprimer : l'activité ID {} n'existe pas", id);
            throw new RuntimeException("Activité introuvable");
        }
        activiteRepository.deleteById(id);
        log.info("Activité ID {} supprimée de la base de données", id);
    }

    public List<ProgrammeDTO> listerProgrammes(Long activiteId) {
        log.debug("Récupération des programmes pour l'activité ID: {}", activiteId);
        return programmeRepository.findByActiviteId(activiteId)
                .stream()
                .map(p -> ProgrammeDTO.builder()
                        .id(p.getId())
                        // 🛡️ CORRECTION : Récupération via le nouvel objet de relation
                        .activiteId(p.getActivite() != null ? p.getActivite().getId() : null)
                        .nom(p.getNom())
                        .description(p.getDescription())
                        .dateCreation(p.getDateCreation())
                        .processus(p.getProcessus())
                        .build())
                .collect(Collectors.toList());
    }

    // --- MAPPERS PRIVÉS ---

    private ActiviteDTO toDTO(Activite a) {
        return ActiviteDTO.builder()
                .id(a.getId())
                .usineId(a.getUsine() != null ? a.getUsine().getId() : null)
                .nom(a.getNom())
                .description(a.getDescription())
                .dateCreation(a.getDateCreation())
                .build();
    }

    private Activite toEntity(ActiviteDTO dto) {
        Usine usine = usineRepository.findById(dto.getUsineId())
                .orElseThrow(() -> new RuntimeException("Usine non trouvée avec l'id : " + dto.getUsineId()));

        return Activite.builder()
                .usine(usine)
                .nom(dto.getNom())
                .description(dto.getDescription())
                .build();
    }
}