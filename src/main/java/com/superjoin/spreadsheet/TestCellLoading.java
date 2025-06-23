package com.superjoin.spreadsheet;

/**
 * Simple test to verify cell loading functionality
 */
public class TestCellLoading {
    
    public static void main(String[] args) {
        try {
            System.out.println("Testing cell loading...");
            
            SpreadsheetGraph graph = new SpreadsheetGraph();
            
            // Load the spreadsheet
            System.out.println("Loading spreadsheet...");
            graph.loadAllSheets("1QjtoAguM5as_G2_FuHOEVoLT3ro_7hfsZENngQk5UTU");
            
            // Try to find cell N27
            System.out.println("Testing cell N27...");
            var impact = graph.analyzeImpact("N27");
            
            System.out.println("Impact analysis result size: " + impact.size());
            
            // Check if the cell exists in the graph
            var cellNode = graph.getGraphService().findCellByA1Notation("Sheet1", "N27");
            if (cellNode != null) {
                System.out.println("SUCCESS: Found cell: " + cellNode.getId());
                System.out.println("Cell value: " + cellNode.getValue());
                System.out.println("Cell formula: " + cellNode.getFormula());
            } else {
                System.out.println("FAILED: Cell N27 not found in graph");
                
                // List some available cells
                System.out.println("Available cells (first 10):");
                var cells = graph.getGraphService().getCellNodes();
                for (int i = 0; i < Math.min(10, cells.size()); i++) {
                    System.out.println("  " + cells.get(i).getId());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 