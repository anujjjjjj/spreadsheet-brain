package com.superjoin.spreadsheet;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for the Spreadsheet Brain application.
 * Provides a command-line interface for analyzing spreadsheet dependencies.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    private static String spreadsheetId;
    private static String sheetName = "Sheet1";
    private static String projectId;
    private static String location = "us-central1";
    private static String geminiApiKey;
    private static boolean liveSync = false;
    private static volatile long lastModifiedTime = -1;
    private static Thread liveSyncThread;

    private static SpreadsheetGraph graph;
    private static GeminiQueryService queryService;

    public static void main(String[] args) {
        try {
            parseArguments(args);
            initializeServices();
            loadSpreadsheet();
            startInteractiveMode();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            logger.error("Application error", e);
            System.exit(1);
        }
    }

    /**
     * Parses command line arguments
     */
    private static void parseArguments(String[] args) {
        if (args.length < 2) {
            showUsage();
            System.exit(1);
        }
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s":
                case "--spreadsheet":
                    if (i + 1 < args.length) {
                        spreadsheetId = args[++i];
                    } else {
                        System.err.println("Error: Missing spreadsheet ID");
                        System.exit(1);
                    }
                    break;
                case "--sheet":
                    if (i + 1 < args.length) {
                        sheetName = args[++i];
                    } else {
                        System.err.println("Error: Missing sheet name");
                        System.exit(1);
                    }
                    break;
                case "--project-id":
                    if (i + 1 < args.length) {
                        projectId = args[++i];
                    } else {
                        System.err.println("Error: Missing project ID");
                        System.exit(1);
                    }
                    break;
                case "--location":
                    if (i + 1 < args.length) {
                        location = args[++i];
                    } else {
                        System.err.println("Error: Missing location");
                        System.exit(1);
                    }
                    break;
                case "--gemini-api-key":
                    if (i + 1 < args.length) {
                        geminiApiKey = args[++i];
                    } else {
                        System.err.println("Error: Missing Gemini API key");
                        System.exit(1);
                    }
                    break;
                case "--live-sync":
                    liveSync = true;
                    break;
                case "-h":
                case "--help":
                    showUsage();
                    System.exit(0);
                    break;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    showUsage();
                    System.exit(1);
            }
        }
        
        if (spreadsheetId == null || projectId == null) {
            System.err.println("Error: Spreadsheet ID and Project ID are required");
            showUsage();
            System.exit(1);
        }
        
        if (geminiApiKey == null) {
            System.out.println("Warning: No Gemini API key provided. Using fallback rule-based query processing.");
        }
    }

    /**
     * Shows usage information
     */
    private static void showUsage() {
        System.out.println("Spreadsheet Brain - A knowledge graph engine for spreadsheets");
        System.out.println();
        System.out.println("Usage: java -jar spreadsheet-brain.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -s, --spreadsheet <id>    Google Spreadsheet ID (required)");
        System.out.println("  --sheet <name>            Sheet name (default: Sheet1)");
        System.out.println("  --project-id <id>         Google Cloud Project ID (required)");
        System.out.println("  --location <location>     Google Cloud location (default: us-central1)");
        System.out.println("  --gemini-api-key <key>     Gemini API key (required)");
        System.out.println("  --live-sync               Enable live sync mode");
        System.out.println("  -h, --help                Show this help message");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -jar spreadsheet-brain.jar -s 1QjtoAguM5as_G2_FuHOEVoLT3ro_7hfsZENngQk5UTU --project-id my-project --gemini-api-key my-gemini-api-key");
    }

    /**
     * Initializes the core services
     */
    private static void initializeServices() throws IOException, GeneralSecurityException {
        System.out.println("Initializing Spreadsheet Brain...");
        logger.info("Initializing Spreadsheet Brain services");
        
        // Initialize the graph service
        graph = new SpreadsheetGraph();
        
        // Initialize the query service
        if (geminiApiKey != null) {
            queryService = new GeminiQueryService(geminiApiKey);
        } else {
            queryService = new GeminiQueryService("fallback");
        }
        
        System.out.println("Services initialized successfully.");
        logger.info("Services initialized successfully");
    }

    /**
     * Loads the specified spreadsheet
     */
    private static void loadSpreadsheet() throws IOException {
        System.out.println("Loading spreadsheet: " + spreadsheetId);
        System.out.println("Sheet: " + sheetName);
        logger.info("Loading spreadsheet: {}, sheet: {}", spreadsheetId, sheetName);
        
        // Load all sheets for multi-sheet support
        graph.loadAllSheets(spreadsheetId);
        
        // Update last modified time
        lastModifiedTime = getSpreadsheetLastModifiedTime();
        
        System.out.println("Spreadsheet loaded successfully!");
        System.out.println(graph.getGraphSummary());
        logger.info("Spreadsheet loaded successfully");
    }

    /**
     * Starts the interactive command mode
     */
    public static void startInteractiveMode() {
        if (liveSync) {
            startLiveSyncThread();
        }
        System.out.println("\n=== Spreadsheet Brain Interactive Mode ===");
        System.out.println("Available commands:");
        System.out.println("  impact <cell>     - Analyze impact of changing a cell");
        System.out.println("  deps <cell>       - Show dependencies of a cell");
        System.out.println("  ask <question>    - Ask a natural language question");
        System.out.println("  formulas          - List all formula cells");
        System.out.println("  summary           - Show graph summary");
        System.out.println("  help              - Show this help");
        System.out.println("  quit              - Exit the application");
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("spreadsheet-brain> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }
            
            if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }
            
            try {
                processCommand(input);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                logger.error("Error processing command: {}", input, e);
            }
        }
    }

    /**
     * Processes a command from the user
     */
    private static void processCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        
        switch (cmd) {
            case "impact":
                if (parts.length < 2) {
                    System.out.println("Usage: impact <cell>");
                    System.out.println("Example: impact A1");
                    return;
                }
                handleImpactCommand(parts[1]);
                break;
            case "deps":
                if (parts.length < 2) {
                    System.out.println("Usage: deps <cell>");
                    System.out.println("Example: deps A1");
                    return;
                }
                handleDepsCommand(parts[1]);
                break;
            case "ask":
                if (parts.length < 2) {
                    System.out.println("Usage: ask <question>");
                    System.out.println("Example: ask what cells depend on A1");
                    return;
                }
                handleAskCommand(parts[1]);
                break;
            case "formulas":
                handleFormulasCommand();
                break;
            case "summary":
                handleSummaryCommand();
                break;
            default:
                System.out.println("Unknown command: " + cmd);
                System.out.println("Type 'help' for available commands.");
        }
    }

    /**
     * Handles the impact command
     */
    private static void handleImpactCommand(String cellRef) {
        System.out.println("Analyzing impact of " + cellRef + "...");
        Set<String> affectedCells = graph.analyzeImpact(cellRef);
        
        if (affectedCells.isEmpty()) {
            System.out.println("No cells are affected by changes to " + cellRef);
        } else {
            System.out.println("Cells affected by " + cellRef + ":");
            for (String cell : affectedCells) {
                System.out.println("  " + cell);
            }
        }
    }

    /**
     * Handles the deps command
     */
    private static void handleDepsCommand(String cellRef) {
        System.out.println("Analyzing dependencies of " + cellRef + "...");
        Set<String> dependencies = graph.findDependencies(cellRef);
        
        if (dependencies.isEmpty()) {
            System.out.println("No dependencies found for " + cellRef);
        } else {
            System.out.println("Dependencies of " + cellRef + ":");
            for (String cell : dependencies) {
                System.out.println("  " + cell);
            }
        }
    }

    /**
     * Handles the ask command
     */
    private static void handleAskCommand(String question) {
        System.out.println("Processing question: " + question);
        logger.info("Processing question: {}", question);
        
        // Translate the natural language query
        GeminiQueryService.QueryResult queryResult = queryService.translateQuery(question, graph);
        
        if (!"success".equals(queryResult.getStatus()) && !queryResult.getStatus().contains("success")) {
            System.out.println("Error translating query: " + queryResult.getStatus());
            return;
        }
        
        // Execute the structured query
        GeminiQueryService.QueryExecutionResult result = queryService.executeQuery(
            queryResult.getStructuredQuery(), graph);
        
        if (result.isSuccess()) {
            System.out.println("Answer:");
            System.out.println(result.getMessage());
            
            // Display results if available
            if (result.getData() instanceof Set) {
                Set<?> data = (Set<?>) result.getData();
                if (!data.isEmpty()) {
                    System.out.println("Results:");
                    for (Object item : data) {
                        System.out.println("  " + item);
                    }
                }
            }
        } else {
            System.out.println("Error: " + result.getMessage());
        }
    }

    /**
     * Handles the formulas command
     */
    private static void handleFormulasCommand() {
        var formulaCells = graph.getFormulaCells();
        if (formulaCells.isEmpty()) {
            System.out.println("No formulas found in the spreadsheet.");
        } else {
            System.out.println("Formula cells:");
            for (var cell : formulaCells) {
                System.out.println("  " + cell.getFullReference() + ": " + cell.getFormula());
            }
        }
    }

    /**
     * Handles the summary command
     */
    private static void handleSummaryCommand() {
        System.out.println(graph.getGraphSummary());
    }

    /**
     * Shows help information
     */
    private static void showHelp() {
        System.out.println("Available commands:");
        System.out.println("  impact <cell>     - Analyze impact of changing a cell");
        System.out.println("  deps <cell>       - Show dependencies of a cell");
        System.out.println("  ask <question>    - Ask a natural language question");
        System.out.println("  formulas          - List all formula cells");
        System.out.println("  summary           - Show graph summary");
        System.out.println("  help              - Show this help");
        System.out.println("  quit              - Exit the application");
        System.out.println();
        System.out.println("Example questions:");
        System.out.println("  ask find all cells with formulas");
        System.out.println("  ask show me all formulas");
    }

    private static void startLiveSyncThread() {
        liveSyncThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // 30 seconds
                    long newModifiedTime = getSpreadsheetLastModifiedTime();
                    if (newModifiedTime > lastModifiedTime) {
                        System.out.println("\n[Live Sync] Spreadsheet updated. Reloading...");
                        logger.info("Live sync detected changes, reloading spreadsheet");
                        loadSpreadsheet();
                        lastModifiedTime = newModifiedTime;
                        System.out.print("spreadsheet-brain> ");
                    }
                } catch (Exception e) {
                    System.err.println("[Live Sync] Error: " + e.getMessage());
                    logger.error("Live sync error", e);
                }
            }
        });
        liveSyncThread.setDaemon(true);
        liveSyncThread.start();
        logger.info("Live sync thread started");
    }

    private static long getSpreadsheetLastModifiedTime() {
        try {
            // Use SheetsReader to get spreadsheet metadata
            return SheetsReader.getSpreadsheetLastModifiedTime(spreadsheetId);
        } catch (Exception e) {
            System.err.println("[Live Sync] Could not fetch last modified time: " + e.getMessage());
            logger.error("Could not fetch last modified time", e);
            return lastModifiedTime;
        }
    }
} 