package com.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "room_type", nullable = false)
    private String roomType; // e.g., 'STUDY_POD', 'LABORATORY'

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
}