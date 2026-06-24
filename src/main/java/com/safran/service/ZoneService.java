package com.safran.service;

import com.safran.dto.ZoneDTO;
import com.safran.entity.Zone;
import com.safran.entity.Usine; // 👈 Ajout de l'import de l'entité Usine
import com.safran.repository.ZoneRepository;
import com.safran.repository.MachineRepository;
import com.safran.repository.UsineRepository; // 👈 1. On importe le repository de l'usine
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final MachineRepository machineRepository;
    private final UsineRepository usineRepository; // 👈 2. On l'injecte ici via @RequiredArgsConstructor

    public List<ZoneDTO> findAll() {
        log.debug("Appel de findAll() dans ZoneService");
        return zoneRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ZoneDTO> findAllByUsine(Long usineId) {
        log.debug("Appel de findAllByUsine() pour l'usine ID: {}", usineId);
        // Optionnel : Vous pouvez aussi vérifier si l'usine existe ici
        return zoneRepository.findByUsineId(usineId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ZoneDTO findById(Long id) {
        return zoneRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.error("Erreur : La zone avec l'ID {} n'existe pas en base de données", id);
                    return new RuntimeException("Zone non trouvée avec id : " + id);
                });
    }

    public ZoneDTO create(ZoneDTO dto) {
        log.info("Tentative de création d'une zone. Vérification de l'existence de l'usine ID: {}", dto.getUsineId());

        // 🛡️ SÉCURITÉ : Vérification que l'usine ciblée existe bien
        if (dto.getUsineId() == null || !usineRepository.existsById(dto.getUsineId())) {
            log.error("Échec de la création : L'usine avec l'ID {} n'existe pas.", dto.getUsineId());
            throw new RuntimeException("Impossible de créer la zone : l'usine spécifiée (ID: " + dto.getUsineId() + ") n'existe pas.");
        }

        log.info("Usine validée. Sauvegarde de la zone '{}' en base de données...", dto.getNom());
        Zone zone = toEntity(dto);
        Zone savedZone = zoneRepository.save(zone);
        log.info("Zone créée avec succès. ID généré: {}", savedZone.getId());
        return toDTO(savedZone);
    }

    public ZoneDTO update(Long id, ZoneDTO dto) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Échec de la modification : Zone ID {} introuvable", id);
                    return new RuntimeException("Zone non trouvée avec id : " + id);
                });

        log.info("Mise à jour de la zone ID {}. Ancien nom: '{}', Nouveau nom: '{}'", id, zone.getNom(), dto.getNom());

        // 🛡️ CORRECTION : Utilisation de zone.getUsine().getId() au lieu de zone.getUsineId()
        Long currentUsineId = (zone.getUsine() != null) ? zone.getUsine().getId() : null;

        if (dto.getUsineId() != null && !dto.getUsineId().equals(currentUsineId)) {
            log.warn("Changement d'usine demandé pour la zone ID {}. Vérification de la nouvelle usine ID: {}", id, dto.getUsineId());

            // On récupère l'objet Usine complet depuis la base de données
            Usine nouvelleUsine = usineRepository.findById(dto.getUsineId())
                    .orElseThrow(() -> {
                        log.error("Échec de la mise à jour : La nouvelle usine ID {} n'existe pas.", dto.getUsineId());
                        return new RuntimeException("La nouvelle usine spécifiée n'existe pas.");
                    });

            log.info("Nouvelle usine validée. Passage de l'usine {} à l'usine {}", currentUsineId, dto.getUsineId());
            zone.setUsine(nouvelleUsine); // 👈 CORRECTION : setUsine au lieu de setUsineId
        }

        zone.setNom(dto.getNom());
        zone.setLongueur(dto.getLongueur());
        zone.setLargeur(dto.getLargeur());

        Zone updatedZone = zoneRepository.save(zone);
        log.info("Zone ID {} mise à jour et enregistrée avec succès.", id);
        return toDTO(updatedZone);
    }

    public void delete(Long id) {
        if (!zoneRepository.existsById(id)) {
            log.error("Impossible de supprimer : la zone ID {} n'existe pas", id);
            throw new RuntimeException("Zone introuvable");
        }
        zoneRepository.deleteById(id);
        log.info("Zone ID {} supprimée de la base de données", id);
    }

    public float calculerSurfaceDisponible(Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> {
                    log.error("Calcul surface impossible : Zone ID {} introuvable", zoneId);
                    return new RuntimeException("Zone non trouvée");
                });

        float surfaceTotale = zone.getLongueur() * zone.getLargeur();
        float surfaceOccupee = machineRepository.findByZoneId(zoneId).stream()
                .map(m -> m.getLongueur() * m.getLargeur())
                .reduce(0f, Float::sum);

        float disponible = surfaceTotale - surfaceOccupee;
        log.debug("Zone ID {}: Surface Totale = {}m², Occupée = {}m², Disponible = {}m²", zoneId, surfaceTotale, surfaceOccupee, disponible);
        return disponible;
    }

    private ZoneDTO toDTO(Zone zone) {
        return ZoneDTO.builder()
                .id(zone.getId())
                // 👈 CORRECTION : Extraction sécurisée de l'ID depuis l'objet Usine
                .usineId(zone.getUsine() != null ? zone.getUsine().getId() : null)
                .nom(zone.getNom())
                .longueur(zone.getLongueur())
                .largeur(zone.getLargeur())
                .surfaceDisponible(calculerSurfaceDisponible(zone.getId()))
                .build();
    }

    private Zone toEntity(ZoneDTO dto) {
        // 👈 CORRECTION : On récupère l'instance ou la référence de l'Usine requise pour le builder
        Usine usine = usineRepository.findById(dto.getUsineId())
                .orElseThrow(() -> new RuntimeException("Usine non trouvée avec id : " + dto.getUsineId()));

        return Zone.builder()
                .usine(usine) // 👈 CORRECTION : Association de l'objet complet
                .nom(dto.getNom())
                .longueur(dto.getLongueur())
                .largeur(dto.getLargeur())
                .build();
    }
}