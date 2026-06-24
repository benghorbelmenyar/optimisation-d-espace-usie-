package com.safran.controller;

import com.safran.dto.ZoneDTO;
import com.safran.service.ZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 👈 Import pour les logs
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@Slf4j // 👈 Active la variable automatique "log"
public class ZoneController {

    private final ZoneService zoneService;

    // ✨ NOUVEAU : Récupérer toutes les zones
    @GetMapping
    public ResponseEntity<List<ZoneDTO>> getAll() {
        log.info("Requête REST pour récupérer toutes les zones disponibles");
        List<ZoneDTO> zones = zoneService.findAll();
        log.info("Retour de {} zones trouvées", zones.size());
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/usine/{usineId}")
    public ResponseEntity<List<ZoneDTO>> getByUsine(@PathVariable Long usineId) {
        log.info("Requête REST pour récupérer les zones de l'usine ID: {}", usineId);
        return ResponseEntity.ok(zoneService.findAllByUsine(usineId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneDTO> getById(@PathVariable Long id) {
        log.info("Requête REST pour récupérer la zone ID: {}", id);
        return ResponseEntity.ok(zoneService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ZoneDTO> create(@Valid @RequestBody ZoneDTO dto) {
        log.info("Requête REST pour créer une nouvelle zone : {}", dto.getNom());
        return ResponseEntity.ok(zoneService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneDTO> update(@PathVariable Long id, @Valid @RequestBody ZoneDTO dto) {
        log.info("Requête REST pour modifier la zone ID: {}", id);
        return ResponseEntity.ok(zoneService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Requête REST pour supprimer la zone ID: {}", id);
        zoneService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/surface-disponible")
    public ResponseEntity<Float> getSurfaceDisponible(@PathVariable Long id) {
        log.info("Requête REST pour calculer la surface disponible de la zone ID: {}", id);
        return ResponseEntity.ok(zoneService.calculerSurfaceDisponible(id));
    }
}