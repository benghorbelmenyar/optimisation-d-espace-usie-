package com.safran.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "machine")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Machine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Vraie relation avec la table Usine (Clé Étrangère)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id", nullable = false)
    private Usine usine;

    // 🔗 Vraie relation avec la table Zone (Clé Étrangère)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false)
    private String nom;

    private float longueur;
    private float largeur;
}