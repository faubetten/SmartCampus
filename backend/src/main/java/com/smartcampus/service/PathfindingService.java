package com.smartcampus.service;

import com.smartcampus.model.CampusEdge;
import com.smartcampus.model.CampusNode;
import com.smartcampus.repository.CampusEdgeRepository;
import com.smartcampus.repository.CampusNodeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PathfindingService {

    private final CampusNodeRepository nodeRepository;
    private final CampusEdgeRepository edgeRepository;

    public PathfindingService(CampusNodeRepository nodeRepository, CampusEdgeRepository edgeRepository) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
    }

    /**
     * Represents a node in the A* search tree with cost metrics.
     */
    private static class SearchNode implements Comparable<SearchNode> {
        CampusNode node;
        double gScore; // Cost from start to current node
        double fScore; // Estimated total cost (gScore + heuristic)
        SearchNode cameFrom;

        public SearchNode(CampusNode node, double gScore, double fScore, SearchNode cameFrom) {
            this.node = node;
            this.gScore = gScore;
            this.fScore = fScore;
            this.cameFrom = cameFrom;
        }

        @Override
        public int compareTo(SearchNode other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    /**
     * Calculates the optimal path between two nodes using A* search.
     * 
     * @param startId         Starting Node ID
     * @param targetId        Destination Node ID
     * @param accessibleOnly  If true, excludes stairs to support wheelchair routing
     * @return List of nodes representing the optimal path, or empty list if no path exists.
     */
    public List<CampusNode> findShortestPath(Long startId, Long targetId, boolean accessibleOnly) {
        CampusNode startNode = nodeRepository.findById(startId)
                .orElseThrow(() -> new IllegalArgumentException("Start node not found: " + startId));
        CampusNode targetNode = nodeRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target node not found: " + targetId));

        PriorityQueue<SearchNode> openSet = new PriorityQueue<>();
        Map<Long, Double> gScores = new HashMap<>();
        Map<Long, SearchNode> allNodes = new HashMap<>();

        gScores.put(startNode.getId(), 0.0);
        SearchNode startSearchNode = new SearchNode(startNode, 0.0, heuristic(startNode, targetNode), null);
        openSet.add(startSearchNode);
        allNodes.put(startNode.getId(), startSearchNode);

        Set<Long> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            SearchNode current = openSet.poll();

            // If we reached the destination, reconstruct and return path
            if (current.node.getId().equals(targetNode.getId())) {
                return reconstructPath(current);
            }

            closedSet.add(current.node.getId());

            // Fetch outgoing edges from current node
            List<CampusEdge> edges = edgeRepository.findBySourceNodeId(current.node.getId());

            for (CampusEdge edge : edges) {
                // Skip stairs if accessibility filter is requested
                if (accessibleOnly && edge.getRequiresStairs()) {
                    continue;
                }

                CampusNode neighbor = nodeRepository.findById(edge.getTargetNodeId()).orElse(null);
                if (neighbor == null || !neighbor.getIsAccessible() && accessibleOnly) {
                    continue;
                }

                if (closedSet.contains(neighbor.getId())) {
                    continue;
                }

                double tentativeGScore = current.gScore + edge.getWeightDistance().doubleValue();

                if (tentativeGScore < gScores.getOrDefault(neighbor.getId(), Double.MAX_VALUE)) {
                    gScores.put(neighbor.getId(), tentativeGScore);
                    double fScore = tentativeGScore + heuristic(neighbor, targetNode);

                    SearchNode neighborSearchNode = new SearchNode(neighbor, tentativeGScore, fScore, current);
                    allNodes.put(neighbor.getId(), neighborSearchNode);
                    
                    // Update priority queue
                    openSet.removeIf(sn -> sn.node.getId().equals(neighbor.getId()));
                    openSet.add(neighborSearchNode);
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    /**
     * Heuristic function: Euclidean distance combined with floor level penalty.
     */
    private double heuristic(CampusNode a, CampusNode b) {
        double dx = a.getCoordX().subtract(b.getCoordX()).doubleValue();
        double dy = a.getCoordY().subtract(b.getCoordY()).doubleValue();
        double spatialDistance = Math.sqrt((dx * dx) + (dy * dy));

        // Add a penalty if changing floors without an elevator
        int floorDiff = Math.abs(a.getFloorNumber() - b.getFloorNumber());
        double floorPenalty = floorDiff * 10.0;

        return spatialDistance + floorPenalty;
    }

    /**
     * Reconstructs the path from target back to start using parent pointers.
     */
    private List<CampusNode> reconstructPath(SearchNode current) {
        LinkedList<CampusNode> path = new LinkedList<>();
        while (current != null) {
            path.addFirst(current.node);
            current = current.cameFrom;
        }
        return path;
    }
}