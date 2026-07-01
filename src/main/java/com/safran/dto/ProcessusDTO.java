package com.safran.dto;

import com.safran.entity.typeP;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessusDTO {
    private Long id;
    private String nom;
    private float chargeAnnuelle; // 💡 Ajouté pour remplacer tempsUnitaire
    private typeP typeP;
    private int nombreOperateurs;
    private float tauxCharge;
    private int anneeCharge; // 💡 Ajouté pour l'historisation
    private LocalDateTime dateAjoutCharge; // 💡 Ajouté pour l'historisation
    private Long zoneId;
    private List<Long> programmeIds;
}