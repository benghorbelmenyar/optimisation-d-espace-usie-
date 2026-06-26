package com.safran.service;

import com.safran.dto.ZoneDTO;
import com.safran.entity.Zone;
import com.safran.entity.Usine;
import com.safran.repository.ZoneRepository;
import com.safran.repository.MachineRepository;
import com.safran.repository.UsineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final MachineRepository machineRepository;
    private final UsineRepository usineRepository;

    @Transactional(readOnly = true)
    public List<ZoneDTO> findAll() {
        log.debug("Appel de findAll() dans ZoneService");
        return zoneRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ZoneDTO> findAllByUsine(Long usineId) {
        log.debug("Appel de findAllByUsine() pour l'usine ID: {}", usineId);
        return zoneRepository.findByUsineId(usineId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ZoneDTO findById(Long id) {
        return zoneRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.error("Erreur : La zone avec l'ID {} n'existe pas en base de données", id);
                    return new RuntimeException("Zone non trouvée avec id : " + id);
                });
    }

    @Transactional
    public ZoneDTO create(ZoneDTO dto) {
        log.info("Tentative de création d'une zone. Vérification de l'existence de l'usine ID: {}", dto.getUsineId());

        // 🛡️ SÉCURITÉ : Vérification et récupération de l'usine pour éviter le usine_id NULL
        if (dto.getUsineId() == null) {
            throw new IllegalArgumentException("Le champ 'usineId' est obligatoire pour créer une zone.");
        }

        Usine usine = usineRepository.findById(dto.getUsineId())
                .orElseThrow(() -> {
                    log.error("Échec de la création : L'usine avec l'ID {} n'existe pas.", dto.getUsineId());
                    return new RuntimeException("Impossible de créer la zone : l'usine spécifiée n'existe pas.");
                });

        log.info("Usine validée. Sauvegarde de la zone '{}' en base de données...", dto.getNom());

        Zone zone = toEntity(dto);
        zone.setUsine(usine); // 👈 FIX : On attache fermement l'usine récupérée

        // Calcul automatique de la surface totale au sol avant sauvegarde
        zone.setSurfaceTotale(zone.getLongueur() * zone.getLargeur());

        Zone savedZone = zoneRepository.save(zone);
        log.info("Zone créée avec succès. ID généré: {}", savedZone.getId());
        return toDTO(savedZone);
    }

    @Transactional
    public ZoneDTO update(Long id, ZoneDTO dto) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Échec de la modification : Zone ID {} introuvable", id);
                    return new RuntimeException("Zone non trouvée avec id : " + id);
                });

        log.info("Mise à jour de la zone ID {}. Ancien nom: '{}', Nouveau nom: '{}'", id, zone.getNom(), dto.getNom());

        Long currentUsineId = (zone.getUsine() != null) ? zone.getUsine().getId() : null;

        if (dto.getUsineId() != null && !dto.getUsineId().equals(currentUsineId)) {
            log.warn("Changement d'usine demandé pour la zone ID {}. Vérification de la nouvelle usine ID: {}", id, dto.getUsineId());

            Usine nouvelleUsine = usineRepository.findById(dto.getUsineId())
                    .orElseThrow(() -> {
                        log.error("Échec de la mise à jour : La nouvelle usine ID {} n'existe pas.", dto.getUsineId());
                        return new RuntimeException("La nouvelle usine spécifiée n'existe pas.");
                    });

            log.info("Nouvelle usine validée. Passage de l'usine {} à l'usine {}", currentUsineId, dto.getUsineId());
            zone.setUsine(nouvelleUsine);
        }

        zone.setNom(dto.getNom());
        zone.setLongueur(dto.getLongueur());
        zone.setLargeur(dto.getLargeur());
        zone.setSurfaceRequiseParPoste(dto.getSurfaceRequiseParPoste());
        zone.setSurfaceTotale(dto.getLongueur() * dto.getLargeur()); // Recalcul de la surface totale

        Zone updatedZone = zoneRepository.save(zone);
        log.info("Zone ID {} mise à jour et enregistrée avec succès.", id);
        return toDTO(updatedZone);
    }

    @Transactional
    public void delete(Long id) {
        if (!zoneRepository.existsById(id)) {
            log.error("Impossible de supprimer : la zone ID {} n'existe pas", id);
            throw new RuntimeException("Zone introuvable");
        }
        zoneRepository.deleteById(id);
        log.info("Zone ID {} supprimée de la base de données", id);
    }

    @Transactional(readOnly = true)
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

    public ZoneDTO toDTO(Zone zone) {
        if (zone == null) return null;

        // On utilise la méthode dynamique calculerSurfaceDisponible pour envoyer l'espace réellement libre au DTO
        float disponible = calculerSurfaceDisponible(zone.getId());

        return ZoneDTO.builder()
                .id(zone.getId())
                .usineId(zone.getUsine() != null ? zone.getUsine().getId() : null)
                .nom(zone.getNom())
                .longueur(zone.getLongueur()) // 👈 FIX : Ajout du mapping manquant
                .largeur(zone.getLargeur())   // 👈 FIX : Ajout du mapping manquant
                .surfaceDisponible(disponible)
                .surfaceRequiseParPoste(zone.getSurfaceRequiseParPoste())
                .build();
    }

    public Zone toEntity(ZoneDTO dto) {
        if (dto == null) return null;

        Zone zone = new Zone();
        zone.setId(dto.getId());
        zone.setNom(dto.getNom());
        zone.setLongueur(dto.getLongueur()); // 👈 FIX : Ajout du mapping manquant
        zone.setLargeur(dto.getLargeur());   // 👈 FIX : Ajout du mapping manquant
        zone.setSurfaceRequiseParPoste(dto.getSurfaceRequiseParPoste());

        // Note: L'usine est gérée directement dans les méthodes create et update pour plus de sécurité
        return zone;
    }
}