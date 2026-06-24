package com.safran.repository;

import com.safran.entity.Usine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsineRepository extends JpaRepository<Usine, Long> {
}