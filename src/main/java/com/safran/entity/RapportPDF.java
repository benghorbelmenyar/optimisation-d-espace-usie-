package com.safran.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rapport_pdf")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RapportPDF {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Vraie relation avec l'entité Simulation (Clé Étrangère)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    @Column(name = "chemin_fichier")
    private String cheminFichier;
}