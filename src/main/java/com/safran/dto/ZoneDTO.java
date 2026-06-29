package com.safran.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ZoneDTO {
    private Long id;
    private Long usineId;
    private String nom;
    private float longueur;
    private float largeur;
    private float surfaceDisponible;
    private float surfaceRequiseParPoste;

    // 💡 Intégration de ton MachineDTO existant
    private List<MachineDTO> machines;
}