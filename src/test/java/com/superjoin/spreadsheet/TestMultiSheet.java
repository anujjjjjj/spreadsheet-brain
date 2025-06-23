package com.superjoin.spreadsheet;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Simple test class for multi-sheet functionality
 */
public class TestMultiSheet {
    
    public static void main(String[] args) {
        try {
            System.out.println("Testing Multi-Sheet Functionality...");
            
            // Test with a public spreadsheet that doesn't require authentication
            String spreadsheetId = "YOUR_SPREADSHEET_ID";
            
            SpreadsheetGraph graph = new SpreadsheetGraph();
            
            System.out.println("Loading all sheets from spreadsheet: " + spreadsheetId);
            graph.loadAllSheets(spreadsheetId);
            
            System.out.println("Graph Summary:");
            System.out.println(graph.getGraphSummary());
            
            System.out.println("\nMulti-sheet test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error during multi-sheet test: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 