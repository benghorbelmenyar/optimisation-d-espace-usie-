package com.safran.algorithm;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class PlacedElement {
    private String nom;
    private String type; // "MACHINE" ou "POSTE_AJOUTE" ou "COULOIR"
    private float x;      // Coordonnée X (abscisse coin haut-gauche)
    private float y;      // Coordonnée Y (ordonnée coin haut-gauche)
    private float largeur;
    private float longueur;
}