package com.safran.repository;

import com.safran.entity.Contrainte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContrainteRepository extends JpaRepository<Contrainte, Long> {
    List<Contrainte> findByPosteSourceIdOrPosteCibleId(Long posteSourceId, Long posteCibleId);
}