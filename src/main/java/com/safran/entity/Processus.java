package com.safran.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "processus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Processus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "charge_annuelle", nullable = false)
    private float chargeAnnuelle; // 💡 Remplace tempsUnitaire (Stocke la charge globale en heures, ex: 108.0)

    @Enumerated(EnumType.STRING)
    @Column(name = "type_p", nullable = false)
    private typeP typeP;

    @Column(name = "nombre_operateurs")
    private int nombreOperateurs;

    @Column(name = "taux_charge")
    private float tauxCharge;

    @Column(name = "annee_charge", nullable = false)
    private int anneeCharge; // 💡 Année cible de la planification capacitaire (ex: 2026)

    @Column(name = "date_ajout_charge", nullable = false)
    private LocalDateTime dateAjoutCharge; // 💡 Horodatage de l'enregistrement ou de la modification

    // ⚙️ Relation Un-à-Un (Une zone dédiée accueille un seul processus majeur)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", unique = true) // unique = true empêche qu'une autre zone s'approprie le même processus
    private Zone zone;

    // 🔗 Relation ManyToMany avec Programme (Côté maître pour la table de jointure)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "programme_processus",
            joinColumns = @JoinColumn(name = "processus_id"),
            inverseJoinColumns = @JoinColumn(name = "programme_id")
    )
    @Builder.Default
    private List<Programme> programmes = new ArrayList<>();
}