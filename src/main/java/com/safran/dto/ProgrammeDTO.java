package com.safran.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProgrammeDTO {
    private Long id;
    private Long activiteId;
    private String nom;
    private String description;
    private LocalDate dateCreation;
    private String processus;
}