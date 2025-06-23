package com.superjoin.spreadsheet;

import com.superjoin.spreadsheet.model.CellNode;
import com.superjoin.spreadsheet.model.GraphNode;
import com.superjoin.spreadsheet.model.SheetNode;
import com.superjoin.spreadsheet.services.KnowledgeGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class that coordinates between the SheetsReader and KnowledgeGraphService.
 * This class builds the knowledge graph from spreadsheet data and handles formula parsing.
 */
public class SpreadsheetGraph {
    private static final Logger logger = LoggerFactory.getLogger(SpreadsheetGraph.class);
    
    private final SheetsReader reader;
    private final KnowledgeGraphService graphService;
    private String currentSpreadsheetId;
    private String currentSheetName;
    private Map<String, String> sheetNames = new HashMap<>(); // Track all loaded sheets

    // Regex pattern to match cell references in formulas (e.g., A1, B2, Sheet1!A1, C:C, D:D)
    private static final Pattern CELL_REFERENCE_PATTERN = Pattern.compile(
        "([A-Za-z]+[0-9]+)|([A-Za-z]+![A-Za-z]+[0-9]+)|([A-Za-z]+![A-Za-z]+:[A-Za-z]+)|([A-Za-z]+:[A-Za-z]+)"
    );
    
    // Simpler pattern for cross-sheet references
    private static final Pattern CROSS_SHEET_PATTERN = Pattern.compile("([A-Za-z0-9_]+)!([A-Za-z0-9:]+)");

    public SpreadsheetGraph() throws IOException, GeneralSecurityException {
        this.reader = new SheetsReader();
        this.graphService = new KnowledgeGraphService();
    }

    /**
     * Loads all sheets from a spreadsheet
     */
    public void loadAllSheets(String spreadsheetId) throws IOException {
        logger.info("Loading all sheets from spreadsheet: {}", spreadsheetId);
        
        this.currentSpreadsheetId = spreadsheetId;
        
        // Clear existing graph
        graphService.clear();
        
        try {
            Map<String, List<Cell>> allSheets = reader.readAllSheets(spreadsheetId);
            logger.info("Retrieved {} sheets from reader", allSheets.size());
            
            boolean firstSheet = true;
            for (Map.Entry<String, List<Cell>> entry : allSheets.entrySet()) {
                String sheetName = entry.getKey();
                List<Cell> cells = entry.getValue();
                
                logger.info("Starting to process sheet: {} with {} cells", sheetName, cells.size());
                
                // Set the first sheet as the current sheet for backward compatibility
                if (firstSheet) {
                    this.currentSheetName = sheetName;
                    firstSheet = false;
                    logger.info("Set current sheet to: {}", sheetName);
                }
                
                logger.info("Processing sheet: {} with {} cells", sheetName, cells.size());
                loadSheetData(sheetName, cells);
                
                // Show progress after each sheet
                var totalCells = graphService.getCellNodes().size();
                logger.info("After processing sheet '{}': total cells in graph = {}", sheetName, totalCells);
            }
            
            // Build cross-sheet dependencies
            logger.info("Building cross-sheet dependencies...");
            buildCrossSheetDependencies();
            
            logger.info("Successfully loaded {} sheets", allSheets.size());
            logger.info("Final graph state: {} total cells", graphService.getCellNodes().size());
            
        } catch (Exception e) {
            throw new IOException("Failed to read sheets: " + e.getMessage(), e);
        }
    }

    /**
     * Loads a specific sheet (backward compatibility)
     */
    public void loadSpreadsheet(String spreadsheetId, String sheetName) throws IOException {
        logger.info("Loading spreadsheet: {}, sheet: {}", spreadsheetId, sheetName);
        
        this.currentSpreadsheetId = spreadsheetId;
        this.currentSheetName = sheetName;
        
        // Clear existing graph
        graphService.clear();
        
        // Create sheet node
        SheetNode sheetNode = new SheetNode(sheetName, sheetName);
        graphService.addNode(sheetNode);
        
        // Read spreadsheet data as a list of cells
        List<Cell> cells;
        try {
            cells = reader.readSheet(spreadsheetId, sheetName);
        } catch (Exception e) {
            throw new IOException("Failed to read sheet: " + e.getMessage(), e);
        }
        
        loadSheetData(sheetName, cells);
    }

