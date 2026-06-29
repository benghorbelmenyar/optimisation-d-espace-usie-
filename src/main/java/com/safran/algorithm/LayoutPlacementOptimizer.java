package com.safran.algorithm;

import com.safran.entity.Zone;
import com.safran.entity.Machine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("layoutOptimizer")
public class LayoutPlacementOptimizer {

    // Distance de sécurité pour les couloirs/accessibilité (ex: 1.5 mètres)
    private static final float ESPACEMENT_COULOIR = 1.5f;

    public LayoutResult calculerLayout(Zone zone, int nbPostesAAjouter, float largPoste, float longPoste) {
        List<PlacedElement> planFinal = new ArrayList<>();

        // 1. Positionner d'abord les machines fixes existantes (Obstacles)
        if (zone.getMachines() != null) {
            for (Machine m : zone.getMachines()) {
                // Si tes machines ont déjà des coordonnées en base, on les reprend.
                // Sinon, on simule une position initiale fixe pour le test.
                planFinal.add(PlacedElement.builder()
                        .nom(m.getNom())
                        .type("MACHINE")
                        .x(2.0f) // Exemple arbitraire si non configuré
                        .y(2.0f)
                        .largeur(m.getLargeur())
                        .longueur(m.getLongueur())
                        .build());
            }
        }

        // 2. Placer les nouveaux postes de travail un par un
        for (int i = 1; i <= nbPostesAAjouter; i++) {
            boolean postePlace = false;

            // Algorithme de balayage spatial pas à pas (ici par pas de 0.5m)
            for (float currentY = 0; currentY <= zone.getLargeur() - largPoste; currentY += 0.5f) {
                for (float currentX = 0; currentX <= zone.getLongueur() - longPoste; currentX += 0.5f) {

                    // Vérifier si cette coordonnée (currentX, currentY) chevauche une machine ou un couloir
                    if (estEmplacementLibre(currentX, currentY, longPoste, largPoste, planFinal, zone)) {

                        // Emplacement valide trouvé ! On l'enregistre
                        planFinal.add(PlacedElement.builder()
                                .nom("Nouveau Poste " + i)
                                .type("POSTE_AJOUTE")
                                .x(currentX)
                                .y(currentY)
                                .largeur(largPoste)
                                .longueur(longPoste)
                                .build());

                        postePlace = true;
                        break; // On passe au poste suivant
                    }
                }
                if (postePlace) break;
            }

            // Si un des postes ne trouve aucune place libre dans la surface totale
            if (!postePlace) {
                return LayoutResult.builder()
                        .placementReussi(false)
                        .messageErreur("Saturation spatiale géométrique : Impossible de positionner le Poste " + i + " en respectant les couloirs d'accès.")
                        .elementsPlaces(planFinal)
                        .build();
            }
        }

        return LayoutResult.builder()
                .placementReussi(true)
                .elementsPlaces(planFinal)
                .build();
    }

    private boolean estEmplacementLibre(float x, float y, float len, float width, List<PlacedElement> existants, Zone zone) {
        // A. Vérifier que ça ne dépasse pas les murs de la zone
        if ((x + len) > zone.getLongueur() || (y + width) > zone.getLargeur()) {
            return false;
        }

        // B. Vérifier la collision avec chaque élément déjà placé + contrainte de couloir
        for (PlacedElement el : existants) {
            // Zone d'exclusion incluant la contrainte de couloir (Espace d'accessibilité)
            float marge = (el.getType().equals("MACHINE")) ? ESPACEMENT_COULOIR : 0.5f;

            boolean overlapX = (x < el.getX() + el.getLongueur() + marge) && (x + len + marge > el.getX());
            boolean overlapY = (y < el.getY() + el.getLargeur() + marge) && (y + width + marge > el.getY());

            if (overlapX && overlapY) {
                return false; // Collision détectée !
            }
        }
        return true; // L'espace est sain
    }
}