package com.safran.controller;

import com.safran.dto.SimulationDTO;
import com.safran.service.SimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulations")
@RequiredArgsConstructor
@Slf4j
public class SimulationController {

    private final SimulationService simulationService;

    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<List<SimulationDTO>> getByCommande(@PathVariable Long commandeId) {
        log.info("Requête REST pour récupérer les simulations de la commande ID: {}", commandeId);
        return ResponseEntity.ok(simulationService.findAllByCommande(commandeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimulationDTO> getById(@PathVariable Long id) {
        log.info("Requête REST pour récupérer la simulation ID: {}", id);
        return ResponseEntity.ok(simulationService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")

    public ResponseEntity<?> lancer(@RequestParam Long commandeId, @RequestParam Long utilisateurId) {
        log.info("Requête REST pour lancer une simulation");
        try {
            SimulationDTO result = simulationService.lancer(commandeId, utilisateurId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors du lancement de la simulation : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Requête REST pour supprimer la simulation ID: {}", id);
        simulationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}