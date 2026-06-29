package com.safran.algorithm;

import com.safran.entity.Zone;
import com.safran.entity.Processus;

public interface SpaceOptimizationStrategy {
    /**
     * Calcule l'optimisation et l'éligibilité d'un processus dans une zone.
     */
    OptimizationResult optimiser(Zone zone, Processus processus);
}