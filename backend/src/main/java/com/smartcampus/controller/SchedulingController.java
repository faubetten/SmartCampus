package com.smartcampus.controller;

import com.smartcampus.service.CSPSchedulingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/scheduling")
public class SchedulingController {

    private final CSPSchedulingService cspSchedulingService;

    public SchedulingController(CSPSchedulingService cspSchedulingService) {
        this.cspSchedulingService = cspSchedulingService;
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
}