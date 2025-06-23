package com.superjoin.spreadsheet.model;

import java.util.Objects;

/**
 * Represents a sheet node in the spreadsheet knowledge graph.
 * This class represents a single sheet (tab) in the spreadsheet
 * and can contain multiple cells.
 */
public class SheetNode implements GraphNode {
    private final String sheetId;
    private final String name;

    public SheetNode(String sheetId, String name) {
        this.sheetId = sheetId;
        this.name = name;
    }

    // GraphNode interface implementation
    @Override
    public String getId() {
        return sheetId;
    }

    @Override
    public String getType() {
        return "SHEET";
    }

    @Override
    public String getDisplayName() {
        return name + " (" + sheetId + ")";
    }

    // Getters
    public String getSheetId() { return sheetId; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SheetNode sheetNode = (SheetNode) o;
        return Objects.equals(sheetId, sheetNode.sheetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheetId);
    }

    @Override
    public String toString() {
        return "SheetNode{" +
                "sheetId='" + sheetId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
} 