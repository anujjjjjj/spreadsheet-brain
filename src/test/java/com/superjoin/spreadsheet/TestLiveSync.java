package com.superjoin.spreadsheet;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Simple test class for live sync functionality
 */
public class TestLiveSync {
    
    public static void main(String[] args) {
        try {
            System.out.println("Testing Live Sync Functionality...");
            
            // Test with a public spreadsheet that doesn't require authentication
            String spreadsheetId = "YOUR_SPREADSHEET_ID";
            
            // Test the last modified time functionality
            System.out.println("Testing getSpreadsheetLastModifiedTime...");
            long lastModified = SheetsReader.getSpreadsheetLastModifiedTime(spreadsheetId);
            System.out.println("Last modified time: " + lastModified + " (epoch millis)");
            System.out.println("Last modified time: " + new java.util.Date(lastModified));
            
            // Test that we can get the time multiple times
            System.out.println("\nTesting multiple calls...");
            long lastModified2 = SheetsReader.getSpreadsheetLastModifiedTime(spreadsheetId);
            System.out.println("Second call - Last modified time: " + lastModified2);
            
            if (lastModified == lastModified2) {
                System.out.println("✓ Live sync timestamp detection working correctly");
            } else {
                System.out.println("⚠ Timestamps differ - this might indicate changes or API inconsistency");
            }
            
            System.out.println("\nLive sync test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error during live sync test: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 