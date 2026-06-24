package com.safran.controller;

import com.safran.dto.ProgrammeDTO;
import com.safran.service.ProgrammeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/programmes")
@RequiredArgsConstructor
@Slf4j
public class ProgrammeController {

    private final ProgrammeService programmeService;

    @GetMapping("/activite/{activiteId}")
    public ResponseEntity<List<ProgrammeDTO>> getByActivite(@PathVariable Long activiteId) {
        log.info("Requête REST pour récupérer les programmes de l'activité ID: {}", activiteId);
        return ResponseEntity.ok(programmeService.findAllByActivite(activiteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgrammeDTO> getById(@PathVariable Long id) {
        log.info("Requête REST pour récupérer le programme ID: {}", id);
        return ResponseEntity.ok(programmeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProgrammeDTO dto) {
        log.info("Requête REST pour créer le programme : {}", dto.getNom());
        try {
            ProgrammeDTO result = programmeService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.error("Échec de création du programme : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ProgrammeDTO dto) {
        log.info("Requête REST pour modifier le programme ID: {}", id);
        try {
            ProgrammeDTO result = programmeService.update(id, dto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("Échec de mise à jour du programme ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Requête REST pour supprimer le programme ID: {}", id);
        programmeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}