package com.safran.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SimulationDTO {
    private Long id;
    private Long commandeId;
    private Long utilisateurId;
    private LocalDateTime dateSimulation;
    private float capaciteCalculee;
    private boolean faisabilite;
    private String posteGoulot;
    private String solutionProposee;
}