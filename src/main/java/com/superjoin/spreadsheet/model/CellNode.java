package com.superjoin.spreadsheet.model;

import java.util.Objects;

/**
 * Represents a cell node in the spreadsheet knowledge graph.
 * This class encapsulates all the properties of a single cell including
 * its position, value, formula, and A1 notation.
 */
public class CellNode implements GraphNode {
    private final String sheetId;
    private final int row;
    private final int column;
    private String value;
    private String formula;
    private final String a1Notation;

    public CellNode(String sheetId, int row, int column, String value, String formula) {
        this.sheetId = sheetId;
        this.row = row;
        this.column = column;
        this.value = value;
        this.formula = formula;
        this.a1Notation = convertToA1Notation(row, column);
    }

    /**
     * Converts row and column numbers to A1 notation (e.g., A1, B2, etc.)
     * This is the standard way to reference cells in spreadsheets.
     */
    private String convertToA1Notation(int row, int column) {
        StringBuilder result = new StringBuilder();
        
        // Convert column number to letter (1=A, 2=B, 27=AA, etc.)
        int col = column;
        while (col > 0) {
            col--;
            result.insert(0, (char) ('A' + col % 26));
            col /= 26;
        }
        
        // Add row number
        result.append(row);
        return result.toString();
    }

    // GraphNode interface implementation
    @Override
    public String getId() {
        return getFullReference();
    }

    @Override
    public String getType() {
        return "CELL";
    }

    @Override
    public String getDisplayName() {
        return getFullReference() + (hasFormula() ? " (formula)" : "");
    }

    // Getters
    public String getSheetId() { return sheetId; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public String getValue() { return value; }
    public String getFormula() { return formula; }
    public String getA1Notation() { return a1Notation; }

    // Setters for mutable properties
    public void setValue(String value) { this.value = value; }
    public void setFormula(String formula) { this.formula = formula; }

    /**
     * Returns the full cell reference including sheet name (e.g., "Sheet1!A1")
     */
    public String getFullReference() {
        return sheetId + "!" + a1Notation;
    }

    /**
     * Checks if this cell contains a formula
     */
    public boolean hasFormula() {
        return formula != null && !formula.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellNode cellNode = (CellNode) o;
        return row == cellNode.row && 
               column == cellNode.column && 
               Objects.equals(sheetId, cellNode.sheetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheetId, row, column);
    }

    @Override
    public String toString() {
        return "CellNode{" +
                "sheetId='" + sheetId + '\'' +
                ", a1Notation='" + a1Notation + '\'' +
                ", value='" + value + '\'' +
                ", formula='" + formula + '\'' +
                '}';
    }
}
