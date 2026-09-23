package com.smartcampus.repository;

import com.smartcampus.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByCapacityGreaterThanEqualAndRoomType(Integer capacity, String roomType);
}