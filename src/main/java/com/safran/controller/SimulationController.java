package com.safran.controller;

import com.safran.dto.SimulationDTO;
import com.safran.enums.UniteTemps;
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

    /**
     * Lance une simulation sur mesure pour le portefeuille global d'une usine.
     */
    @PostMapping("/lancer")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")
    public ResponseEntity<List<SimulationDTO>> lancerSimulation(
            @RequestParam Long usineId,
            @RequestParam Long utilisateurId,
            @RequestParam int duree,
            @RequestParam UniteTemps uniteTemps,
            @RequestParam Long processusId) { // <-- Ajoute ce paramètre ici !

        log.info("Requête REST pour simuler le processus {} de l'usine {}", processusId, usineId);
        List<SimulationDTO> result = simulationService.lancerSimulationDynamiqueParProcessus(usineId, utilisateurId, duree, uniteTemps, processusId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Récupère l'historique des simulations d'une usine.
     */
    @GetMapping("/usine/{usineId}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION') or hasRole('RESPONSABLE_METHODE')")
    public ResponseEntity<List<SimulationDTO>> getByUsine(@PathVariable Long usineId) {
        return ResponseEntity.ok(simulationService.findAllByUsine(usineId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION') or hasRole('RESPONSABLE_METHODE')")
    public ResponseEntity<SimulationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(simulationService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        simulationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}