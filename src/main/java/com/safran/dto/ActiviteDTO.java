package com.safran.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActiviteDTO {
    private Long id;
    private Long usineId;
    private String nom;
    private String description;
    private LocalDate dateCreation;
}