package com.safran.dto;

import com.safran.entity.TypeContrainte;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContrainteDTO {
    private Long id;
    private Long posteSourceId;
    private Long posteCibleId;
    private TypeContrainte type;
    private float valeur;
}