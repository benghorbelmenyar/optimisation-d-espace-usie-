package com.safran.dto;

import com.safran.enums.StatutCommande;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommandeDTO {
    private Long id;
    private Long usineId;
    private StatutCommande statut;
    private String client;
    private int quantiteDemandee;
    private LocalDate dateCommande;
    private LocalDate dateLivraisonSouhaitee;
}