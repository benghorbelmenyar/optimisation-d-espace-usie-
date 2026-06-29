package com.safran.algorithm;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptimizationResult {
    private boolean eligible;
    private float surfaceTotaleZone;       // Surface totale dispo au sol
    private float surfaceOccupeeMachines;   // Espace pris par les machines fixes
    private float surfaceRequiseNouveauxPostes; // Espace nécessaire pour les nouveaux opérateurs
    private float surfaceLibreRestante;    // Espace encore disponible après simulation
    private float tauxOccupationPourcent;
    private String message;
}