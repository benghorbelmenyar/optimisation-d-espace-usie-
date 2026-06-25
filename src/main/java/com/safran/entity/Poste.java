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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processus_id")
    private Processus processus;
}