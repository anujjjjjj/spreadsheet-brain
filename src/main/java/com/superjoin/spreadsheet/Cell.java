package com.superjoin.spreadsheet;

/**
 * Simple data structure representing a cell from Google Sheets.
 * This class is used to store raw cell data before it's converted
 * to a CellNode in the knowledge graph.
 */
public class Cell {
    private final int row;
    private final int column;
    private final String value;
    private final String formula;
    private String sheetName;

    public Cell(int row, int column, String value, String formula) {
        this.row = row;
        this.column = column;
        this.value = value;
        this.formula = formula;
    }

    // Getters
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public String getValue() { return value; }
    public String getFormula() { return formula; }
    public String getSheetName() { return sheetName; }

    // Setters
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }

    /**
     * Checks if this cell contains a formula
     */
    public boolean hasFormula() {
        return formula != null && !formula.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Cell{" +
                "row=" + row +
                ", column=" + column +
                ", value='" + value + '\'' +
                ", formula='" + formula + '\'' +
                '}';
    }
} 