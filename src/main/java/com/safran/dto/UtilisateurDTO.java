package com.safran.dto;

import com.safran.entity.RoleUtilisateur;
import lombok.*;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UtilisateurDTO {
    private Long id;

    @NotBlank
    private String nom;

    @NotBlank
    private String email;

    // 🔑 C'est ce champ que l'utilisateur va remplir dans Swagger !
    private String motDePasse;

    private RoleUtilisateur role;
    private LocalDate dateCreation;
}