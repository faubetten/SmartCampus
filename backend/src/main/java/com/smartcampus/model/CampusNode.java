package com.smartcampus.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "campus_nodes")
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
    private String nodeType; // e.g., 'CORRIDOR', 'ROOM', 'STAIRS', 'ELEVATOR'

    @Column(name = "is_accessible", nullable = false)
    private Boolean isAccessible = true;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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