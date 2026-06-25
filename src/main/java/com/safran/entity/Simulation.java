package com.safran.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id", nullable = false)
    private Usine usine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processus_id", nullable = false)
    private Processus processus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "date_simulation")
    private LocalDateTime dateSimulation;

    @Column(name = "heures_demandees")
    private float heuresDemandees;

    @Column(name = "heures_disponibles_actuelles")
    private float heuresDisponiblesActuelles;

    @Column(name = "operateurs_actuels")
    private int operateursActuels;

    // 👇 CES TROIS LIGNES VONT CORRIGER TON ERREUR 500 👇
    @Column(name = "operateurs_a_ajouter")
    private int operateursAAjouter;

    @Column(name = "operateurs_a_retirer")
    private int operateursARetirer;

    @Column(name = "taux_charge_processus")
    private float tauxChargeProcessus;

    private boolean faisabilite;

    @Column(name = "solution_proposee", length = 500)
    private String solutionProposee;
}