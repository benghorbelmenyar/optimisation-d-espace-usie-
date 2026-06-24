package com.safran.controller;

import com.safran.dto.ActiviteDTO;
import com.safran.dto.ProgrammeDTO;
import com.safran.service.ActiviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 👈 Import pour les logs
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/activites")
@RequiredArgsConstructor
@Slf4j // 👈 Activation des logs
public class ActiviteController {

    private final ActiviteService activiteService;

    // ✨ NOUVEAU : Récupérer toutes les activités
    @GetMapping
    public ResponseEntity<List<ActiviteDTO>> getAll() {
        log.info("Requête REST pour récupérer toutes les activités");
        List<ActiviteDTO> activites = activiteService.findAll();
        log.info("Retour de {} activités trouvées", activites.size());
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/usine/{usineId}")
    public ResponseEntity<List<ActiviteDTO>> getByUsine(@PathVariable Long usineId) {
        log.info("Requête REST pour récupérer les activités de l'usine ID: {}", usineId);
        return ResponseEntity.ok(activiteService.findAllByUsine(usineId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActiviteDTO> getById(@PathVariable Long id) {
        log.info("Requête REST pour récupérer l'activité ID: {}", id);
        return ResponseEntity.ok(activiteService.findById(id));
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")

    public ResponseEntity<ActiviteDTO> create(@Valid @RequestBody ActiviteDTO dto) {
        log.info("Requête REST pour créer une nouvelle activité : {}", dto.getNom());
        return ResponseEntity.ok(activiteService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")

    public ResponseEntity<ActiviteDTO> update(@PathVariable Long id, @Valid @RequestBody ActiviteDTO dto) {
        log.info("Requête REST pour modifier l'activité ID: {}", id);
        return ResponseEntity.ok(activiteService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Requête REST pour supprimer l'activité ID: {}", id);
        activiteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/programmes")
    public ResponseEntity<List<ProgrammeDTO>> getProgrammes(@PathVariable Long id) {
        log.info("Requête REST pour lister les programmes de l'activité ID: {}", id);
        return ResponseEntity.ok(activiteService.listerProgrammes(id));
    }
    // ✨ NOUVEAU : Récupérer toutes les activités de l'application

}