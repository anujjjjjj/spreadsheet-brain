package com.superjoin.spreadsheet.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.superjoin.spreadsheet.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Service for reading data from Google Sheets.
 * Handles authentication and data retrieval from the Google Sheets API.
 */
public class SpreadsheetReader {
    private static final Logger logger = LoggerFactory.getLogger(SpreadsheetReader.class);
    
    private static final String APPLICATION_NAME = "Spreadsheet Brain";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/credentials.json";

    private final Sheets sheetsService;

    public SpreadsheetReader() throws IOException, GeneralSecurityException {
        this.sheetsService = createSheetsService();
    }

    /**
     * Creates and configures the Google Sheets service with proper authentication.
     */
    private Sheets createSheetsService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        Credential credential = getCredentials(HTTP_TRANSPORT);
        
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Gets user credentials for Google Sheets API access.
     * This will trigger OAuth2 flow if credentials don't exist.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Fetches all data from a specific sheet in the spreadsheet.
     * Returns a map of cell positions to Cell objects.
     */
    public Map<String, Cell> readSheetData(String spreadsheetId, String sheetName) throws IOException {
        logger.info("Reading data from sheet: {} in spreadsheet: {}", sheetName, spreadsheetId);
        
        String range = sheetName + "!A1:ZZ1000"; // Read up to 1000 rows and columns
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .setValueRenderOption("UNFORMATTED_VALUE")
                .setDateTimeRenderOption("FORMATTED_STRING")
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            logger.warn("No data found in sheet: {}", sheetName);
            return new HashMap<>();
        }
 
        Map<String, Cell> cells = new HashMap<>();
        
        // Process each row
        for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
            List<Object> row = values.get(rowIndex);
            if (row != null) {
                // Process each cell in the row`
                for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                    Object cellValue = row.get(colIndex);
                    if (cellValue != null) {
                        String value = cellValue.toString();
                        
                        // Check if this is a formula (starts with =)
                        String formula = value.startsWith("=") ? value : null;
                        String displayValue = formula != null ? "" : value; // Formulas don't have display value
                        
                        Cell cell = new Cell(rowIndex + 1, colIndex + 1, displayValue, formula);
                        String key = (rowIndex + 1) + "," + (colIndex + 1);
                        cells.put(key, cell);
                    }
                }
            }
        }

        logger.info("Read {} cells from sheet: {}", cells.size(), sheetName);
        return cells;
    }

    /**
     * Gets the list of sheet names in the spreadsheet.
     */
    public List<String> getSheetNames(String spreadsheetId) throws IOException {
        logger.info("Getting sheet names for spreadsheet: {}", spreadsheetId);
        
        var spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        List<String> sheetNames = new ArrayList<>();
        
        for (var sheet : spreadsheet.getSheets()) {
            sheetNames.add(sheet.getProperties().getTitle());
        }
        
        logger.info("Found {} sheets: {}", sheetNames.size(), sheetNames);
        return sheetNames;
    }

    /**
     * Fetches formulas for a specific range in the sheet.
     * This is used to get the actual formulas when we need them.
     */
    public Map<String, String> getFormulas(String spreadsheetId, String sheetName, String range) throws IOException {
        logger.info("Getting formulas for range: {} in sheet: {}", range, sheetName);
        
        String fullRange = sheetName + "!" + range;
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, fullRange)
                .setValueRenderOption("FORMULA")
                .execute();

        List<List<Object>> values = response.getValues();
        Map<String, String> formulas = new HashMap<>();
        
        if (values != null) {
            for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                List<Object> row = values.get(rowIndex);
                if (row != null) {
                    for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                        Object cellValue = row.get(colIndex);
                        if (cellValue != null) {
                            String value = cellValue.toString();
                            if (value.startsWith("=")) {
                                String key = (rowIndex + 1) + "," + (colIndex + 1);
                                formulas.put(key, value);
                            }
                        }
                    }
                }
            }
        }
        
        logger.info("Found {} formulas in range: {}", formulas.size(), range);
        return formulas;
    }
}
