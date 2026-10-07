package com.smartcampus.controller;

import com.smartcampus.model.CampusEdge;
import com.smartcampus.repository.CampusEdgeRepository;
import com.smartcampus.repository.CampusNodeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/campus-edges")
public class AdminCampusEdgeController {

    private final CampusEdgeRepository campusEdgeRepository;
    private final CampusNodeRepository campusNodeRepository;

    public AdminCampusEdgeController(CampusEdgeRepository campusEdgeRepository,
                                      CampusNodeRepository campusNodeRepository) {
        this.campusEdgeRepository = campusEdgeRepository;
        this.campusNodeRepository = campusNodeRepository;
    }

    @GetMapping
    public ResponseEntity<List<CampusEdge>> getAllCampusEdges() {
        return ResponseEntity.ok(campusEdgeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampusEdge> getCampusEdgeById(@PathVariable Long id) {
        return campusEdgeRepository.findById(Objects.requireNonNull(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCampusEdge(@RequestBody CampusEdge edge) {
        ResponseEntity<?> validation = validateNodes(edge);
        if (validation != null) return validation;
        return ResponseEntity.status(HttpStatus.CREATED).body(campusEdgeRepository.save(edge));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampusEdge(@PathVariable Long id, @RequestBody CampusEdge updatedData) {
        return campusEdgeRepository.findById(Objects.requireNonNull(id)).map(existingEdge -> {
            if (updatedData.getSourceNodeId() != null) existingEdge.setSourceNodeId(updatedData.getSourceNodeId());
            if (updatedData.getTargetNodeId() != null) existingEdge.setTargetNodeId(updatedData.getTargetNodeId());
            if (updatedData.getWeightDistance() != null) existingEdge.setWeightDistance(updatedData.getWeightDistance());
            if (updatedData.getRequiresStairs() != null) existingEdge.setRequiresStairs(updatedData.getRequiresStairs());
            ResponseEntity<?> validation = validateNodes(existingEdge);
            if (validation != null) return validation;
            return ResponseEntity.ok((Object) campusEdgeRepository.save(existingEdge));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampusEdge(@PathVariable Long id) {
        if (!campusEdgeRepository.existsById(Objects.requireNonNull(id))) {
            return ResponseEntity.notFound().build();
        }
        campusEdgeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> validateNodes(CampusEdge edge) {
        if (edge.getSourceNodeId() == null || !campusNodeRepository.existsById(edge.getSourceNodeId())) {
            return ResponseEntity.badRequest().body("Source campus node does not exist.");
        }
        if (edge.getTargetNodeId() == null || !campusNodeRepository.existsById(edge.getTargetNodeId())) {
            return ResponseEntity.badRequest().body("Target campus node does not exist.");
        }
        return null;
    }
}
