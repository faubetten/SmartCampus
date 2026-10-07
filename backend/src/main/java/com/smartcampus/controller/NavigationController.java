package com.smartcampus.controller;

import com.smartcampus.model.CampusNode;
import com.smartcampus.service.PathfindingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/navigation")
// Exposes route searches through a small HTTP-specific adapter.
public class NavigationController {

    private final PathfindingService pathfindingService;

    public NavigationController(PathfindingService pathfindingService) {
        this.pathfindingService = pathfindingService;
    }

    @GetMapping("/route")
    public ResponseEntity<List<CampusNode>> getRoute(
            @RequestParam Long startId,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "false") boolean accessibleOnly) {
        // The service applies A* and the optional accessibility constraint.
        // Delegate graph search to the service so the controller stays focused on HTTP handling.
        List<CampusNode> route = pathfindingService.findShortestPath(startId, targetId, accessibleOnly);
        
        if (route.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(route);
    }
}