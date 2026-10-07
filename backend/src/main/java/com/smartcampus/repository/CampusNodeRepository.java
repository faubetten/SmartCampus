package com.smartcampus.repository;

import com.smartcampus.model.CampusNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CampusNodeRepository extends JpaRepository<CampusNode, Long> {
    // Supports floor-specific map and administration queries.
    List<CampusNode> findByBuildingIdAndFloorNumber(Long buildingId, Integer floorNumber);
}