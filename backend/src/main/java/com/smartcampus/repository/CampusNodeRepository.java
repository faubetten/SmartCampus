package com.smartcampus.repository;

import com.smartcampus.model.CampusNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampusNodeRepository extends JpaRepository<CampusNode, Long> {
    List<CampusNode> findByBuildingIdAndFloorNumber(Long buildingId, Integer floorNumber);
}