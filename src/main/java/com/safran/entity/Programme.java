package com.safran.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "programme")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Programme {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Vraie relation avec l'entité Activite (Clé Étrangère)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    @ManyToMany(mappedBy = "programmes", fetch = FetchType.LAZY)
    private List<Processus> processus;
    // ➕ À AJOUTER dans Programme.java
    @OneToMany(mappedBy = "programme", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Poste> postes = new ArrayList<>();
}