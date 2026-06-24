package com.safran.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "activite")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Activite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id", nullable = false)
    private Usine usine;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Column(name = "date_creation")
    private LocalDate dateCreation;
}