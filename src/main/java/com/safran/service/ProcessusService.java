package com.safran.service;

import com.safran.dto.ProcessusDTO;
import com.safran.entity.Processus;
import com.safran.entity.Programme;
import com.safran.repository.ProcessusRepository;
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
public class ProcessusService {

    private final ProcessusRepository processusRepository;
    private final ProgrammeRepository programmeRepository;

    public List<ProcessusDTO> findAll() {
        return processusRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProcessusDTO findById(Long id) {
        return processusRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'id : " + id));
    }

    @Transactional
    public ProcessusDTO create(ProcessusDTO dto) {
        log.info("Création du processus : {}", dto.getNom());
        Processus processus = Processus.builder()
                .nom(dto.getNom())
                .tempsUnitaire(dto.getTempsUnitaire())
                .typeP(dto.getTypeP())
                .quantite(dto.getQuantite())
                .nombreOperateurs(dto.getNombreOperateurs())
                .tauxCharge(dto.getTauxCharge())
                .build();

        // Gestion de la relation ManyToMany à la création
        if (dto.getProgrammeIds() != null && !dto.getProgrammeIds().isEmpty()) {
            List<Programme> programmes = programmeRepository.findAllById(dto.getProgrammeIds());
            processus.setProgrammes(programmes);
        }

        return toDTO(processusRepository.save(processus));
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

        // Mise à jour de la relation ManyToMany
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
                .build();
    }
}