package com.safran.controller;

import com.safran.dto.MachineDTO;
import com.safran.service.MachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
@Slf4j
public class MachineController {

    private final MachineService machineService;

    @GetMapping("/usine/{usineId}")
    public ResponseEntity<List<MachineDTO>> getByUsine(@PathVariable Long usineId) {
        log.info("Requête REST pour récupérer les machines de l'usine ID: {}", usineId);
        return ResponseEntity.ok(machineService.findAllByUsine(usineId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MachineDTO> getById(@PathVariable Long id) {
        log.info("Requête REST pour récupérer la machine ID: {}", id);
        return ResponseEntity.ok(machineService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<?> create(@Valid @RequestBody MachineDTO dto) {
        log.info("Requête REST pour créer la machine : {}", dto.getNom());
        try {
            MachineDTO result = machineService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.error("Échec de création de la machine : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MachineDTO dto) {
        log.info("Requête REST pour modifier la machine ID: {}", id);
        try {
            MachineDTO result = machineService.update(id, dto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("Échec de mise à jour de la machine ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Requête REST pour supprimer la machine ID: {}", id);
        machineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}