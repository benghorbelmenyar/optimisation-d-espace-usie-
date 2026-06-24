package com.safran.controller;

import com.safran.dto.ContrainteDTO;
import com.safran.service.ContrainteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/contraintes")
@RequiredArgsConstructor
@Slf4j
public class ContrainteController {

    private final ContrainteService contrainteService;

    // ✨ NOUVEAU : Récupérer toutes les contraintes de l'application
    @GetMapping
    public ResponseEntity<List<ContrainteDTO>> getAll() {
        log.info("Requête REST pour récupérer toutes les contraintes");
        return ResponseEntity.ok(contrainteService.findAll());
    }

    @GetMapping("/poste/{posteId}")
    public ResponseEntity<List<ContrainteDTO>> getByPoste(@PathVariable Long posteId) {
        log.info("Requête REST pour récupérer les contraintes liées au poste ID: {}", posteId);
        return ResponseEntity.ok(contrainteService.findAllByPoste(posteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContrainteDTO> getById(@PathVariable Long id) {
        log.info("Requête REST pour récupérer la contrainte ID: {}", id);
        return ResponseEntity.ok(contrainteService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")

    public ResponseEntity<ContrainteDTO> create(@Valid @RequestBody ContrainteDTO dto) {
        log.info("Requête REST pour créer une contrainte");
        return ResponseEntity.ok(contrainteService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")

    public ResponseEntity<ContrainteDTO> update(@PathVariable Long id, @Valid @RequestBody ContrainteDTO dto) {
        log.info("Requête REST pour modifier la contrainte ID: {}", id);
        return ResponseEntity.ok(contrainteService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Requête REST pour supprimer la contrainte ID: {}", id);
        contrainteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}