package com.superjoin.spreadsheet.model;

/**
 * Interface for all nodes in the spreadsheet knowledge graph.
 * This provides a common contract for different types of nodes
 * like cells, sheets, and potentially named ranges in the future.
 */
public interface GraphNode {
    
    /**
     * Returns a unique identifier for this node.
     * This is used for graph operations and equality comparisons.
     */
    String getId();
    
    /**
     * Returns the type of this node (e.g., "CELL", "SHEET").
     * This helps in identifying the node type during graph traversal.
     */
    String getType();
    
    String getDisplayName();
}
