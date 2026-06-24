package com.safran.repository;

import com.safran.entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PosteRepository extends JpaRepository<Poste, Long> {
    List<Poste> findByUsineId(Long usineId);
}