    /**
     * Loads data for a specific sheet
     */
    private void loadSheetData(String sheetName, List<Cell> cells) {
        SheetNode sheetNode = new SheetNode(sheetName, sheetName);
        graphService.addNode(sheetNode);
        sheetNames.put(sheetName, sheetName);
        logger.info("Loading {} cells for sheet: {}", cells.size(), sheetName);
        int processedCells = 0;
        int addedCells = 0;
        int logged = 0;
        for (Cell cell : cells) {
            processedCells++;
            // Add ALL cells for debugging
            CellNode cellNode = new CellNode(sheetName, cell.getRow(), cell.getColumn(), cell.getValue(), cell.getFormula());
            graphService.addNode(cellNode);
            addedCells++;
            if (logged < 10) {
                logger.info("Added cell: {} with ID: {} (value='{}' formula='{}')", cellNode.getA1Notation(), cellNode.getId(), cellNode.getValue(), cellNode.getFormula());
                logged++;
            }
            graphService.addEdge(sheetName, cellNode.getId(), KnowledgeGraphService.CONTAINS_EDGE);
            if (cell.hasFormula()) {
                Set<String> dependencies = parseFormulaDependencies(cell.getFormula(), sheetName);
                for (String dependency : dependencies) {
                    CellNode dependentCell = graphService.findCellByA1Notation(sheetName, dependency);
                    if (dependentCell != null) {
                        graphService.addEdge(cellNode.getId(), dependentCell.getId(), KnowledgeGraphService.DEPENDS_ON_EDGE);
                    }
                }
            }
        }
        logger.info("Finished loading sheet: {} - processed {} cells, added {} cells to graph", sheetName, processedCells, addedCells);
        
        // Debug: Show some cell IDs that should be searchable
        var allCells = graphService.getCellNodes();
        logger.info("Total cells in graph: {}", allCells.size());
        if (allCells.size() > 0) {
            logger.info("Sample cell IDs (first 10):");
            allCells.stream().limit(10).forEach(c -> 
                logger.info("  Cell ID: '{}' (A1: {})", c.getId(), c.getA1Notation()));
        }
    }

    /**
     * Builds cross-sheet dependencies by analyzing formulas that reference other sheets
     */
    private void buildCrossSheetDependencies() {
        List<CellNode> formulaCells = graphService.getFormulaCells();
        logger.info("Building cross-sheet dependencies for {} formula cells", formulaCells.size());
        
        for (CellNode cellNode : formulaCells) {
            if (cellNode.getFormula() != null) {
                logger.info("Processing formula cell: {} with formula: {}", cellNode.getId(), cellNode.getFormula());
                parseCrossSheetDependencies(cellNode, cellNode.getFormula());
            }
        }
        logger.info("Finished building cross-sheet dependencies");
    }

    /**
     * Parses cross-sheet dependencies in formulas (e.g., Sheet2!A1, Sheet2!A:B)
     */
    private void parseCrossSheetDependencies(CellNode cellNode, String formula) {
        // Use the simpler pattern for cross-sheet references
        Matcher matcher = CROSS_SHEET_PATTERN.matcher(formula);
        
        logger.info("Parsing cross-sheet dependencies for {} with formula: {}", cellNode.getId(), formula);
        
        boolean foundAny = false;
        while (matcher.find()) {
            foundAny = true;
            String referencedSheet = matcher.group(1);
            String referencedRange = matcher.group(2);
            
            logger.info("Found cross-sheet reference: {}!{}", referencedSheet, referencedRange);
            
            // Handle column ranges like A:B or C:C
            if (referencedRange.contains(":")) {
                // This is a column range, find all cells in that range
                String[] parts = referencedRange.split(":");
                if (parts.length == 2) {
                    String startCol = parts[0];
                    String endCol = parts[1];
                    
                    logger.info("Processing column range: {} to {}", startCol, endCol);
                    
                    // Find all cells in the referenced sheet that are in this column range
                    List<CellNode> allCellsInSheet = graphService.getCellNodes().stream()
                        .filter(cell -> cell.getSheetId().equals(referencedSheet))
                        .filter(cell -> {
                            String cellCol = cell.getA1Notation().replaceAll("[0-9]+", "");
                            boolean inRange = cellCol.compareTo(startCol) >= 0 && cellCol.compareTo(endCol) <= 0;
                            logger.info("Cell {} column {} in range {} to {}: {}", cell.getId(), cellCol, startCol, endCol, inRange);
                            return inRange;
                        })
                        .collect(java.util.stream.Collectors.toList());
                    
                    logger.info("Found {} cells in column range {}:{} for sheet {}", allCellsInSheet.size(), startCol, endCol, referencedSheet);
                    
                    for (CellNode referencedCell : allCellsInSheet) {
                        graphService.addEdge(cellNode.getId(), referencedCell.getId(), KnowledgeGraphService.DEPENDS_ON_EDGE);
                        logger.info("Cross-sheet dependency: {} depends on {} (column range)", cellNode.getId(), referencedCell.getId());
                    }
                }
            } else {
                // This is a single cell reference
                CellNode referencedCellNode = graphService.findCellByA1Notation(referencedSheet, referencedRange);
                
                if (referencedCellNode != null) {
                    // Add dependency edge
                    graphService.addEdge(cellNode.getId(), referencedCellNode.getId(), KnowledgeGraphService.DEPENDS_ON_EDGE);
                    logger.info("Cross-sheet dependency: {} depends on {}", cellNode.getId(), referencedCellNode.getId());
                } else {
                    logger.warn("Cross-sheet reference not found: {}!{}", referencedSheet, referencedRange);
                }
            }
        }
        
        if (!foundAny) {
            logger.info("No cross-sheet references found in formula: {}", formula);
        }
    }

