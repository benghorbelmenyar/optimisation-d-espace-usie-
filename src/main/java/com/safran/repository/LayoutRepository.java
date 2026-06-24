package com.safran.repository;

import com.safran.entity.Layout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LayoutRepository extends JpaRepository<Layout, Long> {
    List<Layout> findByUsineId(Long usineId);
}