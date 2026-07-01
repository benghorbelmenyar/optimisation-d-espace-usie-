package com.safran.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "postes")
@Data // 💡 Génère automatiquement les getters, setters, toString, etc.
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Poste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private Float longueur;
    private Float largeur;
    private Integer quantite;

    @Enumerated(EnumType.STRING)
    private StatutCouleur statutCouleur;

    private Integer nombreShifts;

    // 💡 AJOUTER CE CHAMP DANS L'ENTITÉ
    private Integer nombreOperateurs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id")
    private Usine usine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id")
    private Programme programme;
}