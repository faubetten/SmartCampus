package com.smartcampus.service;

import com.smartcampus.model.Reservation;
import com.smartcampus.model.Room;
import com.smartcampus.model.User;
import com.smartcampus.repository.ReservationRepository;
import com.smartcampus.repository.RoomRepository;
import com.smartcampus.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.smartcampus.dto.RoomDetailsDto;
import com.smartcampus.dto.BookingResponseDto;



@Service
// Selects a room by filtering its domain and checking temporal conflicts.
public class CSPSchedulingService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(CSPSchedulingService.class);

    public CSPSchedulingService(RoomRepository roomRepository,
                                ReservationRepository reservationRepository,
                                UserRepository userRepository,
                                JdbcTemplate jdbcTemplate) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }


public List<RoomDetailsDto> findAvailableRooms (int requestedCapacity, String roomType, LocalDateTime startTime, LocalDateTime endTime) {
    // Start with rooms that satisfy the static capacity and type constraints.

    if(startTime.isAfter(endTime) || startTime.isEqual(endTime)|| startTime.isBefore(LocalDateTime.now())|| endTime.isBefore(LocalDateTime.now())|| startTime == null || endTime == null) {
        log.warn("findAvailableRooms: Invalid time window [{}, {}).", startTime, endTime);
        return List.of(); // Return empty list for invalid time window
    }

    if (requestedCapacity <= 0) {
        log.warn("findAvailableRooms: Invalid requested capacity {}.", requestedCapacity);
        return List.of(); // Return empty list for invalid capacity
    }

    List<Room> domainRooms;

    if (roomType == null || roomType.isEmpty()|| roomType.equalsIgnoreCase("ANY")) {
        log.info("findAvailableRooms: No specific room type requested, considering all available rooms.");
        domainRooms = roomRepository.findByCapacityGreaterThanEqual(requestedCapacity);
    } else {
        log.info("findAvailableRooms: Specific room type requested: {}.", roomType);
        domainRooms = roomRepository.findByCapacityGreaterThanEqualAndRoomType(requestedCapacity, roomType.trim().toUpperCase());
    }

    if (domainRooms.isEmpty()) {
        log.warn("findAvailableRooms: No rooms match the requested capacity {} and type {}.", requestedCapacity, roomType);
        return List.of(); // Return empty list if no rooms match the criteria
    }

    log.info("findAvailableRooms: Found {} rooms matching the requested capacity and type.", domainRooms.size());

    // Sort the rooms by capacity in ascending order.
    log.info("findAvailableRooms: Sorting rooms by capacity in ascending order.");
    domainRooms.sort(Comparator.comparingInt(Room::getCapacity));
    log.info("findAvailableRooms: Sorted rooms: {}.", domainRooms.stream().map(Room::getId).collect(Collectors.toList()));


    // Filter out rooms that have conflicting reservations in the specified time window.
    
    log.info("findAvailableRooms: Filtering out rooms with conflicting reservations in the time window [{}, {}).", startTime, endTime);
    
    List<RoomDetailsDto> availableRooms = new ArrayList<>();
    for(Room candidate : domainRooms){
        var conflictingBookings = reservationRepository.findConflictingReservations(candidate.getId(), startTime, endTime);
        if(conflictingBookings.isEmpty()){
            availableRooms.add(enrichRoomDetails(candidate));
        } else {
            log.info("findAvailableRooms: Room {} has {} conflicting bookings in the time window [{}, {}).", candidate.getId(), conflictingBookings.size(), startTime, endTime);
        }
    }


    log.info("findAvailableRooms: Found {} available rooms after filtering for time conflicts.", availableRooms.size());
    return availableRooms;
}



    /**
     * Attempts to book a room for a user, given the room ID and desired time window.
     *
     * <p>The operation is transactional: if saving the Reservation fails the method
     * rolls back so no phantom booking is left in the database.</p>
     *
     * @param roomId            ID of the room to be booked
     * @param startTime         Desired start time
     * @param endTime           Desired end time
     * @return BookingResponseDto  With the persisted Reservation on success, or a failure message
     */
    @Transactional
    public BookingResponseDto bookRoom(Long roomId,
                                  LocalDateTime startTime, LocalDateTime endTime) {


        // Verify the room exists before attempting to book it.
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            log.warn("Booking: Room {} not found.", roomId);
            return new BookingResponseDto(false, "Booking Failed: Room not found.", null, null);
        }

        // Validate the time window for the booking request.
        if(startTime.isAfter(endTime) || startTime.isEqual(endTime)|| startTime.isBefore(LocalDateTime.now())|| endTime.isBefore(LocalDateTime.now())|| startTime == null || endTime == null) {
            log.warn("Booking: Invalid time window [{}, {}).", startTime, endTime);
            return new BookingResponseDto(false, "Booking Failed: Invalid time window.", null, null);
        }

        var conflictingBookings = reservationRepository.findConflictingReservations(roomId, startTime, endTime);
        if (!conflictingBookings.isEmpty()) {
            log.warn("Booking: Room {} has {} conflicting bookings in the time window [{}, {}).", roomId, conflictingBookings.size(), startTime, endTime);
            return new BookingResponseDto(false, "Booking Failed: Room is already booked for the requested time window.", null, null);
        }


    
        Long userId = resolveCurrentUserId();
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setRoomId(roomId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus("CONFIRMED");
        log.info("Booking: Persisting confirmed booking for user {} and room {}.", userId, roomId);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Booking: Confirmed booking saved successfully for user {} and room {}.", userId, roomId);

        log.info("Booking: Enriching room details for room {}.", roomId);
        RoomDetailsDto enrichedRoom = enrichRoomDetails(room);

        log.info("Booking: Enriched room details for room {}.", roomId);
        return new BookingResponseDto(true,
                "Booking Confirmed: Room " + room.getRoomNumber() + " reserved successfully.",
                saved,
                enrichedRoom);
    }