    /**
     * Builds dependency edges by parsing formulas
     */
    private void buildFormulaDependencies() {
        List<CellNode> formulaCells = graphService.getFormulaCells();
        
        for (CellNode cellNode : formulaCells) {
            String formula = cellNode.getFormula();
            if (formula != null && !formula.trim().isEmpty()) {
                Set<String> dependencies = parseFormulaDependencies(formula, cellNode.getSheetId());
                
                for (String dependency : dependencies) {
                    // Find the dependent cell node
                    CellNode dependentCell = graphService.findCellByA1Notation(
                        cellNode.getSheetId(), dependency);
                    
                    if (dependentCell != null) {
                        // Add DEPENDS_ON edge from formula cell to dependent cell
                        graphService.addEdge(
                            cellNode.getId(),
                            dependentCell.getId(),
                            KnowledgeGraphService.DEPENDS_ON_EDGE
                        );
                    }
                }
            }
        }
    }

    /**
     * Parses a formula to extract cell references
     */
    private Set<String> parseFormulaDependencies(String formula, String sheetName) {
        Set<String> dependencies = new java.util.HashSet<>();
        Matcher matcher = CELL_REFERENCE_PATTERN.matcher(formula);
        
        logger.info("Parsing formula dependencies for sheet '{}': {}", sheetName, formula);
        
        while (matcher.find()) {
            String reference = matcher.group();
            logger.info("Found reference: {}", reference);
            
            // Handle different reference formats
            if (reference.contains("!")) {
                // Cross-sheet reference (e.g., Sheet1!A1) - skip for now, handled by parseCrossSheetDependencies
                logger.info("Skipping cross-sheet reference in parseFormulaDependencies: {}", reference);
            } else {
                // Same sheet reference (e.g., A1, C:C, D:D)
                if (reference.contains(":")) {
                    // Column range like C:C or D:D
                    logger.info("Found column range reference: {}", reference);
                    // For now, just add the range as a dependency - the cross-sheet parser will handle it
                    dependencies.add(reference);
                } else {
                    // Single cell reference like A1
                    dependencies.add(reference);
                }
            }
        }
        
        logger.info("Parsed dependencies for sheet '{}': {}", sheetName, dependencies);
        return dependencies;
    }

    /**
     * Performs impact analysis on a cell
     */
    public Set<String> analyzeImpact(String cellReference) {
        // Parse cell reference
        String[] parts = cellReference.split("!");
        String sheetName = parts.length > 1 ? parts[0] : currentSheetName;
        String a1Notation = parts.length > 1 ? parts[1] : parts[0];
        
        logger.info("Looking for cell: sheetName='{}', a1Notation='{}'", sheetName, a1Notation);
        logger.info("Current sheet name: '{}'", currentSheetName);
        
        // Debug: List some available cells
        var allCells = graphService.getCellNodes();
        logger.info("Total cells in graph: {}", allCells.size());
        
        // Debug: Show cells from the specific sheet we're looking for
        var sheetCells = allCells.stream()
            .filter(cell -> cell.getSheetId().equals(sheetName))
            .collect(java.util.stream.Collectors.toList());
        logger.info("Cells in sheet '{}': {} cells", sheetName, sheetCells.size());
        
        if (sheetCells.size() > 0) {
            logger.info("First 5 cells in sheet '{}': {}", sheetName, 
                sheetCells.stream()
                    .limit(5)
                    .map(c -> c.getId())
                    .collect(java.util.stream.Collectors.toList()));
        }
        
        if (allCells.size() > 0) {
            logger.info("First 5 cells: {}", allCells.stream()
                .limit(5)
                .map(c -> c.getId())
                .collect(java.util.stream.Collectors.toList()));
        }
        
        // Debug: Show what we're searching for
        String searchId = sheetName + "!" + a1Notation;
        logger.info("Searching for cell ID: '{}'", searchId);
        
        CellNode cell = graphService.findCellByA1Notation(sheetName, a1Notation);
        if (cell == null) {
            logger.warn("Cell not found: {}", cellReference);
            logger.info("Available cell nodes (first 10): {}", graphService.getCellNodes().stream()
                .map(CellNode::getId)
                .limit(10)
                .collect(java.util.stream.Collectors.toList()));
            return new java.util.HashSet<>();
        }
        
        // Find all cells that depend on this cell (transitive dependents)
        Set<String> dependents = graphService.getTransitiveDependents(cell.getId());
        logger.info("Dependents set for {}: {}", cell.getId(), dependents);
        logger.info("Direct dependents (reverse edges) for {}: {}", cell.getId(), graphService.getDependents(cell.getId()));
        logger.info("Impact analysis for {}: {} direct dependents, {} total affected cells", 
                cellReference, graphService.getDependents(cell.getId()).size(), dependents.size());
        return dependents;
    }

