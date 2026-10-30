package com.smartcampus.dto;

import com.smartcampus.model.Room;
import java.math.BigDecimal;
import java.util.List;


/**
     * Data Transfer Object for room details.
*/
public class RoomDetailsDto{
        
        public Long roomId;
        public Long nodeId;
        public String roomName;
        public int capacity;
        public String roomType;
        public String roomNumber;
        public String buildingName;
        public Integer floorNumber;
        public BigDecimal coordX;
        public BigDecimal coordY;
        public List<String> equipment;

        public RoomDetailsDto(Room room, String buildingName, Integer floorNumber, BigDecimal coordX, BigDecimal coordY, List<String> equipment) {
            this.roomId = room.getId();
            this.nodeId = room.getNodeId();
            this.roomName = "Room " + room.getRoomNumber();
            this.capacity = room.getCapacity();
            this.roomType = room.getRoomType();
            this.roomNumber = room.getRoomNumber();
            this.buildingName = buildingName;
            this.floorNumber = floorNumber;
            this.coordX = coordX;
            this.coordY = coordY;
            this.equipment = equipment;
        }
    
    }
