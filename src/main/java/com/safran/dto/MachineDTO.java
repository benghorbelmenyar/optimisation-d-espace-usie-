package com.safran.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MachineDTO {
    private Long id;
    private Long usineId;
    private Long zoneId;
    private String nom;
    private float longueur;
    private float largeur;
}