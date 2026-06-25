package com.safran.repository;

import com.safran.entity.Processus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessusRepository extends JpaRepository<Processus, Long> {
}