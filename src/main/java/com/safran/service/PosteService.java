package com.safran.service;

import com.safran.dto.PosteDTO;
import com.safran.entity.Poste;
import com.safran.entity.Usine;
import com.safran.entity.Programme;
import com.safran.enums.StatutCouleur;
import com.safran.repository.PosteRepository;
import com.safran.repository.UsineRepository;
import com.safran.repository.ProgrammeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosteService {

    private final PosteRepository posteRepository;
    private final UsineRepository usineRepository;
    private final ProgrammeRepository programmeRepository;

    @Transactional(readOnly = true)
    public List<PosteDTO> findAll() {
        log.debug("Appel de findAll() dans PosteService");
        return posteRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PosteDTO> findAllByUsine(Long usineId) {
        log.debug("Appel de findAllByUsine() pour l'usine ID: {}", usineId);
        return posteRepository.findByUsineId(usineId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PosteDTO findById(Long id) {
        return posteRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.error("Erreur : Le poste avec l'ID {} n'existe pas en base", id);
                    return new RuntimeException("Poste non trouvé avec id : " + id);
                });
    }

    @Transactional
    public PosteDTO create(PosteDTO dto) {
        log.info("Tentative de création de poste. Validation de l'usine ID: {} et Programme ID: {}", dto.getUsineId(), dto.getProgrammeId());

        if (dto.getUsineId() == null || !usineRepository.existsById(dto.getUsineId())) {
            log.error("Échec création poste : L'usine ID {} est introuvable !", dto.getUsineId());
            throw new IllegalArgumentException("Impossible de créer le poste : l'usine spécifiée n'existe pas.");
        }

        if (dto.getProgrammeId() == null || !programmeRepository.existsById(dto.getProgrammeId())) {
            log.error("Échec création poste : Le programme ID {} est introuvable !", dto.getProgrammeId());
            throw new IllegalArgumentException("Impossible de créer le poste : le programme spécifié n'existe pas.");
        }

        Poste poste = toEntity(dto);
        if (poste.getStatutCouleur() == null) {
            poste.setStatutCouleur(StatutCouleur.VERT);
        }

        Poste savedPoste = posteRepository.save(poste);
        log.info("Poste '{}' enregistré avec succès (ID: {}).", savedPoste.getNom(), savedPoste.getId());
        return toDTO(savedPoste);
    }

    @Transactional
    public PosteDTO update(Long id, PosteDTO dto) {
        Poste poste = posteRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Échec modification : Poste ID {} introuvable", id);
                    return new RuntimeException("Poste non trouvé avec id : " + id);
                });

        log.info("Mise à jour du poste ID {}. Ancien nom: '{}', Nouveau: '{}'", id, poste.getNom(), dto.getNom());

        Long currentUsineId = (poste.getUsine() != null) ? poste.getUsine().getId() : null;
        if (dto.getUsineId() != null && !dto.getUsineId().equals(currentUsineId)) {
            Usine nouvelleUsine = usineRepository.findById(dto.getUsineId())
                    .orElseThrow(() -> new IllegalArgumentException("La nouvelle usine spécifiée n'existe pas."));
            poste.setUsine(nouvelleUsine);
        }

        Long currentProgrammeId = (poste.getProgramme() != null) ? poste.getProgramme().getId() : null;
        if (dto.getProgrammeId() != null && !dto.getProgrammeId().equals(currentProgrammeId)) {
            Programme nouveauProgramme = programmeRepository.findById(dto.getProgrammeId())
                    .orElseThrow(() -> new IllegalArgumentException("Le nouveau programme spécifié n'existe pas."));
            poste.setProgramme(nouveauProgramme);
        }

        poste.setNom(dto.getNom());
        poste.setLongueur(dto.getLongueur());
        poste.setLargeur(dto.getLargeur());
        poste.setCycleTime(dto.getCycleTime());
        poste.setNombreOperateurs(dto.getNombreOperateurs());
        poste.setQuantite(dto.getQuantite());
        poste.setNombreShifts(dto.getNombreShifts() <= 0 ? 1 : dto.getNombreShifts()); // 💡 AJOUTÉ

        if (dto.getStatutCouleur() != null) {
            poste.setStatutCouleur(dto.getStatutCouleur());
        }

        Poste updatedPoste = posteRepository.save(poste);
        log.info("Poste ID {} mis à jour en base de données.", id);
        return toDTO(updatedPoste);
    }

    @Transactional
    public void delete(Long id) {
        if (!posteRepository.existsById(id)) {
            log.error("Suppression avortée : Poste ID {} introuvable", id);
            throw new RuntimeException("Poste introuvable");
        }
        posteRepository.deleteById(id);
        log.info("Poste ID {} supprimé.", id);
    }

    private PosteDTO toDTO(Poste p) {
        return PosteDTO.builder()
                .id(p.getId())
                .usineId(p.getUsine() != null ? p.getUsine().getId() : null)
                .programmeId(p.getProgramme() != null ? p.getProgramme().getId() : null)
                .nom(p.getNom())
                .longueur(p.getLongueur())
                .largeur(p.getLargeur())
                .cycleTime(p.getCycleTime())
                .nombreOperateurs(p.getNombreOperateurs())
                .quantite(p.getQuantite())
                .statutCouleur(p.getStatutCouleur())
                .nombreShifts(p.getNombreShifts()) // 💡 AJOUTÉ
                .build();
    }

    private Poste toEntity(PosteDTO dto) {
        Usine usine = usineRepository.findById(dto.getUsineId())
                .orElseThrow(() -> new RuntimeException("Usine non trouvée avec l'id : " + dto.getUsineId()));

        Programme programme = programmeRepository.findById(dto.getProgrammeId())
                .orElseThrow(() -> new RuntimeException("Programme non trouvé avec l'id : " + dto.getProgrammeId()));

        return Poste.builder()
                .usine(usine)
                .programme(programme)
                .nom(dto.getNom())
                .longueur(dto.getLongueur())
                .largeur(dto.getLargeur())
                .cycleTime(dto.getCycleTime())
                .nombreOperateurs(dto.getNombreOperateurs())
                .quantite(dto.getQuantite())
                .statutCouleur(dto.getStatutCouleur())
                .nombreShifts(dto.getNombreShifts() <= 0 ? 1 : dto.getNombreShifts()) // 💡 AJOUTÉ
                .build();
    }
}