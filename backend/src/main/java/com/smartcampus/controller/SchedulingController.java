package com.smartcampus.controller;

import com.smartcampus.model.User;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.CSPSchedulingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/scheduling")
// Converts booking parameters from HTTP requests into scheduling-service calls.
public class SchedulingController {

    private final CSPSchedulingService cspSchedulingService;
    private final UserRepository userRepository;

    public SchedulingController(CSPSchedulingService cspSchedulingService,
                                UserRepository userRepository) {
        this.cspSchedulingService = cspSchedulingService;
        this.userRepository = userRepository;
    }

    @PostMapping("/allocate")
    public ResponseEntity<CSPSchedulingService.CSPResult> allocateRoom(
            @RequestParam int capacity,
            @RequestParam String roomType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        // The service applies capacity, type, and time-overlap constraints.
        CSPSchedulingService.CSPResult result = cspSchedulingService.solveRoomAllocationCSP(
                capacity, roomType, startTime, endTime
        );

        if (!result.success) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Allocates the best available room via the CSP solver and immediately
     * persists a CONFIRMED Reservation record for the authenticated user.
     *
     * <p>Requires a valid JWT — the authenticated user's ID is resolved from
     * the security context so it cannot be spoofed via a request parameter.</p>
     *
     * @param capacity  Minimum number of seats required
     * @param roomType  Facility category (e.g. 'STUDY_POD', 'LABORATORY')
     * @param startTime ISO-8601 start of the desired booking window
     * @param endTime   ISO-8601 end of the desired booking window
     * @return 201 Created with the saved Reservation, or 400 on failure
     */
    @PostMapping("/book")
    public ResponseEntity<CSPSchedulingService.BookingResult> bookRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int capacity,
            @RequestParam String roomType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        // Resolve the database user ID from the authenticated principal's email.
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        CSPSchedulingService.BookingResult result =
                cspSchedulingService.bookRoom(user.getId(), capacity, roomType, startTime, endTime);

        if (!result.success) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(201).body(result);
    }
}