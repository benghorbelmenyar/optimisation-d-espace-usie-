package com.safran.service;

import com.safran.dto.ProgrammeDTO;
import com.safran.entity.Activite;
import com.safran.entity.Programme;
import com.safran.repository.ActiviteRepository;
import com.safran.repository.ProgrammeRepository;
import com.safran.repository.ProcessusRepository; // 👈 AJOUTÉ pour lister ou lier les processus si nécessaire
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
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;
    private final ActiviteRepository activiteRepository;
    private final ProcessusRepository processusRepository; // 👈 Injecté si tu veux gérer les liaisons ici

    public List<ProgrammeDTO> findAllByActivite(Long activiteId) {
        log.debug("Recherche des programmes pour l'activité ID: {}", activiteId);
        return programmeRepository.findByActiviteId(activiteId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProgrammeDTO findById(Long id) {
        return programmeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Programme non trouvé avec id : " + id));
    }

    @Transactional
    public ProgrammeDTO create(ProgrammeDTO dto) {
        log.info("[VALIDATION] Tentative de création du programme : {}", dto.getNom());

        if (dto.getActiviteId() == null || !activiteRepository.existsById(dto.getActiviteId())) {
            throw new IllegalArgumentException("Impossible de créer le programme : l'activité avec l'ID " + dto.getActiviteId() + " n'existe pas.");
        }

        Activite activite = activiteRepository.findById(dto.getActiviteId()).get();

        Programme programme = toEntity(dto);
        programme.setActivite(activite);
        programme.setDateCreation(LocalDate.now());

        // 🔗 Gestion optionnelle de la relation ManyToMany si des IDs sont fournis à la création
        if (dto.getProcessusIds() != null && !dto.getProcessusIds().isEmpty()) {
            programme.setProcessus(processusRepository.findAllById(dto.getProcessusIds()));
        }

        Programme savedProgramme = programmeRepository.save(programme);
        log.info("[SUCCÈS] Programme ID {} créé pour l'activité ID {}", savedProgramme.getId(), activite.getId());
        return toDTO(savedProgramme);
    }

    @Transactional
    public ProgrammeDTO update(Long id, ProgrammeDTO dto) {
        Programme programme = programmeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programme non trouvé avec id : " + id));

        log.info("Mise à jour du programme ID {}.", id);

        Long currentActiviteId = (programme.getActivite() != null) ? programme.getActivite().getId() : null;
        if (dto.getActiviteId() != null && !dto.getActiviteId().equals(currentActiviteId)) {
            if (!activiteRepository.existsById(dto.getActiviteId())) {
                throw new IllegalArgumentException("Impossible de modifier le programme : la nouvelle activité avec l'ID " + dto.getActiviteId() + " n'existe pas.");
            }
            Activite nouvelleActivite = activiteRepository.findById(dto.getActiviteId()).get();
            programme.setActivite(nouvelleActivite);
        }

        programme.setNom(dto.getNom());
        programme.setDescription(dto.getDescription());

        // 🔗 Mise à jour optionnelle de la relation ManyToMany
        if (dto.getProcessusIds() != null) {
            programme.setProcessus(processusRepository.findAllById(dto.getProcessusIds()));
        }

        return toDTO(programmeRepository.save(programme));
    }

    public void delete(Long id) {
        if (!programmeRepository.existsById(id)) {
            throw new RuntimeException("Programme introuvable");
        }
        programmeRepository.deleteById(id);
        log.info("Programme ID {} supprimé définitivement.", id);
    }

    public Long getActivite(Long programmeId) {
        Programme p = programmeRepository.findById(programmeId)
                .orElseThrow(() -> new RuntimeException("Programme non trouvé"));
        return p.getActivite() != null ? p.getActivite().getId() : null;
    }

    // --- MAPPERS PRIVÉS ---

    private ProgrammeDTO toDTO(Programme p) {
        return ProgrammeDTO.builder()
                .id(p.getId())
                .activiteId(p.getActivite() != null ? p.getActivite().getId() : null)
                .nom(p.getNom())
                .description(p.getDescription())
                .dateCreation(p.getDateCreation())
                // 🛡️ FIX : Extraction propre des IDs de processus à partir de l'entité
                .processusIds(p.getProcessus() != null ?
                        p.getProcessus().stream().map(proc -> proc.getId()).collect(Collectors.toList()) : null)
                .build();
    }

    private Programme toEntity(ProgrammeDTO dto) {
        return Programme.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                // La gestion de la liste complète est déléguée aux transactions d'écriture ci-dessus
                .build();
    }
}