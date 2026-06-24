package com.safran.repository;

import com.safran.entity.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    // méthode custom (si besoin)
    List<Commande> findByUsineId(Long usineId);
}