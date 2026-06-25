package com.safran.controller;

import com.safran.dto.UsineDTO;
import com.safran.service.UsineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/usines")
@RequiredArgsConstructor
public class UsineController {

    private final UsineService usineService;

    @GetMapping
    public ResponseEntity<List<UsineDTO>> getAll() {
        return ResponseEntity.ok(usineService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsineDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(usineService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<UsineDTO> create(@Valid @RequestBody UsineDTO dto) {
        return ResponseEntity.ok(usineService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<UsineDTO> update(@PathVariable Long id, @Valid @RequestBody UsineDTO dto) {
        return ResponseEntity.ok(usineService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE_PRODUCTION')")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usineService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/surface-disponible")
    public ResponseEntity<Float> getSurfaceDisponible(@PathVariable Long id) {
        return ResponseEntity.ok(usineService.calculerSurfaceDisponible(id));
    }
}