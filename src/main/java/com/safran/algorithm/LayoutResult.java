package com.safran.algorithm;

import lombok.*;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class LayoutResult {
    private boolean placementReussi;
    private List<PlacedElement> elementsPlaces; // Liste de tout ce qui est positionné (x, y)
    private String messageErreur;
}