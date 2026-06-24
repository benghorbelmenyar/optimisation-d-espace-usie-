package com.safran.controller;

import com.safran.dto.RapportPDFDTO;
import com.safran.service.RapportPDFService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
@Slf4j
public class RapportPDFController {

    private final RapportPDFService rapportPDFService;

    @GetMapping("/simulation/{simulationId}")    public ResponseEntity<List<RapportPDFDTO>> getBySimulation(@PathVariable Long simulationId) {
        log.info("Requête REST pour lister les rapports de la simulation ID: {}", simulationId);
        return ResponseEntity.ok(rapportPDFService.findAllBySimulation(simulationId));
    }

    @PostMapping("/simulation/{simulationId}/generer")
    public ResponseEntity<?> generer(@PathVariable Long simulationId) {
        log.info("Requête REST pour générer le rapport de la simulation ID: {}", simulationId);
        try {
            RapportPDFDTO result = rapportPDFService.generer(simulationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.error("Échec de la génération du rapport : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/telecharger")
    public ResponseEntity<Resource> telecharger(@PathVariable Long id) {
        log.info("Requête REST pour télécharger le rapport ID: {}", id);
        Resource resource = rapportPDFService.telecharger(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}