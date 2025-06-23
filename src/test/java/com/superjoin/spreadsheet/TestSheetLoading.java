package com.superjoin.spreadsheet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.Map;
import java.util.List;

public class TestSheetLoading {
    
    private SheetsReader reader;
    
    @BeforeEach
    void setUp() throws Exception {
        reader = new SheetsReader();
    }
    
    @Test
    void testSheetLoading() throws Exception {
        String spreadsheetId = "1q3-iEbNfAur8HB9zlW6-cw04KgdqWZznIi7Ublil5UI";
        
        System.out.println("Testing sheet loading for spreadsheet: " + spreadsheetId);
        
        try {
            Map<String, List<Cell>> allSheets = reader.readAllSheets(spreadsheetId);
            
            System.out.println("Successfully loaded " + allSheets.size() + " sheets:");
            
            for (Map.Entry<String, List<Cell>> entry : allSheets.entrySet()) {
                String sheetName = entry.getKey();
                List<Cell> cells = entry.getValue();
                System.out.println("  Sheet: '" + sheetName + "' - " + cells.size() + " cells");
                
                // Show first few cells
                for (int i = 0; i < Math.min(3, cells.size()); i++) {
                    Cell cell = cells.get(i);
                    System.out.println("    " + cell.getSheetName() + "!" + 
                                     (char)('A' + cell.getColumn() - 1) + cell.getRow() + 
                                     ": " + cell.getValue());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error loading sheets: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 