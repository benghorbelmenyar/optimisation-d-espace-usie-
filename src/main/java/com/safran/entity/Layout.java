package com.safran.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "layout")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Layout {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usine_id", nullable = false)
    private Long usineId;

    @Column(name = "simulation_id")
    private Long simulationId;

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    @Lob
    @Column(name = "donnees_placement")
    private String donneesPlacement; // JSON sérialisé des coordonnées des postes

    @Column(name = "taux_occupation")
    private float tauxOccupation;
}