/**
    * Enriches a Room entity with Building, Floor, Coordinates, and Equipment details.
*/
    public RoomDetailsDto enrichRoomDetails(Room room) {
        RoomDetailsDto dto = new RoomDetailsDto( room, null, null, null, null, null);

        // Query spatial & building info

        log.info("enrichRoomDetails: Fetching building and spatial details for room {}.", room.getId());

        String spatialSql = """
            SELECT b.name, cn.floor_number, cn.coord_x, cn.coord_y
            FROM campus_nodes cn
            JOIN buildings b ON cn.building_id = b.building_id
            WHERE cn.node_id = ?
        """;

        log.info("enrichRoomDetails: Executing spatial query for room {}.", room.getId());
        jdbcTemplate.query(spatialSql, rs -> {
            dto.buildingName = rs.getString("name");
            dto.floorNumber = rs.getInt("floor_number");
            dto.coordX = rs.getBigDecimal("coord_x");
            dto.coordY = rs.getBigDecimal("coord_y");
        }, room.getNodeId());

        log.info("enrichRoomDetails: Fetching equipment details for room {}.", room.getId());

        // Query installed equipment
        String equipSql = """
            SELECT e.equipment_name, re.quantity
            FROM room_equipment re
            JOIN equipment e ON re.equipment_id = e.equipment_id
            WHERE re.room_id = ?
            ORDER BY e.equipment_name
        """;

        log.info("enrichRoomDetails: Executing equipment query for room {}.", room.getId());
        dto.equipment = jdbcTemplate.query(equipSql, (rs, rowNum) ->
                rs.getString("equipment_name") + " (x" + rs.getInt("quantity") + ")",
                room.getId()
        );

        log.info("enrichRoomDetails: Fetched equipment details for room {}.", room.getId());

        return dto;
    }


    // Resolves the currently authenticated user's ID from the security context.
    private Long resolveCurrentUserId() {
        log.info("Resolving current authenticated user ID.");
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                return userRepository.findByEmail(auth.getName())
                        .map(User::getId)
                        .orElse(1L);
            }
        } catch (Exception ignored) {}
        return 1L;
    }
}