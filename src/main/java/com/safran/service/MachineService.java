package com.safran.service;

import com.safran.dto.MachineDTO;
import com.safran.entity.Machine;
import com.safran.entity.Usine;
import com.safran.entity.Zone;
import com.safran.repository.MachineRepository;
import com.safran.repository.UsineRepository; // 👈 Injection requise
import com.safran.repository.ZoneRepository;  // 👈 Injection requise
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MachineService {

    private final MachineRepository machineRepository;
    private final UsineRepository usineRepository;
    private final ZoneRepository zoneRepository;

    public List<MachineDTO> findAllByUsine(Long usineId) {
        log.debug("Recherche de toutes les machines de l'usine ID: {}", usineId);
        return machineRepository.findByUsineId(usineId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MachineDTO findById(Long id) {
        return machineRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.error("Machine introuvable avec l'id : {}", id);
                    return new RuntimeException("Machine non trouvée avec id : " + id);
                });
    }

    @Transactional
    public MachineDTO create(MachineDTO dto) {
        log.info("[VALIDATION] Tentative de création d'une machine : {}", dto.getNom());

        // 🛡️ SÉCURITÉ STRICTE : Vérification de l'existence de l'usine et de la zone
        if (dto.getUsineId() == null || !usineRepository.existsById(dto.getUsineId())) {
            throw new IllegalArgumentException("Impossible de créer la machine : l'usine avec l'ID " + dto.getUsineId() + " n'existe pas.");
        }
        if (dto.getZoneId() == null || !zoneRepository.existsById(dto.getZoneId())) {
            throw new IllegalArgumentException("Impossible de créer la machine : la zone avec l'ID " + dto.getZoneId() + " n'existe pas.");
        }

        Machine machine = toEntity(dto);
        Machine savedMachine = machineRepository.save(machine);
        log.info("[SUCCÈS] Machine ID {} créée.", savedMachine.getId());
        return toDTO(savedMachine);
    }

    @Transactional
    public MachineDTO update(Long id, MachineDTO dto) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvée avec id : " + id));

        log.info("Mise à jour de la machine ID {}.", id);

        // 🛡️ SÉCURITÉ STRICTE SUR LE PUT : Validation de la nouvelle usine s'il y a un changement
        Long currentUsineId = (machine.getUsine() != null) ? machine.getUsine().getId() : null;
        if (dto.getUsineId() != null && !dto.getUsineId().equals(currentUsineId)) {
            if (!usineRepository.existsById(dto.getUsineId())) {
                throw new IllegalArgumentException("Impossible de modifier la machine : la nouvelle usine avec l'ID " + dto.getUsineId() + " n'existe pas.");
            }
            Usine nouvelleUsine = usineRepository.findById(dto.getUsineId()).get();
            machine.setUsine(nouvelleUsine);
        }

        // 🛡️ SÉCURITÉ STRICTE SUR LE PUT : Validation de la nouvelle zone s'il y a un changement
        Long currentZoneId = (machine.getZone() != null) ? machine.getZone().getId() : null;
        if (dto.getZoneId() != null && !dto.getZoneId().equals(currentZoneId)) {
            if (!zoneRepository.existsById(dto.getZoneId())) {
                throw new IllegalArgumentException("Impossible de modifier la machine : la nouvelle zone avec l'ID " + dto.getZoneId() + " n'existe pas.");
            }
            Zone nouvelleZone = zoneRepository.findById(dto.getZoneId()).get();
            machine.setZone(nouvelleZone);
        }

        machine.setNom(dto.getNom());
        machine.setLongueur(dto.getLongueur());
        machine.setLargeur(dto.getLargeur());

        return toDTO(machineRepository.save(machine));
    }

    public void delete(Long id) {
        if (!machineRepository.existsById(id)) {
            throw new RuntimeException("Machine introuvable");
        }
        machineRepository.deleteById(id);
        log.info("Machine ID {} supprimée définitivement.", id);
    }

    // --- MAPPERS PRIVÉS ---

    private MachineDTO toDTO(Machine m) {
        return MachineDTO.builder()
                .id(m.getId())
                .usineId(m.getUsine() != null ? m.getUsine().getId() : null)
                .zoneId(m.getZone() != null ? m.getZone().getId() : null)
                .nom(m.getNom())
                .longueur(m.getLongueur())
                .largeur(m.getLargeur())
                .build();
    }

    private Machine toEntity(MachineDTO dto) {
        Usine usine = usineRepository.findById(dto.getUsineId())
                .orElseThrow(() -> new RuntimeException("Usine non trouvée avec l'id : " + dto.getUsineId()));
        Zone zone = zoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new RuntimeException("Zone non trouvée avec l'id : " + dto.getZoneId()));

        return Machine.builder()
                .usine(usine)
                .zone(zone)
                .nom(dto.getNom())
                .longueur(dto.getLongueur())
                .largeur(dto.getLargeur())
                .build();
    }
}