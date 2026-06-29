package com.safran.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "machine")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Machine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🏭 La machine appartient toujours obligatoirement à une Usine
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id", nullable = false)
    private Usine usine;

    // 🗺️ Le lien devient optionnel (nullable = true) pour permettre la flexibilité
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = true)
    private Zone zone;

    @Column(nullable = false)
    private String nom;

    private float longueur;
    private float largeur;
}