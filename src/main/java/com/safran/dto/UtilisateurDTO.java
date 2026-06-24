package com.safran.dto;

import com.safran.enums.RoleUtilisateur;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UtilisateurDTO {
    private Long id;
    private String nom;
    private String email;
    private String motDePasse; // en clair, uniquement pour la création/login — jamais renvoyé en sortie
    private RoleUtilisateur role;
    private LocalDate dateCreation;
}