package com.safran.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RapportPDFDTO {
    private Long id;
    private Long simulationId;
    private LocalDateTime dateGeneration;
    private String cheminFichier;
}