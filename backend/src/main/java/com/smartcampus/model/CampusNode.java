package com.smartcampus.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "campus_nodes")
// Represents a location vertex in the campus navigation graph.
public class CampusNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "node_id")
    private Long id;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "coord_x", precision = 8, scale = 2, nullable = false)
    private BigDecimal coordX;

    @Column(name = "coord_y", precision = 8, scale = 2, nullable = false)
    private BigDecimal coordY;

    @Column(name = "node_type", nullable = false)
    // Describes the location's role in the navigation graph.
    private String nodeType;

    @Column(name = "is_accessible", nullable = false)
    private Boolean isAccessible = true;

    // Accessors used when mapping database rows and serializing route results.
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
    public BigDecimal getCoordX() { return coordX; }
    public void setCoordX(BigDecimal coordX) { this.coordX = coordX; }
    public BigDecimal getCoordY() { return coordY; }
    public void setCoordY(BigDecimal coordY) { this.coordY = coordY; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public Boolean getIsAccessible() { return isAccessible; }
    public void setIsAccessible(Boolean accessible) { isAccessible = accessible; }
}