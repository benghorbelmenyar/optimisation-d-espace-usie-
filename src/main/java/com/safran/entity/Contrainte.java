package com.safran.entity;

import com.safran.enums.TypeContrainte;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "contrainte")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contrainte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Clé étrangère vers le poste source
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_source_id", nullable = false)
    private Poste posteSource;

    // 🔗 Clé étrangère vers le poste cible
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_cible_id", nullable = false)
    private Poste posteCible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeContrainte type;

    private float valeur;
}