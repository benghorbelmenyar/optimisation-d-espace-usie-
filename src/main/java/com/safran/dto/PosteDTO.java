package com.safran.dto;

import com.safran.enums.StatutCouleur;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PosteDTO {
    private Long id;
    private Long usineId;
    private String nom;
    private float longueur;
    private float largeur;
    private float cycleTime;
    private int nombreOperateurs;
    private int quantite;
    private StatutCouleur statutCouleur;
    private double capacite;
}