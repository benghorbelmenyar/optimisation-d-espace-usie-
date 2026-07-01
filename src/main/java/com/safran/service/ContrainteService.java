package com.safran.service;

import com.safran.dto.ContrainteDTO;
import com.safran.entity.Contrainte;
import com.safran.entity.Poste;
import com.safran.entity.TypeContrainte;
import com.safran.repository.ContrainteRepository;
import com.safran.repository.PosteRepository; // 👈 Requis pour les vérifications
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 👈 Activation des logs
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContrainteService {

    private final ContrainteRepository contrainteRepository;
    private final PosteRepository posteRepository; // 👈 Injecté pour sécuriser les clés étrangères

    // ✨ AJOUT : Récupérer absolument toutes les contraintes
    public List<ContrainteDTO> findAll() {
        log.debug("Appel de findAll() dans ContrainteService");
        return contrainteRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ContrainteDTO> findAllByPoste(Long posteId) {
        log.debug("Appel de findAllByPoste() pour le poste ID: {}", posteId);
        return contrainteRepository.findByPosteSourceIdOrPosteCibleId(posteId, posteId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ContrainteDTO findById(Long id) {
        return contrainteRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.error("Erreur : La contrainte ID {} n'existe pas en base", id);
                    return new RuntimeException("Contrainte non trouvée avec id : " + id);
                });
    }

    public ContrainteDTO create(ContrainteDTO dto) {
        log.info("Tentative de création d'une contrainte entre le poste {} et le poste {}", dto.getPosteSourceId(), dto.getPosteCibleId());

        // 🛡️ SÉCURITÉ : Validation du poste source
        if (dto.getPosteSourceId() == null || !posteRepository.existsById(dto.getPosteSourceId())) {
            log.error("Échec création contrainte : Le poste source ID {} n'existe pas", dto.getPosteSourceId());
            throw new RuntimeException("Impossible de créer la contrainte : le poste source spécifié n'existe pas.");
        }

        // 🛡️ SÉCURITÉ : Validation du poste cible
        if (dto.getPosteCibleId() == null || !posteRepository.existsById(dto.getPosteCibleId())) {
            log.error("Échec création contrainte : Le poste cible ID {} n'existe pas", dto.getPosteCibleId());
            throw new RuntimeException("Impossible de créer la contrainte : le poste cible spécifié n'existe pas.");
        }

        Contrainte contrainte = toEntity(dto);
        Contrainte savedContrainte = contrainteRepository.save(contrainte);
        log.info("Contrainte enregistrée avec succès sous l'ID : {}", savedContrainte.getId());
        return toDTO(savedContrainte);
    }

    public ContrainteDTO update(Long id, ContrainteDTO dto) {
        Contrainte contrainte = contrainteRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Échec modification : Contrainte ID {} introuvable", id);
                    return new RuntimeException("Contrainte non trouvée avec id : " + id);
                });

        log.info("Mise à jour de la contrainte ID {}. Ancien type: {}, Nouveau: {}", id, contrainte.getType(), dto.getType());

        // 🛡️ LOGIQUE PUT : Modification dynamique et sécurisée du Poste Source
        if (dto.getPosteSourceId() != null && !dto.getPosteSourceId().equals(contrainte.getPosteSource().getId())) {
            log.warn("Changement de poste source détecté pour la contrainte ID {}.", id);
            Poste nouveauPosteSource = posteRepository.findById(dto.getPosteSourceId())
                    .orElseThrow(() -> new RuntimeException("Le nouveau poste source spécifié n'existe pas."));
            contrainte.setPosteSource(nouveauPosteSource);
        }

        // 🛡️ LOGIQUE PUT : Modification dynamique et sécurisée du Poste Cible
        if (dto.getPosteCibleId() != null && !dto.getPosteCibleId().equals(contrainte.getPosteCible().getId())) {
            log.warn("Changement de poste cible détecté pour la contrainte ID {}.", id);
            Poste nouveauPosteCible = posteRepository.findById(dto.getPosteCibleId())
                    .orElseThrow(() -> new RuntimeException("Le nouveau poste cible spécifié n'existe pas."));
            contrainte.setPosteCible(nouveauPosteCible);
        }

        contrainte.setType(dto.getType());
        contrainte.setValeur(dto.getValeur());

        Contrainte updatedContrainte = contrainteRepository.save(contrainte);
        log.info("Contrainte ID {} mise à jour en base de données.", id);
        return toDTO(updatedContrainte);
    }

    public void delete(Long id) {
        if (!contrainteRepository.existsById(id)) {
            log.error("Suppression impossible : Contrainte ID {} introuvable", id);
            throw new RuntimeException("Contrainte introuvable");
        }
        contrainteRepository.deleteById(id);
        log.info("Contrainte ID {} supprimée de la base de données.", id);
    }

    public boolean verifier(Contrainte contrainte, double distanceReelle, boolean sontAdjacents) {
        log.debug("Vérification de la contrainte ID {} (Type: {}). Valeur exigée: {}, Distance réelle: {}",
                contrainte.getId(), contrainte.getType(), contrainte.getValeur(), distanceReelle);
        switch (contrainte.getType()) {
            case DISTANCE:
                return distanceReelle >= contrainte.getValeur();
            case ADJACENCE:
                return sontAdjacents;
            case INCOMPATIBILITE:
                return distanceReelle >= contrainte.getValeur();
            default:
                return true;
        }
    }

    public void appliquer(Long posteSourceId, Long posteCibleId, TypeContrainte type, float valeur) {
        log.info("Application directe d'une contrainte de type {} entre {} et {}", type, posteSourceId, posteCibleId);
        Poste source = posteRepository.findById(posteSourceId).orElseThrow(() -> new RuntimeException("Source introuvable"));
        Poste cible = posteRepository.findById(posteCibleId).orElseThrow(() -> new RuntimeException("Cible introuvable"));

        Contrainte contrainte = Contrainte.builder()
                .posteSource(source)
                .posteCible(cible)
                .type(type)
                .valeur(valeur)
                .build();
        contrainteRepository.save(contrainte);
    }

    private ContrainteDTO toDTO(Contrainte c) {
        return ContrainteDTO.builder()
                .id(c.getId())
                .posteSourceId(c.getPosteSource() != null ? c.getPosteSource().getId() : null)
                .posteCibleId(c.getPosteCible() != null ? c.getPosteCible().getId() : null)
                .type(c.getType())
                .valeur(c.getValeur())
                .build();
    }

    private Contrainte toEntity(ContrainteDTO dto) {
        Poste source = posteRepository.findById(dto.getPosteSourceId())
                .orElseThrow(() -> new RuntimeException("Poste source introuvable avec l'id : " + dto.getPosteSourceId()));
        Poste cible = posteRepository.findById(dto.getPosteCibleId())
                .orElseThrow(() -> new RuntimeException("Poste cible introuvable avec l'id : " + dto.getPosteCibleId()));

        return Contrainte.builder()
                .posteSource(source)
                .posteCible(cible)
                .type(dto.getType())
                .valeur(dto.getValeur())
                .build();
    }
}