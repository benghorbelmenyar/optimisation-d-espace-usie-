package com.safran.dto;

import com.safran.enums.typeP;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessusDTO {
    private Long id;
    private String nom;
    private float tempsUnitaire;
    private typeP typeP;
    private int quantite;
    private int nombreOperateurs;
    private float tauxCharge;
    private List<Long> programmeIds;

    // ⚙️ AJOUT : L'identifiant de la zone pour fermer la relation OneToOne
    private Long zoneId;
}