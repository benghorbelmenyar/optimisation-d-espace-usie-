package com.safran.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "zone")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 On remplace le "Long usineId" par la vraie relation vers l'entité Usine
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id", nullable = false)
    private Usine usine;

    @Column(nullable = false)
    private String nom;

    private float longueur;
    private float largeur;
}