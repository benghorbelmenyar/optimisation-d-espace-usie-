package com.safran.repository;

import com.safran.entity.Activite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActiviteRepository extends JpaRepository<Activite, Long> {
    List<Activite> findByUsineId(Long usineId);
}