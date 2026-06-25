package com.safran.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List; // 👈 Ajoute cet import

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProgrammeDTO {
    private Long id;
    private Long activiteId;
    private String nom;
    private String description;
    private LocalDate dateCreation;

    // 🔄 MODIFICATION : Remplacement du type String par List<Long>
    private List<Long> processusIds;
}