package com.safran.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UsineDTO {
    private Long id;
    private String nom;
    private float longueur;
    private float largeur;
    private float zoneSecurite;
    private LocalDate dateCreation;
    private float surfaceDisponible;
}