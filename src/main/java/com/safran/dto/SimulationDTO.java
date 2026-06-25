package com.safran.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class SimulationDTO {
    private Long id;
    private Long usineId;
    private Long processusId;
    private String processusNom;
    private Long utilisateurId;
    private LocalDateTime dateSimulation;
    private float heuresDemandees;
    private float heuresDisponiblesActuelles;
    private int operateursActuels;
    private int operateursAAjouter;
    private int operateursARetirer;
    private float tauxChargeProcessus;
    private boolean faisabilite;
    private String solutionProposee;
}