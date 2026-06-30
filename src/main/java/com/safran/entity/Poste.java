package com.safran.entity;

import com.safran.enums.StatutCouleur;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "poste")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Poste {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id", nullable = false)
    private Usine usine;

    @Column(nullable = false)
    private String nom;

    private float longueur;
    private float largeur;

    @Column(name = "cycle_time")
    private float cycleTime;

    @Column(name = "nombre_operateurs")
    private int nombreOperateurs;

    private int quantite;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_couleur")
    private StatutCouleur statutCouleur;

    // 🔄 NOUVELLE RELATION : Un poste est lié à un Programme
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id", nullable = false) // Mettre nullable = false si un poste doit obligatoirement appartenir à un programme
    private Programme programme;
    // Dans Poste.java
    @Column(name = "nombre_shifts", nullable = false)
    @Builder.Default
    private int nombreShifts = 1; // 👈 1 = standard, 2 = double shift, 3 = triple shift
}