package com.safran.entity;

import com.safran.enums.StatutCommande;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commande")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Commande {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id", nullable = false)
    private Usine usine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id", nullable = false) // Devient une clé étrangère
    private Programme programmeAvion;

    private String client;

    @Column(name = "date_commande")
    private LocalDate dateCommande;

    @Column(name = "date_livraison_souhaitee")
    private LocalDate dateLivraisonSouhaitee;

    @Enumerated(EnumType.STRING)
    private StatutCommande statut;

    // 📊 Une commande contient désormais plusieurs lignes de besoins (Soudure, Cintrage...)
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BesoinCommande> besoins = new ArrayList<>();
}