package com.smartcampus.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "campus_edges")
public class CampusEdge {

    // Database identifier for this directed connection in the campus graph.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "edge_id")
    private Long id;

    @Column(name = "source_node_id", nullable = false)
    private Long sourceNodeId;

    @Column(name = "target_node_id", nullable = false)
    private Long targetNodeId;

    @Column(name = "weight_distance", precision = 8, scale = 2, nullable = false)
    private BigDecimal weightDistance;

    @Column(name = "requires_stairs", nullable = false)
    private Boolean requiresStairs = false;

    // Accessors used by JPA and by the pathfinding service.
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(Long sourceNodeId) { this.sourceNodeId = sourceNodeId; }
    public Long getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(Long targetNodeId) { this.targetNodeId = targetNodeId; }
    public BigDecimal getWeightDistance() { return weightDistance; }
    public void setWeightDistance(BigDecimal weightDistance) { this.weightDistance = weightDistance; }
    public Boolean getRequiresStairs() { return requiresStairs; }
    public void setRequiresStairs(Boolean requiresStairs) { this.requiresStairs = requiresStairs; }
}