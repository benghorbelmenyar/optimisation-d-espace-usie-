package com.safran.service;

import com.safran.dto.ZoneDTO;
import com.safran.dto.MachineDTO;
import com.safran.entity.Zone;
import com.safran.entity.Usine;
import com.safran.entity.Machine;
import com.safran.repository.ZoneRepository;
import com.safran.repository.MachineRepository;
import com.safran.repository.UsineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        log.info("Tentative de création d'une zone pour l'usine ID: {}", dto.getUsineId());

        if (dto.getUsineId() == null) {
            throw new IllegalArgumentException("Le champ 'usineId' est obligatoire pour créer une zone.");
        }

        Usine usine = usineRepository.findById(dto.getUsineId())
                .orElseThrow(() -> new RuntimeException("Impossible de créer la zone : l'usine spécifiée n'existe pas."));

        Zone zone = toEntity(dto);
        zone.setUsine(usine);
        zone.setSurfaceTotale(zone.getLongueur() * zone.getLargeur());

        if (zone.getMachines() != null) {
            for (Machine machine : zone.getMachines()) {
                machine.setZone(zone);
                machine.setUsine(usine); // Une machine hérite de l'usine liée à sa zone
            }
        }

        Zone savedZone = zoneRepository.save(zone);
        log.info("Zone et machines créées avec succès. ID généré: {}", savedZone.getId());
        return toDTO(savedZone);
    }

    @Transactional
    public ZoneDTO update(Long id, ZoneDTO dto) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone non trouvée avec id : " + id));

        Long currentUsineId = (zone.getUsine() != null) ? zone.getUsine().getId() : null;
        Usine usineConcernee = zone.getUsine();

        if (dto.getUsineId() != null && !dto.getUsineId().equals(currentUsineId)) {
            usineConcernee = usineRepository.findById(dto.getUsineId())
                    .orElseThrow(() -> new RuntimeException("La nouvelle usine spécifiée n'existe pas."));
            zone.setUsine(usineConcernee);
        }

        zone.setNom(dto.getNom());
        zone.setLongueur(dto.getLongueur());
        zone.setLargeur(dto.getLargeur());
        zone.setSurfaceRequiseParPoste(dto.getSurfaceRequiseParPoste());
        zone.setSurfaceTotale(dto.getLongueur() * dto.getLargeur());

        // 💡 FIX : Remplacement complet des méthodes toEntity de DTO obsolètes
        if (dto.getMachines() != null) {
            zone.getMachines().clear();
            final Usine finalUsine = usineConcernee;
            List<Machine> nouvellesMachines = dto.getMachines().stream()
                    .map(mDTO -> Machine.builder()
                            .id(mDTO.getId())
                            .nom(mDTO.getNom())
                            .longueur(mDTO.getLongueur())
                            .largeur(mDTO.getLargeur())
                            .zone(zone)
                            .usine(finalUsine)
                            .build()
                    ).collect(Collectors.toList());
            zone.getMachines().addAll(nouvellesMachines);
        }

        Zone updatedZone = zoneRepository.save(zone);
        return toDTO(updatedZone);
    }

    @Transactional
    public void delete(Long id) {
        if (!zoneRepository.existsById(id)) {
            throw new RuntimeException("Zone introuvable");
        }
        zoneRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public float calculerSurfaceDisponible(Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new RuntimeException("Zone non trouvée"));

        float surfaceTotale = zone.getLongueur() * zone.getLargeur();
        float surfaceOccupee = machineRepository.findByZoneId(zoneId).stream()
                .map(m -> m.getLongueur() * m.getLargeur())
                .reduce(0f, Float::sum);

        return surfaceTotale - surfaceOccupee;
    }

    public ZoneDTO toDTO(Zone zone) {
        if (zone == null) return null;

        float disponible = calculerSurfaceDisponible(zone.getId());

        List<MachineDTO> machineDTOs = new ArrayList<>();
        if (zone.getMachines() != null) {
            machineDTOs = zone.getMachines().stream()
                    .map(m -> MachineDTO.builder()
                            .id(m.getId())
                            .nom(m.getNom())
                            .longueur(m.getLongueur())
                            .largeur(m.getLargeur())
                            .zoneId(zone.getId())
                            .usineId(zone.getUsine() != null ? zone.getUsine().getId() : null)
                            .build())
                    .collect(Collectors.toList());
        }

        return ZoneDTO.builder()
                .id(zone.getId())
                .usineId(zone.getUsine() != null ? zone.getUsine().getId() : null)
                .nom(zone.getNom())
                .longueur(zone.getLongueur())
                .largeur(zone.getLargeur())
                .surfaceDisponible(disponible)
                .surfaceRequiseParPoste(zone.getSurfaceRequiseParPoste())
                .machines(machineDTOs)
                .build();
    }

    public Zone toEntity(ZoneDTO dto) {
        if (dto == null) return null;

        Zone zone = new Zone();
        zone.setId(dto.getId());
        zone.setNom(dto.getNom());
        zone.setLongueur(dto.getLongueur());
        zone.setLargeur(dto.getLargeur());
        zone.setSurfaceRequiseParPoste(dto.getSurfaceRequiseParPoste());

        if (dto.getMachines() != null) {
            List<Machine> entitesMachines = dto.getMachines().stream()
                    .map(mDTO -> Machine.builder()
                            .id(mDTO.getId())
                            .nom(mDTO.getNom())
                            .longueur(mDTO.getLongueur())
                            .largeur(mDTO.getLargeur())
                            .zone(zone)
                            .build()
                    ).collect(Collectors.toList());
            zone.setMachines(entitesMachines);
        } else {
            zone.setMachines(new ArrayList<>());
        }

        return zone;
    }
}