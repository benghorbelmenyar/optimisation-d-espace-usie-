package com.safran.service;

import com.safran.dto.ProcessusDTO;
import com.safran.entity.Processus;
import com.safran.entity.Programme;
import com.safran.entity.Zone; // 👈 FIX 1 : Importation de l'entité Zone manquante
import com.safran.repository.ProcessusRepository;
import com.safran.repository.ProgrammeRepository;
import com.safran.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessusService {

    private final ProcessusRepository processusRepository;
    private final ProgrammeRepository programmeRepository;
    private final ZoneRepository zoneRepository;

    @Transactional(readOnly = true)
    public List<ProcessusDTO> findAll() {
        return processusRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProcessusDTO findById(Long id) {
        return processusRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'id : " + id));
    }

    @Transactional
    public ProcessusDTO create(ProcessusDTO dto) {
        log.info("Création d'un nouveau processus : {}", dto.getNom());
        Processus processus = new Processus();
        processus.setNom(dto.getNom());
        processus.setTempsUnitaire(dto.getTempsUnitaire());
        processus.setTypeP(dto.getTypeP());
        processus.setQuantite(dto.getQuantite());
        processus.setNombreOperateurs(dto.getNombreOperateurs());
        processus.setTauxCharge(dto.getTauxCharge());

        // ⚙️ FIX 2 : Liaison sécurisée avec la Zone (Plus de doublons, plus de NULL)
        if (dto.getZoneId() != null) {
            Zone zone = zoneRepository.findById(dto.getZoneId())
                    .orElseThrow(() -> new IllegalArgumentException("La zone avec l'ID " + dto.getZoneId() + " n'existe pas."));
            processus.setZone(zone);
        }

        Processus savedProcessus = processusRepository.save(processus);
        return toDTO(savedProcessus);
    }

    @Transactional
    public ProcessusDTO update(Long id, ProcessusDTO dto) {
        log.info("Mise à jour du processus ID : {}", id);
        Processus processus = processusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'id : " + id));

        processus.setNom(dto.getNom());
        processus.setTempsUnitaire(dto.getTempsUnitaire());
        processus.setTypeP(dto.getTypeP());
        processus.setQuantite(dto.getQuantite());
        processus.setNombreOperateurs(dto.getNombreOperateurs());
        processus.setTauxCharge(dto.getTauxCharge());

        // Mise à jour de la relation avec la Zone lors du PUT
        if (dto.getZoneId() != null) {
            Zone zone = zoneRepository.findById(dto.getZoneId())
                    .orElseThrow(() -> new IllegalArgumentException("La zone spécifiée n'existe pas."));
            processus.setZone(zone);
        } else {
            processus.setZone(null);
        }

        if (dto.getProgrammeIds() != null) {
            List<Programme> programmes = programmeRepository.findAllById(dto.getProgrammeIds());
            processus.setProgrammes(programmes);
        }

        return toDTO(processusRepository.save(processus));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Suppression du processus ID : {}", id);
        processusRepository.deleteById(id);
    }

    // --- MAPPERS ---
    private ProcessusDTO toDTO(Processus p) {
        List<Long> pIds = p.getProgrammes() != null ?
                p.getProgrammes().stream().map(Programme::getId).collect(Collectors.toList()) : null;

        return ProcessusDTO.builder()
                .id(p.getId())
                .nom(p.getNom())
                .tempsUnitaire(p.getTempsUnitaire())
                .typeP(p.getTypeP())
                .quantite(p.getQuantite())
                .nombreOperateurs(p.getNombreOperateurs())
                .tauxCharge(p.getTauxCharge())
                .programmeIds(pIds)
                .zoneId(p.getZone() != null ? p.getZone().getId() : null) // 👈 FIX 3 : Mapping retour de la zone
                .build();
    }
}