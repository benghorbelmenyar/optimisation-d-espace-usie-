package com.safran.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BesoinCommandeDTO {
    private Long processusId;
    private float heuresDemandees;
}