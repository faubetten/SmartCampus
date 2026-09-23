package com.smartcampus.repository;

import com.smartcampus.model.CampusEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampusEdgeRepository extends JpaRepository<CampusEdge, Long> {
    List<CampusEdge> findBySourceNodeId(Long sourceNodeId);
}