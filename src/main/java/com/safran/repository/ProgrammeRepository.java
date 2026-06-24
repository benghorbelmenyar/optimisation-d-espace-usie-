package com.safran.repository;

import com.safran.entity.Programme;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgrammeRepository extends JpaRepository<Programme, Long> {
    List<Programme> findByActiviteId(Long activiteId);
}