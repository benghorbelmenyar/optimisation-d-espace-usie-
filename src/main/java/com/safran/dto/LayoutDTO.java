package com.safran.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LayoutDTO {
    private Long id;
    private Long usineId;
    private Long simulationId;
    private LocalDateTime dateGeneration;
    private String donneesPlacement;
    private float tauxOccupation;
}