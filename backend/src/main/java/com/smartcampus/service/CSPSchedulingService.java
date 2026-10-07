package com.smartcampus.service;

import com.smartcampus.model.Room;
import com.smartcampus.repository.ReservationRepository;
import com.smartcampus.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
// Selects a room by filtering its domain and checking temporal conflicts.
public class CSPSchedulingService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public CSPSchedulingService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Carries either the selected room or a short explanation of failure.
     */
    public static class CSPResult {
        public boolean success;
        public Room assignedRoom;
        public String message;

        public CSPResult(boolean success, Room assignedRoom, String message) {
            this.success = success;
            this.assignedRoom = assignedRoom;
            this.message = message;
        }
    }

    /**
     * Solves the Room Allocation CSP for a given booking request.
     * 
     * @param requestedCapacity Minimum group size required
     * @param roomType          Category of facility ('STUDY_POD', 'LABORATORY', etc.)
     * @param startTime         Desired start time
     * @param endTime           Desired end time
     * @return CSPResult containing the allocated room or failure explanation.
     */
    public CSPResult solveRoomAllocationCSP(int requestedCapacity, String roomType, 
                                            LocalDateTime startTime, LocalDateTime endTime) {
        
        // Start with rooms that satisfy the static capacity and type constraints.
        List<Room> domainRooms = roomRepository.findByCapacityGreaterThanEqualAndRoomType(requestedCapacity, roomType);

        if (domainRooms.isEmpty()) {
            return new CSPResult(false, null, "CSP Failed: No rooms match the requested capacity and type domain.");
        }

        // Test each candidate until one satisfies the time constraint.
        for (Room candidateRoom : domainRooms) {
            
            // Keep a defensive check even though the repository already filters capacity.
            if (candidateRoom.getCapacity() < requestedCapacity) {
                continue; // Violates constraint, prune from domain
            }

            // A room cannot be selected when a confirmed or active reservation overlaps.
            var conflictingBookings = reservationRepository.findConflictingReservations(
                candidateRoom.getId(), startTime, endTime
            );

            if (!conflictingBookings.isEmpty()) {
                // Violates temporal exclusion constraint; backtrack/skip this room
                continue; 
            }

            // All constraints satisfied! Return successful variable assignment.
            return new CSPResult(true, candidateRoom, "CSP Success: Valid room configuration found satisfying all constraints.");
        }

        return new CSPResult(false, null, "CSP Backtracking Exhausted: All candidate rooms have temporal conflicts for this time window.");
    }
}