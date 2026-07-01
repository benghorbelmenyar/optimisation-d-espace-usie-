package com.safran.service;

import com.safran.dto.ProcessusDTO;
import com.safran.entity.Processus;
import com.safran.entity.Programme;
import com.safran.entity.Zone;
import com.safran.repository.ProcessusRepository;
import com.safran.repository.ProgrammeRepository;
import com.safran.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        log.debug("Appel de findAll() dans ProcessusService");
        return processusRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProcessusDTO findById(Long id) {
        log.debug("Recherche du processus ID : {}", id);
        return processusRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'id : " + id));
    }

    @Transactional
    public ProcessusDTO create(ProcessusDTO dto) {
        log.info("Création d'un nouveau processus macro : {}", dto.getNom());

        Processus processus = new Processus();
        processus.setNom(dto.getNom());
        processus.setChargeAnnuelle(dto.getChargeAnnuelle()); // 💡 Mis à jour (ex: 108h)
        processus.setAnneeCharge(dto.getAnneeCharge() <= 0 ? 2026 : dto.getAnneeCharge()); // 💡 Géré automatiquement si absent
        processus.setDateAjoutCharge(LocalDateTime.now()); // 💡 Horodatage automatique
        processus.setTypeP(dto.getTypeP());
        processus.setNombreOperateurs(dto.getNombreOperateurs());
        processus.setTauxCharge(dto.getTauxCharge());

        // Liaison avec la Zone (Relation OneToOne)
        if (dto.getZoneId() != null) {
            Zone zone = zoneRepository.findById(dto.getZoneId())
                    .orElseThrow(() -> new IllegalArgumentException("La zone avec l'ID " + dto.getZoneId() + " n'existe pas."));
            processus.setZone(zone);
        }

        // Liaison ManyToMany avec les programmes associés
        if (dto.getProgrammeIds() != null && !dto.getProgrammeIds().isEmpty()) {
            List<Programme> programmes = programmeRepository.findAllById(dto.getProgrammeIds());
            processus.setProgrammes(programmes);
        }

        Processus savedProcessus = processusRepository.save(processus);
        log.info("Processus '{}' enregistré avec succès avec l'ID : {}", savedProcessus.getNom(), savedProcessus.getId());
        return toDTO(savedProcessus);
    }

    @Transactional
    public ProcessusDTO update(Long id, ProcessusDTO dto) {
        log.info("Mise à jour du processus ID : {}", id);
        Processus processus = processusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'id : " + id));

        processus.setNom(dto.getNom());
        processus.setChargeAnnuelle(dto.getChargeAnnuelle()); // 💡 Mis à jour
        processus.setAnneeCharge(dto.getAnneeCharge() <= 0 ? 2026 : dto.getAnneeCharge()); // 💡 Mis à jour
        processus.setDateAjoutCharge(LocalDateTime.now()); // 💡 Actualisation automatique de la date de modification
        processus.setTypeP(dto.getTypeP());
        processus.setNombreOperateurs(dto.getNombreOperateurs());
        processus.setTauxCharge(dto.getTauxCharge());

        // Mise à jour de la relation OneToOne Zone
        if (dto.getZoneId() != null) {
            Zone zone = zoneRepository.findById(dto.getZoneId())
                    .orElseThrow(() -> new IllegalArgumentException("La zone spécifiée n'existe pas."));
            processus.setZone(zone);
        } else {
            processus.setZone(null);
        }

        // Mise à jour de la relation ManyToMany Programmes
        if (dto.getProgrammeIds() != null) {
            List<Programme> programmes = programmeRepository.findAllById(dto.getProgrammeIds());
            processus.setProgrammes(programmes);
        }

        Processus updatedProcessus = processusRepository.save(processus);
        log.info("Processus ID {} mis à jour en base de données.", id);
        return toDTO(updatedProcessus);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Suppression du processus ID : {}", id);
        if (!processusRepository.existsById(id)) {
            log.error("Suppression avortée : Processus ID {} introuvable", id);
            throw new RuntimeException("Processus introuvable");
        }
        processusRepository.deleteById(id);
        log.info("Processus ID {} supprimé de la base.", id);
    }

    // --- MAPPERS ---
    private ProcessusDTO toDTO(Processus p) {
        List<Long> pIds = p.getProgrammes() != null ?
                p.getProgrammes().stream().map(Programme::getId).collect(Collectors.toList()) : null;

        return ProcessusDTO.builder()
                .id(p.getId())
                .nom(p.getNom())
                .chargeAnnuelle(p.getChargeAnnuelle()) // 💡 Mis à jour
                .anneeCharge(p.getAnneeCharge())       // 💡 Mis à jour
                .dateAjoutCharge(p.getDateAjoutCharge()) // 💡 Mis à jour
                .typeP(p.getTypeP())
                .nombreOperateurs(p.getNombreOperateurs())
                .tauxCharge(p.getTauxCharge())
                .programmeIds(pIds)
                .zoneId(p.getZone() != null ? p.getZone().getId() : null)
                .build();
    }
}