package com.safran.algorithm;

import com.safran.entity.Zone;
import com.safran.entity.Processus;
import com.safran.entity.Machine;
import org.springframework.stereotype.Component;

@Component("greedyOptimizer")
public class GreedySpaceOptimizer implements SpaceOptimizationStrategy {

    @Override
    public OptimizationResult optimiser(Zone zone, Processus processus) {
        float surfaceTotaleZone = zone.getLongueur() * zone.getLargeur();

        if (surfaceTotaleZone <= 0) {
            return OptimizationResult.builder()
                    .eligible(false)
                    .message("Erreur : La zone possède une surface invalide.")
                    .build();
        }

        // 1. Calcul de la saturation par les machines fixes
        float surfaceMachines = 0f;
        if (zone.getMachines() != null) {
            for (Machine machine : zone.getMachines()) {
                surfaceMachines += (machine.getLongueur() * machine.getLargeur());
            }
        }

        // 2. Calcul de la saturation par les postes de travail requis
        float surfaceParPoste = zone.getSurfaceRequiseParPoste();
        float surfaceRequiseProcessus = processus.getNombreOperateurs() * surfaceParPoste;

        // 3. Bilans globaux
        float surfaceOccupeeTotale = surfaceMachines + surfaceRequiseProcessus;
        float surfaceLibre = surfaceTotaleZone - surfaceOccupeeTotale;
        float tauxOccupation = (surfaceOccupeeTotale / surfaceTotaleZone) * 100f;

        boolean eligible = surfaceOccupeeTotale <= surfaceTotaleZone;

        String message = eligible
                ? String.format("Espace suffisant. Taux d'occupation : %.1f%%", tauxOccupation)
                : String.format("SATURATION PHYSIQUE : Manque %.2f m² dans l'atelier.", Math.abs(surfaceLibre));

        return OptimizationResult.builder()
                .eligible(eligible)
                .surfaceTotaleZone(surfaceTotaleZone)
                .surfaceOccupeeMachines(surfaceMachines)
                .surfaceRequiseNouveauxPostes(surfaceRequiseProcessus)
                .surfaceLibreRestante(Math.max(0, surfaceLibre))
                .tauxOccupationPourcent(tauxOccupation)
                .message(message)
                .build();
    }
}