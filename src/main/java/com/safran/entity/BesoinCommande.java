package com.safran.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "besoin_commande")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BesoinCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processus_id", nullable = false)
    private Processus processus;

    @Column(name = "heures_demandees", nullable = false)
    private float heuresDemandees;
}