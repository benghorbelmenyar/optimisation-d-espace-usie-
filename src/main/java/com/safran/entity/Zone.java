package com.safran.entity;

import lombok.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "zone")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private Float longueur;

    @Column(nullable = false)
    private Float largeur;

    @Column(name = "surface_totale")
    private Float surfaceTotale;

    @Column(name = "surface_requise_par_poste")
    private Float surfaceRequiseParPoste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id")
    private Usine usine;

    @OneToOne(mappedBy = "zone", fetch = FetchType.LAZY)
    private Processus processus;

    // 💡 CORRECTION : Ajout de CascadeType.ALL et orphanRemoval pour sauvegarder automatiquement les machines imbriquées
    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Machine> machines = new ArrayList<>();
}