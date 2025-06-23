package com.superjoin.spreadsheet.services;

import com.superjoin.spreadsheet.model.CellNode;
import com.superjoin.spreadsheet.model.GraphNode;
import com.superjoin.spreadsheet.model.SheetNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing the knowledge graph of the spreadsheet.
 * This service handles the graph structure, nodes, edges, and graph operations.
 * 
 * Note: This is a simplified implementation without JGraphT dependency for now.
 * In a full implementation, we would use JGraphT for more sophisticated graph operations.
 */
public class KnowledgeGraphService {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeGraphService.class);
    
    // Graph structure using maps for simplicity
    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final Map<String, Set<String>> edges = new HashMap<>(); // source -> set of targets
    private final Map<String, Set<String>> reverseEdges = new HashMap<>(); // target -> set of sources
    
    // Edge types
    public static final String CONTAINS_EDGE = "CONTAINS";
    public static final String DEPENDS_ON_EDGE = "DEPENDS_ON";

    public KnowledgeGraphService() {
        logger.info("Initializing Knowledge Graph Service");
    }

    /**
     * Adds a node to the graph
     */
    public void addNode(GraphNode node) {
        nodes.put(node.getId(), node);
        edges.putIfAbsent(node.getId(), new HashSet<>());
        reverseEdges.putIfAbsent(node.getId(), new HashSet<>());
        logger.debug("Added node: {}", node.getId());
    }

    /**
     * Adds an edge between two nodes
     */
    public void addEdge(String sourceId, String targetId, String edgeType) {
        if (!nodes.containsKey(sourceId) || !nodes.containsKey(targetId)) {
            logger.warn("Cannot add edge: one or both nodes not found. Source: {}, Target: {}", sourceId, targetId);
            return;
        }
        edges.get(sourceId).add(targetId);
        reverseEdges.get(targetId).add(sourceId);
        logger.debug("Added edge: {} -> {} ({})", sourceId, targetId, edgeType);
        logger.info("[EDGE] {} -> {} ({})", sourceId, targetId, edgeType);
    }

    /**
     * Removes an edge between two nodes
     */
    public void removeEdge(String sourceId, String targetId) {
        if (edges.containsKey(sourceId)) {
            edges.get(sourceId).remove(targetId);
        }
        if (reverseEdges.containsKey(targetId)) {
            reverseEdges.get(targetId).remove(sourceId);
        }
        logger.debug("Removed edge: {} -> {}", sourceId, targetId);
    }

    /**
     * Gets all nodes of a specific type
     */
    public List<GraphNode> getNodesByType(String type) {
        return nodes.values().stream()
                .filter(node -> node.getType().equals(type))
                .collect(Collectors.toList());
    }

    /**
     * Gets all cell nodes
     */
    public List<CellNode> getCellNodes() {
        return nodes.values().stream()
                .filter(node -> node instanceof CellNode)
                .map(node -> (CellNode) node)
                .collect(Collectors.toList());
    }

    /**
     * Gets all sheet nodes
     */
    public List<SheetNode> getSheetNodes() {
        return nodes.values().stream()
                .filter(node -> node instanceof SheetNode)
                .map(node -> (SheetNode) node)
                .collect(Collectors.toList());
    }

    /**
     * Gets a node by its ID
     */
    public GraphNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Gets all direct dependencies of a node (outgoing edges)
     */
    public Set<String> getDependencies(String nodeId) {
        return edges.getOrDefault(nodeId, new HashSet<>());
    }

    /**
     * Gets all nodes that depend on this node (incoming edges)
     */
    public Set<String> getDependents(String nodeId) {
        Set<String> dependents = reverseEdges.getOrDefault(nodeId, new HashSet<>());
        logger.info("getDependents for {}: {}", nodeId, dependents);
        return dependents;
    }

    /**
     * Finds all nodes that are reachable from the given node (transitive dependencies)
     */
    public Set<String> getTransitiveDependencies(String nodeId) {
        Set<String> visited = new HashSet<>();
        Set<String> result = new HashSet<>();
        dfs(nodeId, visited, result);
        return result;
    }

    /**
     * Finds all nodes that can reach the given node (transitive dependents)
     */
    public Set<String> getTransitiveDependents(String nodeId) {
        Set<String> visited = new HashSet<>();
        Set<String> result = new HashSet<>();
        reverseDfs(nodeId, visited, result);
        logger.info("getTransitiveDependents for {}: {}", nodeId, result);
        return result;
    }

    /**
     * Depth-first search for finding transitive dependencies
     */
    private void dfs(String nodeId, Set<String> visited, Set<String> result) {
        if (visited.contains(nodeId)) {
            return;
        }
        visited.add(nodeId);
        
        Set<String> dependencies = edges.getOrDefault(nodeId, new HashSet<>());
        for (String dependency : dependencies) {
            result.add(dependency);
            dfs(dependency, visited, result);
        }
    }

    /**
     * Reverse depth-first search for finding transitive dependents
     */
    private void reverseDfs(String nodeId, Set<String> visited, Set<String> result) {
        if (visited.contains(nodeId)) {
            return;
        }
        visited.add(nodeId);
        
        Set<String> dependents = reverseEdges.getOrDefault(nodeId, new HashSet<>());
        for (String dependent : dependents) {
            result.add(dependent);
            reverseDfs(dependent, visited, result);
        }
    }

    /**
     * Gets all cells that contain formulas
     */
    public List<CellNode> getFormulaCells() {
        return getCellNodes().stream()
                .filter(CellNode::hasFormula)
                .collect(Collectors.toList());
    }

    /**
     * Finds a cell node by its A1 notation
     */
    public CellNode findCellByA1Notation(String sheetName, String a1Notation) {
        String fullReference = sheetName + "!" + a1Notation;
        GraphNode node = nodes.get(fullReference);
        return node instanceof CellNode ? (CellNode) node : null;
    }

    /**
     * Gets the total number of nodes in the graph
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * Gets the total number of edges in the graph
     */
    public int getEdgeCount() {
        return edges.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * Clears the entire graph
     */
    public void clear() {
        nodes.clear();
        edges.clear();
        reverseEdges.clear();
        logger.info("Cleared knowledge graph");
    }

    /**
     * Gets a summary of the graph structure
     */
    public String getGraphSummary() {
        int cellCount = getCellNodes().size();
        int sheetCount = getSheetNodes().size();
        int formulaCount = getFormulaCells().size();
        int edgeCount = getEdgeCount();
        
        return String.format("Graph Summary: %d cells, %d sheets, %d formulas, %d edges", 
                cellCount, sheetCount, formulaCount, edgeCount);
    }
}
