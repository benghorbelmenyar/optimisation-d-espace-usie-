package com.safran.repository;

import com.safran.entity.Programme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgrammeRepository extends JpaRepository<Programme, Long> {
    Optional<Programme> findByNom(String nom);
    List<Programme> findByActiviteId(Long activiteId);
}