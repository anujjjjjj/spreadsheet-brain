package com.superjoin.spreadsheet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for integrating with Google's Gemini AI for natural language query processing.
 * This service translates natural language queries into structured operations on the knowledge graph.
 */
public class GeminiQueryService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiQueryService.class);
    
    private final String apiKey;
    private final OkHttpClient client;
    private final Gson gson;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public GeminiQueryService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        this.gson = new Gson();
        
        if ("fallback".equals(apiKey)) {
            logger.info("Using fallback rule-based query processing (no AI integration)");
        }
    }

    /**
     * Translates a natural language query into a structured query
     */
    public QueryResult translateQuery(String query, SpreadsheetGraph graph) {
        try {
            if ("fallback".equals(apiKey) || apiKey == null || apiKey.trim().isEmpty()) {
                // Use fallback rule-based processing
                return fallbackQueryProcessing(query, graph);
            }
            
            String context = buildContext(graph);
            String prompt = buildTranslationPrompt(query, context);
            
            String response = callGeminiAPI(prompt);
            String structuredQuery = extractResponseText(response);
            
            return new QueryResult(structuredQuery, "success");
        } catch (Exception e) {
            logger.error("Error translating query: " + e.getMessage());
            // Fall back to rule-based processing when API call fails
            logger.info("Falling back to rule-based query processing");
            return fallbackQueryProcessing(query, graph);
        }
    }

    /**
     * Fallback rule-based query processing when no API key is available
     */
    private QueryResult fallbackQueryProcessing(String query, SpreadsheetGraph graph) {
        query = query.toLowerCase();
        logger.info("fallbackQueryProcessing - processing query: '{}'", query);
        
        // Sheet listing queries
        if (query.contains("sheets") || query.contains("show me all sheets")) {
            logger.info("fallbackQueryProcessing - matched sheet listing query");
            return new QueryResult(createListSheetsQuery(), "success (fallback)");
        }
        
        // Sheet-specific cell queries
        if (query.contains("cells in") && query.contains("sheet")) {
            String sheetName = extractSheetName(query);
            logger.info("fallbackQueryProcessing - extracted sheet name: '{}'", sheetName);
            if (sheetName != null) {
                return new QueryResult(createSheetSpecificFindCellsQuery(sheetName), "success (fallback)");
            }
        }
        
        // Date-related queries
        if (query.contains("date") || query.contains("dates")) {
            return new QueryResult(createFindCellsQuery("date", "Find cells containing dates"), "success (fallback)");
        }
        
        // Financial data queries
        if (query.contains("financial") || query.contains("revenue") || query.contains("cost") || query.contains("money")) {
            return new QueryResult(createFindCellsQuery("financial", "Find cells with financial data"), "success (fallback)");
        }
        
        // Impact analysis queries
        if (query.contains("impact") || query.contains("affected") || query.contains("depend")) {
            String cellRef = extractCellReference(query);
            if (cellRef != null) {
                return new QueryResult(createImpactQuery(cellRef), "success (fallback)");
            }
        }
        
        // Dependency queries
        if (query.contains("depend on") || query.contains("uses") || query.contains("references")) {
            String cellRef = extractCellReference(query);
            if (cellRef != null) {
                return new QueryResult(createDependencyQuery(cellRef), "success (fallback)");
            }
        }
        
        // Formula queries
        if (query.contains("formula") || query.contains("formulas")) {
            return new QueryResult(createFormulaQuery(), "success (fallback)");
        }
        
        // Cell update queries
        if (query.contains("set") || query.contains("update") || query.contains("change")) {
            String cellRef = extractCellReference(query);
            String newValue = extractNewValue(query);
            if (cellRef != null && newValue != null) {
                return new QueryResult(createUpdateQuery(cellRef, newValue), "success (fallback)");
            }
        }
        
        // Default to impact analysis if no specific pattern is found
        String cellRef = extractCellReference(query);
        if (cellRef != null) {
            return new QueryResult(createImpactQuery(cellRef), "success (fallback)");
        }
        
        logger.warn("fallbackQueryProcessing - no pattern matched for query: '{}'", query);
        return new QueryResult(createErrorQuery("Could not understand query: " + query), "error (fallback)");
    }

    /**
     * Extracts cell reference from natural language query
     */
    private String extractCellReference(String query) {
        // Simple regex to find cell references like A1, B2, etc.
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("([A-Z]+[0-9]+)");
        java.util.regex.Matcher matcher = pattern.matcher(query.toUpperCase());
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }

    /**
     * Creates a structured impact analysis query
     */
    private String createImpactQuery(String cellRef) {
        Map<String, Object> query = new HashMap<>();
        query.put("command", "impact_analysis");
        query.put("target_cell", cellRef);
        query.put("description", "Find all cells affected by changing a target cell");
        
        return gson.toJson(query);
    }

    /**
     * Creates a structured dependency query
     */
    private String createDependencyQuery(String cellRef) {
        Map<String, Object> query = new HashMap<>();
        query.put("command", "dependency_analysis");
        query.put("target_cell", cellRef);
        query.put("description", "Find all cells that " + cellRef + " depends on");
        
        return gson.toJson(query);
    }

    /**
     * Creates a structured formula query
     */
    private String createFormulaQuery() {
        Map<String, Object> query = new HashMap<>();
        query.put("command", "list_formulas");
        query.put("description", "List all cells containing formulas");
        
        return gson.toJson(query);
    }

    /**
     * Creates an error query
     */
    private String createErrorQuery(String error) {
        Map<String, Object> query = new HashMap<>();
        query.put("command", "error");
        query.put("error", error);
        
        return gson.toJson(query);
    }

    /**
     * Extracts new value from natural language query
     */
    private String extractNewValue(String query) {
        // Simple patterns to extract values
        if (query.contains("to ")) {
            String[] parts = query.split("to ");
            if (parts.length > 1) {
                String value = parts[1].trim();
                // Remove common words that might follow the value
                value = value.replaceAll("\\s+(formula|value|cell|number|text).*", "");
                return value;
            }
        }
        
        // Look for numbers or quoted strings
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
        java.util.regex.Matcher numberMatcher = numberPattern.matcher(query);
        if (numberMatcher.find()) {
            return numberMatcher.group();
        }
        
        return null;
    }

    /**
     * Creates a structured update query
     */
    private String createUpdateQuery(String cellRef, String newValue) {
        Map<String, Object> query = new HashMap<>();
        query.put("command", "update_cell");
        query.put("target_cell", cellRef);
        query.put("new_value", newValue);
        query.put("description", "Update cell " + cellRef + " to " + newValue);
        
        return gson.toJson(query);
    }

    /**
     * Creates a structured find cells query
     */
    private String createFindCellsQuery(String criteria, String description) {
        Map<String, Object> query = new HashMap<>();
        query.put("command", "find_cells");
        query.put("criteria", criteria);
        query.put("description", description);
        
        return gson.toJson(query);
    }

    /**
     * Creates a list sheets query
     */
    private String createListSheetsQuery() {
        Map<String, Object> query = new HashMap<>();
        query.put("command", "list_sheets");
        query.put("description", "List all sheets in the spreadsheet");
        
        return gson.toJson(query);
    }

    /**
     * Creates a sheet-specific find cells query
     */
    private String createSheetSpecificFindCellsQuery(String sheetName) {
        Map<String, Object> query = new HashMap<>();
        query.put("command", "find_cells");
        query.put("sheet_name", sheetName);
        query.put("description", "Show cells in the " + sheetName + " sheet");
        
        return gson.toJson(query);
    }

    /**
     * Extracts sheet name from natural language query
     */
    private String extractSheetName(String query) {
        // Pattern to match various sheet-specific query formats, including multi-word sheet names
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:cells?\\s+in\\s+(?:the\\s+)?|show\\s+me\\s+cells?\\s+(?:in|from)\\s+(?:the\\s+)?|list\\s+cells?\\s+in\\s+(?:the\\s+)?|all\\s+cells?\\s+in\\s+(?:the\\s+)?)([A-Za-z0-9 _-]+?)(?:\\s+sheet)?[.?!]?$");
        java.util.regex.Matcher matcher = pattern.matcher(query.toLowerCase());
        
        if (matcher.find()) {
            String sheetName = matcher.group(1).trim();
            if (sheetName != null && !sheetName.isEmpty()) {
                // Capitalize each word to match sheet names
                String[] words = sheetName.split(" ");
                StringBuilder sb = new StringBuilder();
                for (String word : words) {
                    if (!word.isEmpty()) {
                        sb.append(Character.toUpperCase(word.charAt(0)));
                        if (word.length() > 1) sb.append(word.substring(1).toLowerCase());
                        sb.append(" ");
                    }
                }
                return sb.toString().trim();
            }
        }
        return null;
    }

    /**
     * Makes an HTTP call to the Gemini API
     */
    private String callGeminiAPI(String prompt) throws IOException {
        String url = GEMINI_API_URL + "?key=" + apiKey;
        
        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        
        // Create the parts array properly
        JsonObject[] partsArray = {textPart};
        content.add("parts", gson.toJsonTree(partsArray));
        
        // Create the contents array properly
        JsonObject[] contentsArray = {content};
        requestBody.add("contents", gson.toJsonTree(contentsArray));
        
        RequestBody body = RequestBody.create(
            requestBody.toString(), 
            MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new IOException("Unexpected response: " + response + "\nError body: " + errorBody);
            }
            return response.body().string();
        }
    }

    /**
     * Extracts the text response from Gemini API response
     */
    private String extractResponseText(String apiResponse) {
        try {
            JsonObject response = JsonParser.parseString(apiResponse).getAsJsonObject();
            JsonObject candidates = response.getAsJsonArray("candidates").get(0).getAsJsonObject();
            JsonObject content = candidates.getAsJsonObject("content");
            JsonObject part = content.getAsJsonArray("parts").get(0).getAsJsonObject();
            String rawText = part.get("text").getAsString();
            
            // Clean the response - extract JSON from the text
            return extractJsonFromText(rawText);
        } catch (Exception e) {
            logger.error("Error parsing API response: " + e.getMessage());
            logger.error("Raw response: " + apiResponse);
            return "{\"command\": \"error\", \"error\": \"Failed to parse API response\"}";
        }
    }

    /**
     * Extracts JSON from AI response text, handling cases where AI adds extra text
     */
    private String extractJsonFromText(String text) {
        try {
            // Try to find JSON object in the text
            int startBrace = text.indexOf('{');
            int endBrace = text.lastIndexOf('}');
            
            if (startBrace != -1 && endBrace != -1 && endBrace > startBrace) {
                String jsonText = text.substring(startBrace, endBrace + 1);
                
                // Validate it's proper JSON by parsing it
                JsonParser.parseString(jsonText);
                return jsonText;
            }
            
            // If no JSON found, create a fallback response
            return createFallbackResponse(text);
        } catch (Exception e) {
            logger.error("Error extracting JSON from text: " + e.getMessage());
            return createFallbackResponse(text);
        }
    }

    /**
     * Creates a fallback response when JSON parsing fails
     */
    private String createFallbackResponse(String text) {
        // Try to understand the intent from the text
        text = text.toLowerCase();
        
        if (text.contains("impact") || text.contains("affected")) {
            return "{\"command\": \"impact_analysis\", \"description\": \"Fallback impact analysis\"}";
        } else if (text.contains("depend") || text.contains("dependency")) {
            return "{\"command\": \"dependency_analysis\", \"description\": \"Fallback dependency analysis\"}";
        } else if (text.contains("formula")) {
            return "{\"command\": \"list_formulas\", \"description\": \"Fallback formula list\"}";
        } else {
            return "{\"command\": \"error\", \"error\": \"Could not parse AI response: " + text.replace("\"", "\\\"") + "\"}";
        }
    }

    /**
     * Builds context about the current spreadsheet for the AI
     */
    private String buildContext(SpreadsheetGraph graph) {
        StringBuilder context = new StringBuilder();
        context.append("Spreadsheet: ").append(graph.getCurrentSpreadsheetId()).append("\n");
        context.append("Sheet: ").append(graph.getCurrentSheetName()).append("\n");
        context.append("Graph Summary: ").append(graph.getGraphSummary()).append("\n");
        
        // Add information about formula cells
        var formulaCells = graph.getFormulaCells();
        context.append("Formula cells: ").append(formulaCells.size()).append("\n");
        
        // Add some example formulas for context
        if (!formulaCells.isEmpty()) {
            context.append("Example formulas:\n");
            for (int i = 0; i < Math.min(5, formulaCells.size()); i++) {
                var cell = formulaCells.get(i);
                context.append("  ").append(cell.getFullReference()).append(": ").append(cell.getFormula()).append("\n");
            }
        }
        
        return context.toString();
    }

    /**
     * Builds a prompt for translating natural language to structured queries
     */
    private String buildTranslationPrompt(String query, String context) {
        return String.format(
            "You are an AI assistant that helps analyze spreadsheet knowledge graphs. " +
            "Given a natural language query, translate it into a structured JSON query.\n\n" +
            "Context:\n%s\n\n" +
            "User Query: %s\n\n" +
            "Available commands:\n" +
            "- list_sheets: List all sheets in the spreadsheet\n" +
            "- impact_analysis: Find cells affected by changing a target cell\n" +
            "- dependency_analysis: Find cells that a target cell depends on\n" +
            "- list_formulas: List all cells containing formulas\n" +
            "- find_cells: Find cells matching certain criteria (can be sheet-specific)\n" +
            "- update_cell: Update a cell's value or formula\n\n" +
            "IMPORTANT: Respond with ONLY a valid JSON object. Do not include any other text, explanations, or formatting.\n\n" +
            "JSON format:\n" +
            "{\n" +
            "    \"command\": \"command_name\",\n" +
            "    \"target_cell\": \"cell_reference\" (if applicable),\n" +
            "    \"new_value\": \"value_to_set\" (for update_cell),\n" +
            "    \"sheet_name\": \"sheet_name\" (for sheet-specific queries),\n" +
            "    \"description\": \"what this query does\"\n" +
            "}\n\n" +
            "Examples:\n" +
            "- \"show me all sheets\" -> {\"command\": \"list_sheets\", \"description\": \"List all sheets in the spreadsheet\"}\n" +
            "- \"show me cells in the Deals sheet\" -> {\"command\": \"find_cells\", \"sheet_name\": \"Deals\", \"description\": \"Show cells in the Deals sheet\"}\n" +
            "- \"what cells depend on A1\" -> {\"command\": \"dependency_analysis\", \"target_cell\": \"A1\", \"description\": \"Find all cells that A1 depends on\"}\n" +
            "- \"show me all formulas\" -> {\"command\": \"list_formulas\", \"description\": \"List all cells containing formulas\"}\n" +
            "- \"what is the impact of changing B2\" -> {\"command\": \"impact_analysis\", \"target_cell\": \"B2\", \"description\": \"Find all cells affected by changing B2\"}\n" +
            "- \"find all cells that reference revenue\" -> {\"command\": \"find_cells\", \"description\": \"Find cells containing revenue references\"}\n" +
            "- \"set cell A1 to 100\" -> {\"command\": \"update_cell\", \"target_cell\": \"A1\", \"new_value\": \"100\", \"description\": \"Update cell A1 to value 100\"}\n" +
            "- \"change B2 formula to =A1*2\" -> {\"command\": \"update_cell\", \"target_cell\": \"B2\", \"new_value\": \"=A1*2\", \"description\": \"Update cell B2 with formula =A1*2\"}\n\n" +
            "JSON response:", context, query);
    }

    /**
     * Executes a structured query against the knowledge graph
     */
    public QueryExecutionResult executeQuery(String structuredQuery, SpreadsheetGraph graph) {
        try {
            JsonObject queryJson = JsonParser.parseString(structuredQuery).getAsJsonObject();
            
            // Check if command field exists
            if (!queryJson.has("command") || queryJson.get("command") == null) {
                return new QueryExecutionResult(false, "Invalid query: missing 'command' field", null);
            }
            
            String command = queryJson.get("command").getAsString();
            if (command == null || command.trim().isEmpty()) {
                return new QueryExecutionResult(false, "Invalid query: empty 'command' field", null);
            }
            
            switch (command) {
                case "impact_analysis":
                    return executeImpactAnalysis(queryJson, graph);
                case "dependency_analysis":
                    return executeDependencyAnalysis(queryJson, graph);
                case "list_formulas":
                    return executeListFormulas(queryJson, graph);
                case "list_sheets":
                    return executeListSheets(queryJson, graph);
                case "find_cells":
                    return executeFindCells(queryJson, graph);
                case "update_cell":
                    return executeUpdateCell(queryJson, graph);
                case "error":
                    String errorMsg = queryJson.has("error") && queryJson.get("error") != null ? 
                        queryJson.get("error").getAsString() : "Unknown error";
                    return new QueryExecutionResult(false, errorMsg, null);
                default:
                    return new QueryExecutionResult(false, "Unknown command: " + command, null);
            }
        } catch (Exception e) {
            logger.error("Error executing query: {}", e.getMessage());
            return new QueryExecutionResult(false, "Error executing query: " + e.getMessage(), null);
        }
    }

    /**
     * Executes impact analysis
     */
    private QueryExecutionResult executeImpactAnalysis(JsonObject query, SpreadsheetGraph graph) {
        if (!query.has("target_cell") || query.get("target_cell") == null) {
            return new QueryExecutionResult(false, "Impact analysis requires a target cell", null);
        }
        
        String targetCell = query.get("target_cell").getAsString();
        if (targetCell == null || targetCell.trim().isEmpty()) {
            return new QueryExecutionResult(false, "Target cell cannot be empty", null);
        }
        
        var result = graph.analyzeImpact(targetCell);
        
        StringBuilder message = new StringBuilder();
        message.append("Impact analysis for ").append(targetCell).append(": ").append(result.size()).append(" cells affected\n");
        
        if (!result.isEmpty()) {
            message.append("Cells affected:\n");
            for (String cellId : result) {
                message.append("  ").append(cellId).append("\n");
            }
        } else {
            message.append("No cells are affected by changes to ").append(targetCell);
        }
        
        return new QueryExecutionResult(true, message.toString(), result);
    }

    /**
     * Executes dependency analysis
     */
    private QueryExecutionResult executeDependencyAnalysis(JsonObject query, SpreadsheetGraph graph) {
        if (!query.has("target_cell") || query.get("target_cell") == null) {
            return new QueryExecutionResult(false, "Dependency analysis requires a target cell", null);
        }
        
        String targetCell = query.get("target_cell").getAsString();
        if (targetCell == null || targetCell.trim().isEmpty()) {
            return new QueryExecutionResult(false, "Target cell cannot be empty", null);
        }
        
        var result = graph.findDependencies(targetCell);
        
        StringBuilder message = new StringBuilder();
        message.append("Dependency analysis for ").append(targetCell).append(": ").append(result.size()).append(" dependencies\n");
        
        if (!result.isEmpty()) {
            message.append("Dependencies:\n");
            for (String cellId : result) {
                message.append("  ").append(cellId).append("\n");
            }
        } else {
            message.append("No dependencies found for ").append(targetCell);
        }
        
        return new QueryExecutionResult(true, message.toString(), result);
    }

    /**
     * Executes list formulas
     */
    private QueryExecutionResult executeListFormulas(JsonObject query, SpreadsheetGraph graph) {
        var formulaCells = graph.getFormulaCells();
        
        StringBuilder result = new StringBuilder();
        result.append("Found ").append(formulaCells.size()).append(" cells with formulas:\n");
        
        for (int i = 0; i < formulaCells.size(); i++) {
            var cell = formulaCells.get(i);
            result.append(i + 1).append(". ").append(cell.getId())
                  .append(": ").append(cell.getFormula())
                  .append("\n");
        }
        
        return new QueryExecutionResult(true, result.toString(), formulaCells);
    }

    /**
     * Executes find cells (new feature)
     */
    private QueryExecutionResult executeFindCells(JsonObject query, SpreadsheetGraph graph) {
        try {
            String description = query.has("description") ? query.get("description").getAsString() : "";
            String sheetName = query.has("sheet_name") ? query.get("sheet_name").getAsString() : null;
            
            logger.info("executeFindCells - description: '{}', sheetName: '{}'", description, sheetName);
            
            // Get all cells from the graph
            var allCells = graph.getAllCells();
            logger.info("executeFindCells - total cells in graph: {}", allCells.size());
            
            // Debug: Show unique sheet names in the graph
            var uniqueSheetNames = allCells.stream()
                .map(cell -> cell.getSheetName())
                .filter(name -> name != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            logger.info("executeFindCells - unique sheet names in graph: {}", uniqueSheetNames);
            
            // Filter by sheet if specified
            if (sheetName != null && !sheetName.trim().isEmpty()) {
                allCells = allCells.stream()
                    .filter(cell -> cell.getSheetName() != null && cell.getSheetName().equalsIgnoreCase(sheetName))
                    .toList();
                logger.info("executeFindCells - after sheet filtering: {} cells for sheet '{}'", allCells.size(), sheetName);
            }
            
            // If sheet name is specified, show cells from that sheet (regardless of description)
            if (sheetName != null && !sheetName.trim().isEmpty()) {
                var sheetCells = allCells.stream()
                    .filter(cell -> cell.getSheetName() != null && cell.getSheetName().equalsIgnoreCase(sheetName))
                    .limit(20) // Show more cells for sheet-specific queries
                    .toList();
                
                logger.info("executeFindCells - sheetCells found: {} cells", sheetCells.size());
                
                StringBuilder result = new StringBuilder();
                result.append("Cells in sheet '").append(sheetName).append("':\n");
                for (int i = 0; i < Math.min(sheetCells.size(), 20); i++) {
                    Cell cell = sheetCells.get(i);
                    result.append(i + 1).append(". ").append(getA1Notation(cell.getRow(), cell.getColumn()))
                          .append(": ").append(cell.getValue() != null ? cell.getValue() : "(empty)")
                          .append("\n");
                }
                if (sheetCells.size() > 20) {
                    result.append("... and ").append(sheetCells.size() - 20).append(" more cells");
                }
                
                return new QueryExecutionResult(true, result.toString(), sheetCells);
            }
            
            // Apply description-based filtering for non-sheet-specific queries
            if (description.toLowerCase().contains("july") || description.toLowerCase().contains("date")) {
                // Filter cells that might contain date-related data
                var filteredCells = allCells.stream()
                    .filter(cell -> cell.getValue() != null && 
                        (cell.getValue().toLowerCase().contains("july") || 
                         cell.getValue().toLowerCase().contains("date") ||
                         cell.getValue().toLowerCase().contains("2024")))
                    .limit(10) // Limit results
                    .toList();
                
                return new QueryExecutionResult(true, 
                    "Found " + filteredCells.size() + " cells containing date-related data", 
                    filteredCells);
            } else if (description.toLowerCase().contains("revenue") || description.toLowerCase().contains("money")) {
                // Filter cells that might contain financial data
                var filteredCells = allCells.stream()
                    .filter(cell -> cell.getValue() != null && 
                        (cell.getValue().toLowerCase().contains("revenue") || 
                         cell.getValue().toLowerCase().contains("$") ||
                         cell.getValue().toLowerCase().contains("price")))
                    .limit(10)
                    .toList();
                
                return new QueryExecutionResult(true, 
                    "Found " + filteredCells.size() + " cells containing financial data", 
                    filteredCells);
            } else if (description.toLowerCase().contains("formula") || description.toLowerCase().contains("formulas")) {
                // Filter cells that contain formulas
                var formulaCells = graph.getFormulaCells();
                
                StringBuilder result = new StringBuilder();
                result.append("Found ").append(formulaCells.size()).append(" cells with formulas:\n");
                
                for (int i = 0; i < formulaCells.size(); i++) {
                    var cell = formulaCells.get(i);
                    result.append(i + 1).append(". ").append(cell.getId())
                          .append(": ").append(cell.getFormula())
                          .append("\n");
                }
                
                return new QueryExecutionResult(true, result.toString(), formulaCells);
            } else {
                // Default: return first few cells from all sheets
                var sampleCells = allCells.stream()
                    .limit(10)
                    .toList();
                
                return new QueryExecutionResult(true, 
                    "Found " + sampleCells.size() + " sample cells across all sheets (use more specific search terms)", 
                    sampleCells);
            }
        } catch (Exception e) {
            logger.error("Error in executeFindCells", e);
            return new QueryExecutionResult(false, 
                "Error in find_cells: " + e.getMessage(), 
                null);
        }
    }

    /**
     * Converts row and column numbers to A1 notation
     */
    private String getA1Notation(int row, int column) {
        StringBuilder result = new StringBuilder();
        
        // Convert column number to letter(s)
        while (column > 0) {
            column--;
            result.insert(0, (char) ('A' + column % 26));
            column /= 26;
        }
        
        // Add row number
        result.append(row);
        
        return result.toString();
    }

    /**
     * Executes update cell
     */
    private QueryExecutionResult executeUpdateCell(JsonObject query, SpreadsheetGraph graph) {
        if (!query.has("target_cell") || query.get("target_cell") == null) {
            return new QueryExecutionResult(false, "Update cell requires a target cell", null);
        }
        
        if (!query.has("new_value") || query.get("new_value") == null) {
            return new QueryExecutionResult(false, "Update cell requires a new value", null);
        }
        
        String targetCell = query.get("target_cell").getAsString();
        String newValue = query.get("new_value").getAsString();
        
        if (targetCell == null || targetCell.trim().isEmpty()) {
            return new QueryExecutionResult(false, "Target cell cannot be empty", null);
        }
        
        if (newValue == null) {
            return new QueryExecutionResult(false, "New value cannot be null", null);
        }
        
        boolean success = graph.updateCell(targetCell, newValue);
        
        return new QueryExecutionResult(success, 
            "Cell " + targetCell + " updated to " + newValue, 
            null);
    }

    /**
     * Executes list sheets
     */
    private QueryExecutionResult executeListSheets(JsonObject query, SpreadsheetGraph graph) {
        List<String> sheets = graph.getSheetNames();
        
        StringBuilder result = new StringBuilder();
        result.append("Sheets in this spreadsheet:\n");
        for (int i = 0; i < sheets.size(); i++) {
            result.append(i + 1).append(". ").append(sheets.get(i)).append("\n");
        }
        
        return new QueryExecutionResult(true, result.toString(), sheets);
    }

    /**
     * Result of query translation
     */
    public static class QueryResult {
        private final String structuredQuery;
        private final String status;

        public QueryResult(String structuredQuery, String status) {
            this.structuredQuery = structuredQuery;
            this.status = status;
        }

        public String getStructuredQuery() { return structuredQuery; }
        public String getStatus() { return status; }
    }

    /**
     * Result of query execution
     */
    public static class QueryExecutionResult {
        private final boolean success;
        private final String message;
        private final Object data;

        public QueryExecutionResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
} 