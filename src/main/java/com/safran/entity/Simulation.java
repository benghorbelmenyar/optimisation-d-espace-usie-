package com.safran.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Simulation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Vraie relation avec l'entité Commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    // 🔗 Vraie relation avec l'entité Utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "date_simulation")
    private LocalDateTime dateSimulation;

    @Column(name = "capacite_calculee")
    private float capaciteCalculee;

    private boolean faisabilite;

    @Column(name = "poste_goulot")
    private String posteGoulot;

    @Lob
    @Column(name = "solution_proposee")
    private String solutionProposee;
}