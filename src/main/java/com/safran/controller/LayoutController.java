package com.safran.controller;

import com.safran.dto.LayoutDTO;
import com.safran.service.LayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/layouts")
@RequiredArgsConstructor
public class LayoutController {

    private final LayoutService layoutService;

    @GetMapping("/usine/{usineId}")
    public ResponseEntity<List<LayoutDTO>> getByUsine(@PathVariable Long usineId) {
        return ResponseEntity.ok(layoutService.findAllByUsine(usineId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LayoutDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(layoutService.findById(id));
    }

    @PostMapping("/usine/{usineId}/generer")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<LayoutDTO> genererPlacement(@PathVariable Long usineId) {
        return ResponseEntity.ok(layoutService.genererPlacement(usineId));
    }

    @PostMapping("/{id}/optimiser")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<LayoutDTO> optimiser(@PathVariable Long id) {
        return ResponseEntity.ok(layoutService.optimiser(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        layoutService.delete(id);
        return ResponseEntity.noContent().build();
    }
}