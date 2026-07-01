package com.safran.dto;

import com.safran.entity.StatutCommande;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommandeDTO {
    private Long id;
    private Long usineId;
    private String programmeAvion; // 👈 "A320"
    private String client;
    private LocalDate dateCommande;
    private LocalDate dateLivraisonSouhaitee;
    private StatutCommande statut;

    private List<BesoinCommandeDTO> besoins; // 👈 La liste des couples (Atelier / Heures)
}