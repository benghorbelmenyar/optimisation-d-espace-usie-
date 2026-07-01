package com.safran.dto;

import com.safran.entity.StatutCouleur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosteDTO {
    private Long id;
    private Long usineId;
    private Long programmeId;
    private String nom;
    private Float longueur;
    private Float largeur;
    private Integer quantite;
    private StatutCouleur statutCouleur;
    private Integer nombreShifts;

    // 💡 AJOUTER CE CHAMP S'IL EST MANQUANT OU CORRIGER L'ORTHOGRAPHE
    private Integer nombreOperateurs;
}