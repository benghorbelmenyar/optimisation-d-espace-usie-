package com.safran.controller;

import com.safran.dto.PosteDTO;
import com.safran.service.PosteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 👈 Pour les logs
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/postes")
@RequiredArgsConstructor
@Slf4j
public class PosteController {

    private final PosteService posteService;

    // ✨ NOUVEAU : Récupérer tous les postes de l'application
    @GetMapping
    public ResponseEntity<List<PosteDTO>> getAll() {
        log.info("Requête REST pour récupérer tous les postes de l'application");
        List<PosteDTO> postes = posteService.findAll();
        log.info("Retour de {} postes trouvés", postes.size());
        return ResponseEntity.ok(postes);
    }

    @GetMapping("/usine/{usineId}")
    public ResponseEntity<List<PosteDTO>> getByUsine(@PathVariable Long usineId) {
        log.info("Requête REST pour récupérer les postes de l'usine ID: {}", usineId);
        return ResponseEntity.ok(posteService.findAllByUsine(usineId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PosteDTO> getById(@PathVariable Long id) {
        log.info("Requête REST pour récupérer le poste ID: {}", id);
        return ResponseEntity.ok(posteService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<PosteDTO> create(@Valid @RequestBody PosteDTO dto) {
        log.info("Requête REST pour créer un poste : {}", dto.getNom());
        return ResponseEntity.ok(posteService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<PosteDTO> update(@PathVariable Long id, @Valid @RequestBody PosteDTO dto) {
        log.info("Requête REST pour modifier le poste ID: {}", id);
        return ResponseEntity.ok(posteService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Requête REST pour supprimer le poste ID: {}", id);
        posteService.delete(id);
        return ResponseEntity.noContent().build();
    }


}