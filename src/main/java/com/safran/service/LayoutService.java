package com.safran.service;

import com.safran.dto.LayoutDTO;
import com.safran.entity.Layout;
import com.safran.entity.Poste;
import com.safran.repository.LayoutRepository;
import com.safran.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LayoutService {

    private final LayoutRepository layoutRepository;
    private final PosteRepository posteRepository;
    // private final PlacementAlgorithmService placementAlgorithmService; // à brancher Sprint 3/4

    public List<LayoutDTO> findAllByUsine(Long usineId) {
        return layoutRepository.findByUsineId(usineId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public LayoutDTO findById(Long id) {
        return toDTO(layoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Layout non trouvé avec id : " + id)));
    }

    /**
     * Génère le placement initial des postes (Couche 1 : MaxRects / Bin Packing).
     * Stub pour l'instant — sera implémenté en Sprint 3.
     */
    public LayoutDTO genererPlacement(Long usineId) {
        List<Poste> postes = posteRepository.findByUsineId(usineId);

        // TODO Sprint 3 : appeler l'algorithme MaxRects ici
        String donneesPlacementJson = "{\"postes\":" + postes.size() + ",\"placement\":\"a_implementer\"}";

        Layout layout = Layout.builder()
                .usineId(usineId)
                .dateGeneration(LocalDateTime.now())
                .donneesPlacement(donneesPlacementJson)
                .tauxOccupation(0f)
                .build();
        return toDTO(layoutRepository.save(layout));
    }

    /**
     * Optimise le layout existant via métaheuristique (Couche 2).
     * Stub pour l'instant — sera implémenté en Sprint 4.
     */
    public LayoutDTO optimiser(Long layoutId) {
        Layout layout = layoutRepository.findById(layoutId)
                .orElseThrow(() -> new RuntimeException("Layout non trouvé"));

        // TODO Sprint 4 : appeler la métaheuristique (recuit simulé / génétique / tabou)
        layout.setDateGeneration(LocalDateTime.now());
        return toDTO(layoutRepository.save(layout));
    }

    public void delete(Long id) {
        layoutRepository.deleteById(id);
    }

    private LayoutDTO toDTO(Layout l) {
        return LayoutDTO.builder()
                .id(l.getId()).usineId(l.getUsineId()).simulationId(l.getSimulationId())
                .dateGeneration(l.getDateGeneration()).donneesPlacement(l.getDonneesPlacement())
                .tauxOccupation(l.getTauxOccupation())
                .build();
    }
}