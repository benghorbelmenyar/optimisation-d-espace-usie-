package com.safran.service;

import com.safran.dto.PosteDTO;
import com.safran.entity.Poste;
import com.safran.entity.Usine; // 👈 Important
import com.safran.enums.StatutCouleur;
import com.safran.repository.PosteRepository;
import com.safran.repository.UsineRepository; // 👈 Injection requise pour la clé étrangère
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosteService {

    private final PosteRepository posteRepository;
    private final UsineRepository usineRepository; // 👈 Injecté automatiquement

    // ✨ NOUVEAU : Récupération globale
    public List<PosteDTO> findAll() {
        log.debug("Appel de findAll() dans PosteService");
        return posteRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<PosteDTO> findAllByUsine(Long usineId) {
        log.debug("Appel de findAllByUsine() pour l'usine ID: {}", usineId);
        return posteRepository.findByUsineId(usineId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PosteDTO findById(Long id) {
        return posteRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.error("Erreur : Le poste avec l'ID {} n'existe pas en base", id);
                    return new RuntimeException("Poste non trouvé avec id : " + id);
                });
    }

    public PosteDTO create(PosteDTO dto) {
        log.info("Tentative de création de poste. Validation de l'usine ID: {}", dto.getUsineId());

        // 🛡️ SÉCURITÉ : Bloque si l'usine n'existe pas
        if (dto.getUsineId() == null || !usineRepository.existsById(dto.getUsineId())) {
            log.error("Échec création poste : L'usine ID {} est introuvable !", dto.getUsineId());
            throw new RuntimeException("Impossible de créer le poste : l'usine spécifiée n'existe pas.");
        }

        Poste poste = toEntity(dto);
        if (poste.getStatutCouleur() == null) {
            poste.setStatutCouleur(StatutCouleur.VERT);
        }

        Poste savedPoste = posteRepository.save(poste);
        log.info("Poste '{}' enregistré avec succès (ID: {}).", savedPoste.getNom(), savedPoste.getId());
        return toDTO(savedPoste);
    }

    public PosteDTO update(Long id, PosteDTO dto) {
        Poste poste = posteRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Échec modification : Poste ID {} introuvable", id);
                    return new RuntimeException("Poste non trouvé avec id : " + id);
                });

        log.info("Mise à jour du poste ID {}. Ancien nom: '{}', Nouveau: '{}'", id, poste.getNom(), dto.getNom());

        // 🛡️ SÉCURITÉ & LOGIQUE PUT : Changement et validation de l'usine
        Long currentUsineId = (poste.getUsine() != null) ? poste.getUsine().getId() : null;
        if (dto.getUsineId() != null && !dto.getUsineId().equals(currentUsineId)) {
            log.warn("Changement d'usine détecté pour le poste ID {}. Validation de la nouvelle usine: {}", id, dto.getUsineId());

            Usine nouvelleUsine = usineRepository.findById(dto.getUsineId())
                    .orElseThrow(() -> {
                        log.error("Échec PUT : La nouvelle usine ID {} n'existe pas.", dto.getUsineId());
                        return new RuntimeException("La nouvelle usine spécifiée n'existe pas.");
                    });
            poste.setUsine(nouvelleUsine);
        }

        poste.setNom(dto.getNom());
        poste.setLongueur(dto.getLongueur());
        poste.setLargeur(dto.getLargeur());
        poste.setCycleTime(dto.getCycleTime());
        poste.setNombreOperateurs(dto.getNombreOperateurs());
        poste.setQuantite(dto.getQuantite());

        if (dto.getStatutCouleur() != null) {
            poste.setStatutCouleur(dto.getStatutCouleur());
        }

        Poste updatedPoste = posteRepository.save(poste);
        log.info("Poste ID {} mis à jour en base de données.", id);
        return toDTO(updatedPoste);
    }

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
                // Extraction de l'ID via l'objet de relation Usine
                .usineId(p.getUsine() != null ? p.getUsine().getId() : null)
                .nom(p.getNom())
                .longueur(p.getLongueur()).largeur(p.getLargeur())
                .cycleTime(p.getCycleTime()).nombreOperateurs(p.getNombreOperateurs())
                .quantite(p.getQuantite()).statutCouleur(p.getStatutCouleur())
                .build();
    }

    private Poste toEntity(PosteDTO dto) {
        Usine usine = usineRepository.findById(dto.getUsineId())
                .orElseThrow(() -> new RuntimeException("Usine non trouvée pour le poste avec l'id : " + dto.getUsineId()));

        return Poste.builder()
                .usine(usine) // Assignation de l'objet Usine complet
                .nom(dto.getNom())
                .longueur(dto.getLongueur()).largeur(dto.getLargeur())
                .cycleTime(dto.getCycleTime()).nombreOperateurs(dto.getNombreOperateurs())
                .quantite(dto.getQuantite()).statutCouleur(dto.getStatutCouleur())
                .build();
    }
}