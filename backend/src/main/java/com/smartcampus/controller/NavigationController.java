package com.smartcampus.controller;

import com.smartcampus.model.CampusNode;
import com.smartcampus.service.PathfindingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/navigation")
// Exposes route searches through a small HTTP-specific adapter.
public class NavigationController {

    private final PathfindingService pathfindingService;

    public NavigationController(PathfindingService pathfindingService) {
        this.pathfindingService = pathfindingService;
    }

    @GetMapping("/route")
    public ResponseEntity<?> getRoute(
            @RequestParam Long startId,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "false") boolean accessibleOnly) {
        try {
            // The service applies A* and the optional accessibility constraint.
            // Delegate graph search to the service so the controller stays focused on HTTP handling.
            List<CampusNode> route = pathfindingService.findShortestPath(startId, targetId, accessibleOnly);

            if (route.isEmpty()) {
                // A path exists in the DB but is unreachable under the chosen constraints.
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(route);
        } catch (IllegalArgumentException e) {
            // One or both node IDs do not exist — return 400 with an explanatory message
            // so the caller knows to fix the request rather than interpret it as missing resource.
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}