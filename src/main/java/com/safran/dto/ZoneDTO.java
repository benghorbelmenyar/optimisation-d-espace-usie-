package com.safran.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ZoneDTO {
    private Long id;
    private Long usineId;
    private String nom;
    private float longueur;
    private float largeur;
    private float surfaceDisponible;
}