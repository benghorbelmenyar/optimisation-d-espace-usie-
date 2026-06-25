package com.safran.controller;

import com.safran.dto.CommandeDTO;
import com.safran.service.CommandeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
@Slf4j
public class CommandeController {

    private final CommandeService commandeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION') or hasRole('RESPONSABLE_METHODE')")
    public ResponseEntity<List<CommandeDTO>> getAll() {
        log.info("Requête REST pour récupérer toutes les commandes");
        return ResponseEntity.ok(commandeService.findAll());
    }

    @GetMapping("/usine/{usineId}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION') or hasRole('RESPONSABLE_METHODE')")
    public ResponseEntity<List<CommandeDTO>> getByUsine(@PathVariable Long usineId) {
        log.info("Requête REST pour récupérer les commandes de l'usine ID: {}", usineId);
        return ResponseEntity.ok(commandeService.findAllByUsine(usineId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION') or hasRole('RESPONSABLE_METHODE')")
    public ResponseEntity<CommandeDTO> getById(@PathVariable Long id) {
        log.info("Requête REST pour récupérer la commande ID: {}", id);
        return ResponseEntity.ok(commandeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")
    public ResponseEntity<?> create(@Valid @RequestBody CommandeDTO dto) {
        log.info("Requête REST pour enregistrer une commande du client: {}", dto.getClient());
        try {
            CommandeDTO result = commandeService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la création de la commande : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CommandeDTO dto) {
        log.info("Requête REST pour modifier la commande ID: {}", id);
        try {
            CommandeDTO result = commandeService.update(id, dto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la modification de la commande ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Requête REST pour supprimer la commande ID: {}", id);
        commandeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}