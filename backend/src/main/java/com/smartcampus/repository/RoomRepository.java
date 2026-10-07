package com.smartcampus.repository;

import com.smartcampus.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    // Forms the initial room domain for the scheduling constraints.
    List<Room> findByCapacityGreaterThanEqualAndRoomType(Integer capacity, String roomType);
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByRoomType(String roomType);
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);
    Optional<Room> findByNodeId(Long nodeId);
}


