package com.smartcampus.repository;

import com.smartcampus.model.CampusEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CampusEdgeRepository extends JpaRepository<CampusEdge, Long> {
    // A* expands only the edges leaving its current node.
    List<CampusEdge> findBySourceNodeId(Long sourceNodeId);
}