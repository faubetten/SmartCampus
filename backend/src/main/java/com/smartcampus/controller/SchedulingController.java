package com.smartcampus.controller;
import com.smartcampus.service.CSPSchedulingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import com.smartcampus.dto.RoomDetailsDto;
import com.smartcampus.dto.BookingResponseDto;

@RestController
@RequestMapping("/api/scheduling")
public class SchedulingController {

    private final CSPSchedulingService cspSchedulingService;
    private static final Logger log = LoggerFactory.getLogger(SchedulingController.class);

    public SchedulingController(CSPSchedulingService cspSchedulingService) {
        this.cspSchedulingService = cspSchedulingService;
    }


    /**
     * Returns all candidate rooms that satisfy the user's preferences and time slot.
    */
    @GetMapping("/available")
    public ResponseEntity<List<RoomDetailsDto>> findAvailableRooms(
            @RequestParam int capacity,
            @RequestParam(defaultValue = "ANY") String roomType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {


        log.info("Finding available rooms with capacity >= {}, type = {}, time window [{}, {}).", capacity, roomType, startTime, endTime);
        List<RoomDetailsDto> rooms = cspSchedulingService.findAvailableRooms(
                capacity, roomType, startTime, endTime
        );
        log.info("Found {} available rooms for the time window [{}, {}).", rooms.size(), startTime, endTime);
        return ResponseEntity.ok(rooms);
    }


    /**
     * Books a specific room selected by the user from the available rooms list.
    */
    @PostMapping("/book")
    public ResponseEntity<BookingResponseDto> bookSpecificRoom(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {


        log.info("Booking request received for room {} from {} to {}.", roomId, startTime, endTime);
        BookingResponseDto response = cspSchedulingService.bookRoom(
                roomId, startTime, endTime
        );
        if (!response.success) {
            log.warn("Booking failed for room {}: {}", roomId, response.message);
            return ResponseEntity.badRequest().body(response);
        }
        log.info("Booking successful for room {}: {}", roomId, response);
        return ResponseEntity.ok(response);
    }

}