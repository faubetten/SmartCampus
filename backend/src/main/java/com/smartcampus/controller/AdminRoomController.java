package com.smartcampus.controller;

import com.smartcampus.model.Room;
import com.smartcampus.repository.CampusNodeRepository;
import com.smartcampus.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {

    private final RoomRepository roomRepository;
    private final CampusNodeRepository campusNodeRepository;

    public AdminRoomController(RoomRepository roomRepository, CampusNodeRepository campusNodeRepository) {
        this.roomRepository = roomRepository;
        this.campusNodeRepository = campusNodeRepository;
    }

    // 1. Get all rooms
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomRepository.findAll());
    }

    // 2. Get a room by ID
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomRepository.findById(Objects.requireNonNull(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Create a new room
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Room roomRequest) {
        if (roomRepository.findByRoomNumber(roomRequest.getRoomNumber()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A room with number " + roomRequest.getRoomNumber() + " already exists.");
        }

        // Validate that the associated map node exists
        Long nodeId = roomRequest.getNodeId();
        if (nodeId == null) {
            return ResponseEntity.badRequest()
                .body("Campus node with ID null does not exist.");
        }
        if (!campusNodeRepository.existsById(nodeId)) {
            return ResponseEntity.badRequest()
                .body("Campus node with ID " + nodeId + " does not exist.");
        }

        Room savedRoom = roomRepository.save(roomRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    // 4. Update an existing room
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room updatedData) {
        return roomRepository.findById(Objects.requireNonNull(id)).map(existingRoom -> {
            if (updatedData.getNodeId() != null) {
                Long nodeId = Objects.requireNonNull(updatedData.getNodeId());
                if (!campusNodeRepository.existsById(nodeId)) {
                    return ResponseEntity.badRequest()
                            .body("Campus node with ID " + nodeId + " does not exist.");
                }
                existingRoom.setNodeId(nodeId);
            }

            if (updatedData.getRoomNumber() != null) {
                existingRoom.setRoomNumber(updatedData.getRoomNumber());
            }
            if (updatedData.getCapacity() != null) {
                existingRoom.setCapacity(updatedData.getCapacity());
            }
            if (updatedData.getRoomType() != null) {
                existingRoom.setRoomType(updatedData.getRoomType());
            }

            Room saved = roomRepository.save(Objects.requireNonNull(existingRoom));
            return ResponseEntity.ok((Object) saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Delete a room
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        if (!roomRepository.existsById(Objects.requireNonNull(id))) {
            return ResponseEntity.notFound().build();
        }
        roomRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}