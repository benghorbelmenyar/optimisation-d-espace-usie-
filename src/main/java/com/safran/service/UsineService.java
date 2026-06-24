package com.safran.service;

import com.safran.dto.UsineDTO;
import com.safran.entity.Usine;
import com.safran.repository.PosteRepository;
import com.safran.repository.UsineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsineService {

    private final UsineRepository usineRepository;
    private final PosteRepository posteRepository;

    public List<UsineDTO> findAll() {
        return usineRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UsineDTO findById(Long id) {
        return toDTO(usineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usine non trouvée avec id : " + id)));
    }

    public UsineDTO create(UsineDTO dto) {
        Usine usine = toEntity(dto);
        usine.setDateCreation(LocalDate.now());
        return toDTO(usineRepository.save(usine));
    }

    public UsineDTO update(Long id, UsineDTO dto) {
        Usine usine = usineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usine non trouvée avec id : " + id));
        usine.setNom(dto.getNom());
        usine.setLongueur(dto.getLongueur());
        usine.setLargeur(dto.getLargeur());
        usine.setZoneSecurite(dto.getZoneSecurite());
        return toDTO(usineRepository.save(usine));
    }

    public void delete(Long id) {
        usineRepository.deleteById(id);
    }

    public float calculerSurfaceDisponible(Long usineId) {
        Usine usine = usineRepository.findById(usineId)
                .orElseThrow(() -> new RuntimeException("Usine non trouvée"));
        float surfaceTotale = usine.getLongueur() * usine.getLargeur();
        float surfaceZoneSecurite = surfaceTotale * (usine.getZoneSecurite() / 100f);
        float surfaceOccupee = posteRepository.findByUsineId(usineId).stream()
                .map(p -> p.getLongueur() * p.getLargeur())
                .reduce(0f, Float::sum);
        return surfaceTotale - surfaceZoneSecurite - surfaceOccupee;
    }

    private UsineDTO toDTO(Usine u) {
        return UsineDTO.builder()
                .id(u.getId()).nom(u.getNom()).longueur(u.getLongueur())
                .largeur(u.getLargeur()).zoneSecurite(u.getZoneSecurite())
                .dateCreation(u.getDateCreation())
                .surfaceDisponible(calculerSurfaceDisponible(u.getId()))
                .build();
    }

    private Usine toEntity(UsineDTO dto) {
        return Usine.builder()
                .nom(dto.getNom()).longueur(dto.getLongueur())
                .largeur(dto.getLargeur()).zoneSecurite(dto.getZoneSecurite())
                .build();
    }
}