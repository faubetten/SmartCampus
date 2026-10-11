package com.smartcampus.service;

import com.smartcampus.model.Reservation;
import com.smartcampus.model.Room;
import com.smartcampus.model.User;
import com.smartcampus.repository.ReservationRepository;
import com.smartcampus.repository.RoomRepository;
import com.smartcampus.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
// Selects a room by filtering its domain and checking temporal conflicts.
public class CSPSchedulingService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public CSPSchedulingService(RoomRepository roomRepository,
                                ReservationRepository reservationRepository,
                                UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
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
     * Carries the CSP outcome together with the persisted Reservation on success.
     */
    public static class BookingResult {
        public boolean success;
        public String message;
        public Reservation reservation;

        public BookingResult(boolean success, String message, Reservation reservation) {
            this.success = success;
            this.message = message;
            this.reservation = reservation;
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

    /**
     * Combines CSP room allocation with immediate persistence of a CONFIRMED booking.
     *
     * <p>The operation is transactional: if saving the Reservation fails the method
     * rolls back so no phantom booking is left in the database.</p>
     *
     * @param userId            ID of the authenticated user making the booking
     * @param requestedCapacity Minimum group size required
     * @param roomType          Category of facility ('STUDY_POD', 'LABORATORY', etc.)
     * @param startTime         Desired start time
     * @param endTime           Desired end time
     * @return BookingResult with the persisted Reservation on success, or a failure message
     */
    @Transactional
    public BookingResult bookRoom(Long userId, int requestedCapacity, String roomType,
                                  LocalDateTime startTime, LocalDateTime endTime) {

        // Verify the user exists before attempting to allocate any room.
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new BookingResult(false, "Booking Failed: User not found.", null);
        }

        // Run the CSP solver to find a conflict-free room.
        CSPResult cspResult = solveRoomAllocationCSP(requestedCapacity, roomType, startTime, endTime);
        if (!cspResult.success) {
            return new BookingResult(false, cspResult.message, null);
        }

        // Persist the confirmed booking for the allocated room.
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setRoomId(cspResult.assignedRoom.getId());
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus("CONFIRMED");

        Reservation saved = reservationRepository.save(reservation);
        return new BookingResult(true,
                "Booking Confirmed: Room " + cspResult.assignedRoom.getId() + " reserved successfully.",
                saved);
    }
}