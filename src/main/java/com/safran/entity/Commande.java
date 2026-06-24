package com.safran.entity;

import com.safran.enums.StatutCommande;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "commande")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Commande {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usine_id", nullable = false)
    private Usine usine;


    @Enumerated(EnumType.STRING)
    private StatutCommande statut;

    private String client;

    @Column(name = "quantite_demandee")
    private int quantiteDemandee;

    @Column(name = "date_commande")
    private LocalDate dateCommande;

    @Column(name = "date_livraison_souhaitee")
    private LocalDate dateLivraisonSouhaitee;
}