    /**
     * Finds all cells that depend on a given cell
     */
    public Set<String> findDependents(String cellReference) {
        return analyzeImpact(cellReference);
    }

    /**
     * Finds all cells that a given cell depends on
     */
    public Set<String> findDependencies(String cellReference) {
        // Parse cell reference
        String[] parts = cellReference.split("!");
        String sheetName = parts.length > 1 ? parts[0] : currentSheetName;
        String a1Notation = parts.length > 1 ? parts[1] : parts[0];
        
        CellNode cell = graphService.findCellByA1Notation(sheetName, a1Notation);
        if (cell == null) {
            logger.warn("Cell not found: {}", cellReference);
            return new java.util.HashSet<>();
        }
        
        // Find all cells that this cell depends on (transitive dependencies)
        Set<String> dependencies = graphService.getTransitiveDependencies(cell.getId());
        
        logger.info("Dependency analysis for {}: {} direct dependencies, {} total dependency cells", 
                cellReference, graphService.getDependencies(cell.getId()).size(), dependencies.size());
        
        return dependencies;
    }

    /**
     * Gets all formula cells in the current sheet
     */
    public List<CellNode> getFormulaCells() {
        return graphService.getFormulaCells();
    }

    /**
     * Gets a summary of the current graph
     */
    public String getGraphSummary() {
        return graphService.getGraphSummary();
    }

    /**
     * Gets the current spreadsheet ID
     */
    public String getCurrentSpreadsheetId() {
        return currentSpreadsheetId;
    }

    /**
     * Gets the current sheet name
     */
    public String getCurrentSheetName() {
        return currentSheetName;
    }

    /**
     * Gets all loaded sheet names
     */
    public List<String> getSheetNames() {
        return new ArrayList<>(sheetNames.keySet());
    }

    /**
     * Gets the knowledge graph service for advanced operations
     */
    public KnowledgeGraphService getGraphService() {
        return graphService;
    }

    /**
     * Updates a cell value and refreshes the graph
     */
    public boolean updateCell(String cellReference, String newValue) {
        try {
            // Parse cell reference
            String[] parts = cellReference.split("!");
            String sheetName = parts.length > 1 ? parts[0] : currentSheetName;
            String a1Notation = parts.length > 1 ? parts[1] : parts[0];
            
            logger.info("Updating cell {} to: {}", cellReference, newValue);
            
            // Update the cell in Google Sheets
            boolean success = reader.updateCell(currentSpreadsheetId, sheetName, a1Notation, newValue);
            
            if (success) {
                // Reload the graph to reflect changes
                loadAllSheets(currentSpreadsheetId);
                logger.info("Successfully updated cell {} and refreshed graph", cellReference);
                return true;
            } else {
                logger.error("Failed to update cell {}", cellReference);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error updating cell {}: {}", cellReference, e.getMessage());
            return false;
        }
    }

    /**
     * Gets all cells in the graph
     */
    public List<Cell> getAllCells() {
        List<Cell> cells = new ArrayList<>();
        for (CellNode cellNode : graphService.getCellNodes()) {
            Cell cell = new Cell(cellNode.getRow(), cellNode.getColumn(), cellNode.getValue(), cellNode.getFormula());
            cell.setSheetName(cellNode.getSheetId()); // Set the sheet name from the CellNode
            cells.add(cell);
        }
        return cells;
    }
} 