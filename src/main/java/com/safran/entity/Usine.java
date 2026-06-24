package com.safran.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "usine")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private float longueur;
    private float largeur;

    @Column(name = "zone_securite")
    private float zoneSecurite;

    @Column(name = "date_creation")
    private LocalDate dateCreation;
}