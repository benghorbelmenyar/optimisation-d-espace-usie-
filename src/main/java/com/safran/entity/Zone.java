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

    // 💡 Remplacement de float par Float pour tolérer le NULL
    @Column(nullable = false)
    private Float longueur;

    @Column(nullable = false)
    private Float largeur;

    @Column(name = "surface_totale")
    private Float surfaceTotale; // 👈 Accepte désormais les NULL sans crasher

    @Column(name = "surface_requise_par_poste")
    private Float surfaceRequiseParPoste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id")
    private Usine usine;

    @OneToOne(mappedBy = "zone", fetch = FetchType.LAZY)
    private Processus processus;

    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Machine> machines = new ArrayList<>();
}