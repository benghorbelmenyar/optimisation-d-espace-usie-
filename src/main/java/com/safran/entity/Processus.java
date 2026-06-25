package com.safran.entity;

import com.safran.enums.typeP;
import lombok.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "processus")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Processus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "temps_unitaire")
    private float tempsUnitaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_p", nullable = false)
    private typeP typeP;

    private int quantite;

    @Column(name = "nombre_operateurs")
    private int nombreOperateurs;

    @Column(name = "taux_charge")
    private float tauxCharge;

    // 🔗 Relation ManyToMany avec Programme (Côté maître pour la table de jointure)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "programme_processus",
            joinColumns = @JoinColumn(name = "processus_id"),
            inverseJoinColumns = @JoinColumn(name = "programme_id")
    )
    @Builder.Default
    private List<Programme> programmes = new ArrayList<>();

    // 🔗 Relation OneToMany avec Poste (Un processus pour plusieurs postes)
    @OneToMany(mappedBy = "processus", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Poste> postes = new ArrayList<>();
}