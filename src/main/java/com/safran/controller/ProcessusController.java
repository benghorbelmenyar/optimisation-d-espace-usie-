package com.safran.controller;

import com.safran.dto.ProcessusDTO;
import com.safran.service.ProcessusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/processus")
@RequiredArgsConstructor
@Slf4j
public class ProcessusController {

    private final ProcessusService processusService;

    @GetMapping
    public ResponseEntity<List<ProcessusDTO>> getAll() {
        log.info("Appel REST pour obtenir tous les processus");
        return ResponseEntity.ok(processusService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessusDTO> getById(@PathVariable Long id) {
        log.info("Appel REST pour obtenir le processus ID : {}", id);
        return ResponseEntity.ok(processusService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<ProcessusDTO> create(@Valid @RequestBody ProcessusDTO dto) {
        log.info("Appel REST pour créer un processus");
        return ResponseEntity.status(HttpStatus.CREATED).body(processusService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<ProcessusDTO> update(@PathVariable Long id, @Valid @RequestBody ProcessusDTO dto) {
        log.info("Appel REST pour modifier le processus ID : {}", id);
        return ResponseEntity.ok(processusService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Appel REST pour supprimer le processus ID : {}", id);
        processusService.delete(id);
        return ResponseEntity.noContent().build();
    }
}