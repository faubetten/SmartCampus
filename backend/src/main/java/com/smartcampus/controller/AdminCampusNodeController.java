package com.smartcampus.controller;

import com.smartcampus.model.CampusNode;
import com.smartcampus.repository.CampusNodeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/campus-nodes")
public class AdminCampusNodeController {

    private final CampusNodeRepository campusNodeRepository;

    public AdminCampusNodeController(CampusNodeRepository campusNodeRepository) {
        this.campusNodeRepository = campusNodeRepository;
    }

    @GetMapping
    public ResponseEntity<List<CampusNode>> getAllCampusNodes() {
        return ResponseEntity.ok(campusNodeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampusNode> getCampusNodeById(@PathVariable Long id) {
        return campusNodeRepository.findById(Objects.requireNonNull(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CampusNode> createCampusNode(@RequestBody CampusNode campusNode) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campusNodeRepository.save(campusNode));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampusNode> updateCampusNode(@PathVariable Long id,
                                                         @RequestBody CampusNode updatedData) {
        return campusNodeRepository.findById(Objects.requireNonNull(id)).map(existingNode -> {
            if (updatedData.getBuildingId() != null) existingNode.setBuildingId(updatedData.getBuildingId());
            if (updatedData.getFloorNumber() != null) existingNode.setFloorNumber(updatedData.getFloorNumber());
            if (updatedData.getCoordX() != null) existingNode.setCoordX(updatedData.getCoordX());
            if (updatedData.getCoordY() != null) existingNode.setCoordY(updatedData.getCoordY());
            if (updatedData.getNodeType() != null) existingNode.setNodeType(updatedData.getNodeType());
            if (updatedData.getIsAccessible() != null) existingNode.setIsAccessible(updatedData.getIsAccessible());
            return ResponseEntity.ok(campusNodeRepository.save(existingNode));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampusNode(@PathVariable Long id) {
        if (!campusNodeRepository.existsById(Objects.requireNonNull(id))) {
            return ResponseEntity.notFound().build();
        }
        campusNodeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
