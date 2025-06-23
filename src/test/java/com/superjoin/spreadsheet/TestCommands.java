package com.superjoin.spreadsheet;

import com.superjoin.spreadsheet.GeminiQueryService;
import com.superjoin.spreadsheet.SpreadsheetGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TestCommands {
    
    private SpreadsheetGraph graph;
    private GeminiQueryService queryService;
    
    @BeforeEach
    void setUp() throws IOException, GeneralSecurityException {
        graph = new SpreadsheetGraph();
        queryService = new GeminiQueryService("test-key");
        
        // Load spreadsheet data
        graph.loadAllSheets("YOUR_SPREADSHEET_ID");
    }
    
    @Test
    void testListSheetsCommand() {
        System.out.println("Testing list_sheets command...");
        
        // Test the fallback processing
        GeminiQueryService.QueryResult result = queryService.translateQuery("Show me all sheets in this spreadsheet", graph);
        System.out.println("Query result: " + result.getStructuredQuery());
        System.out.println("Status: " + result.getStatus());
        
        // Execute the query
        GeminiQueryService.QueryExecutionResult execResult = queryService.executeQuery(result.getStructuredQuery(), graph);
        System.out.println("Execution result: " + execResult.getMessage());
        System.out.println("Success: " + execResult.isSuccess());
        
        if (execResult.getData() != null) {
            System.out.println("Data: " + execResult.getData());
        }
    }
    
    @Test
    void testSheetSpecificFindCellsCommand() {
        System.out.println("Testing sheet-specific find_cells command...");
        
        // Test the fallback processing
        GeminiQueryService.QueryResult result = queryService.translateQuery("Show me cells in the Class Data sheet", graph);
        System.out.println("Query result: " + result.getStructuredQuery());
        System.out.println("Status: " + result.getStatus());
        
        // Execute the query
        GeminiQueryService.QueryExecutionResult execResult = queryService.executeQuery(result.getStructuredQuery(), graph);
        System.out.println("Execution result: " + execResult.getMessage());
        System.out.println("Success: " + execResult.isSuccess());
        
        if (execResult.getData() != null) {
            System.out.println("Data: " + execResult.getData());
        }
    }
    
    @Test
    void testDependencyCommand() {
        System.out.println("Testing dependency command...");
        
        // Test the fallback processing
        GeminiQueryService.QueryResult result = queryService.translateQuery("What cells depend on A1", graph);
        System.out.println("Query result: " + result.getStructuredQuery());
        System.out.println("Status: " + result.getStatus());
        
        // Execute the query
        GeminiQueryService.QueryExecutionResult execResult = queryService.executeQuery(result.getStructuredQuery(), graph);
        System.out.println("Execution result: " + execResult.getMessage());
        System.out.println("Success: " + execResult.isSuccess());
        
        if (execResult.getData() != null) {
            System.out.println("Data: " + execResult.getData());
        }
    }
    
    @Test
    void testFormulaCommand() {
        System.out.println("Testing formula command...");
        
        // Test the fallback processing
        GeminiQueryService.QueryResult result = queryService.translateQuery("Show me all cells containing formulas", graph);
        System.out.println("Query result: " + result.getStructuredQuery());
        System.out.println("Status: " + result.getStatus());
        
        // Execute the query
        GeminiQueryService.QueryExecutionResult execResult = queryService.executeQuery(result.getStructuredQuery(), graph);
        System.out.println("Execution result: " + execResult.getMessage());
        System.out.println("Success: " + execResult.isSuccess());
        
        if (execResult.getData() != null) {
            System.out.println("Data: " + execResult.getData());
        }
    }
    
    @Test
    void testSheetSpecificQuery() {
        System.out.println("Testing sheet-specific query...");
        
        // Test the fallback processing
        GeminiQueryService.QueryResult result = queryService.translateQuery("Show me cells in the Class Data sheet", graph);
        System.out.println("Query result: " + result.getStructuredQuery());
        System.out.println("Status: " + result.getStatus());
        
        // Execute the query
        GeminiQueryService.QueryExecutionResult execResult = queryService.executeQuery(result.getStructuredQuery(), graph);
        System.out.println("Execution result: " + execResult.getMessage());
        System.out.println("Success: " + execResult.isSuccess());
        
        if (execResult.getData() != null) {
            System.out.println("Data: " + execResult.getData());
        }
    }
    
    @Test
    void testImpactAnalysis() {
        System.out.println("Testing impact analysis...");
        
        // Test the fallback processing
        GeminiQueryService.QueryResult result = queryService.translateQuery("What is the impact of changing A1", graph);
        System.out.println("Query result: " + result.getStructuredQuery());
        System.out.println("Status: " + result.getStatus());
        
        // Execute the query
        GeminiQueryService.QueryExecutionResult execResult = queryService.executeQuery(result.getStructuredQuery(), graph);
        System.out.println("Execution result: " + execResult.getMessage());
        System.out.println("Success: " + execResult.isSuccess());
        
        if (execResult.getData() != null) {
            System.out.println("Data: " + execResult.getData());
        }
    }
} 