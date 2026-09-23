package com.smartcampus.service;

import com.smartcampus.model.Room;
import com.smartcampus.repository.ReservationRepository;
import com.smartcampus.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CSPSchedulingService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public CSPSchedulingService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * CSP Formulation Result Holder
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
        
        // Step 1: Define Domain (D) -> Fetch all rooms matching basic type and capacity
        List<Room> domainRooms = roomRepository.findByCapacityGreaterThanEqualAndRoomType(requestedCapacity, roomType);

        if (domainRooms.isEmpty()) {
            return new CSPResult(false, null, "CSP Failed: No rooms match the requested capacity and type domain.");
        }

        // Step 2: Apply Constraint Propagation & Backtracking Search
        for (Room candidateRoom : domainRooms) {
            
            // Constraint 1: Hard Capacity Check (redundant safety check)
            if (candidateRoom.getCapacity() < requestedCapacity) {
                continue; // Violates constraint, prune from domain
            }

            // Constraint 2: Temporal Non-Overlap Check (Checking active database reservations)
            List<org.springframework.data.jpa.repository.query.JpaQueryMethodFactory> conflicts = null; // type safety
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