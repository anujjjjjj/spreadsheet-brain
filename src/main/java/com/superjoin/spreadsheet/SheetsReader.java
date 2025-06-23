package com.superjoin.spreadsheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class SheetsReader {
    private static final Logger logger = LoggerFactory.getLogger(SheetsReader.class);

    private static final String APPLICATION_NAME = "Spreadsheet Brain";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final List<String> DRIVE_SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, com.google.api.services.drive.DriveScopes.DRIVE_METADATA_READONLY);

    private Credential getCredentials() throws Exception {
        InputStream in = SheetsReader.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private Credential getDriveCredentials() throws Exception {
        InputStream in = SheetsReader.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, DRIVE_SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public List<Cell> readSheet(String spreadsheetId, String sheetName) throws Exception {
        Sheets service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();

        Spreadsheet response = service.spreadsheets().get(spreadsheetId)
                .setIncludeGridData(true)
                .execute();

        List<Cell> cells = new ArrayList<>();

        response.getSheets().forEach(sheet -> {
            String currentSheetName = sheet.getProperties().getTitle();
            if (!currentSheetName.equals(sheetName)) return;
            for (GridData gridData : sheet.getData()) {
                int rowIndex = 0;
                if (gridData.getRowData() == null) continue;
                for (RowData rowData : gridData.getRowData()) {
                    int colIndex = 0;
                    if (rowData.getValues() != null) {
                        for (CellData cellData : rowData.getValues()) {
                            String value = cellData.getFormattedValue() != null ? cellData.getFormattedValue() : null;
                            String formula = (cellData.getUserEnteredValue() != null && cellData.getUserEnteredValue().getFormulaValue() != null)
                                ? cellData.getUserEnteredValue().getFormulaValue() : null;
                            Cell cell = new Cell(rowIndex + 1, colIndex + 1, value, formula);
                            cells.add(cell);
                            colIndex++;
                        }
                    }
                    rowIndex++;
                }
            }
        });
        return cells;
    }

    /**
     * Reads all sheets from a spreadsheet
     */
    public Map<String, List<Cell>> readAllSheets(String spreadsheetId) throws Exception {
        Sheets service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();

        logger.info("Fetching spreadsheet: {}", spreadsheetId);
        Spreadsheet response = service.spreadsheets().get(spreadsheetId)
                .setIncludeGridData(true)
                .execute();

        Map<String, List<Cell>> allSheets = new HashMap<>();
        
        logger.info("Found {} sheets in spreadsheet", response.getSheets().size());
        response.getSheets().forEach(sheet -> {
            String sheetName = sheet.getProperties().getTitle();
            logger.info("Processing sheet: {}", sheetName);
        });

        response.getSheets().forEach(sheet -> {
            String sheetName = sheet.getProperties().getTitle();
            List<Cell> cells = new ArrayList<>();
            logger.info("Fetching range for sheet: {}", sheetName);
            
            try {
                logger.info("Sheet {} has {} grid data sections", sheetName, sheet.getData().size());
                for (GridData gridData : sheet.getData()) {
                    int rowIndex = 0;
                    if (gridData.getRowData() == null) {
                        logger.info("Sheet {} grid data has no row data", sheetName);
                        continue;
                    }
                    logger.info("Sheet {} has {} rows", sheetName, gridData.getRowData().size());
                    for (RowData rowData : gridData.getRowData()) {
                        int colIndex = 0;
                        if (rowData.getValues() != null) {
                            for (CellData cellData : rowData.getValues()) {
                                String value = cellData.getFormattedValue() != null ? cellData.getFormattedValue() : null;
                                String formula = (cellData.getUserEnteredValue() != null && cellData.getUserEnteredValue().getFormulaValue() != null)
                                    ? cellData.getUserEnteredValue().getFormulaValue() : null;
                                
                                Cell cell = new Cell(rowIndex + 1, colIndex + 1, value, formula);
                                cell.setSheetName(sheetName);
                                cells.add(cell);
                                colIndex++;
                            }
                        }
                        rowIndex++;
                    }
                }
                logger.info("Loaded {} cells for sheet: {}", cells.size(), sheetName);
                for (int i = 0; i < Math.min(5, cells.size()); i++) {
                    Cell c = cells.get(i);
                    logger.info("Cell {}{}: value='{}' formula='{}'", (char)('A'+c.getColumn()-1), c.getRow(), c.getValue(), c.getFormula());
                }
            } catch (Exception e) {
                logger.error("Error fetching data for sheet {}: {}", sheetName, e.getMessage());
                e.printStackTrace();
            }
            allSheets.put(sheetName, cells);
        });
        
        logger.info("Total sheets loaded: {}", allSheets.size());
        allSheets.keySet().forEach(sheetName -> {
            logger.info("Sheet '{}': {} cells", sheetName, allSheets.get(sheetName).size());
        });
        
        return allSheets;
    }

    /**
     * Updates a cell's value in the Google Sheet
     */
    public boolean updateCell(String spreadsheetId, String sheetName, String cellReference, String newValue) {
        try {
            Sheets service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials())
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Create the value range for the update
            List<List<Object>> values = Arrays.asList(Arrays.asList(newValue));
            ValueRange body = new ValueRange().setValues(values);

            // Update the cell
            UpdateValuesResponse result = service.spreadsheets().values()
                    .update(spreadsheetId, sheetName + "!" + cellReference, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

            logger.info("Updated {} cells", result.getUpdatedCells());
            return true;
        } catch (Exception e) {
            logger.error("Error updating cell: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Returns the last modified time of the spreadsheet (epoch millis)
     */
    public static long getSpreadsheetLastModifiedTime(String spreadsheetId) throws Exception {
        SheetsReader reader = new SheetsReader();
        com.google.api.services.drive.Drive driveService = new com.google.api.services.drive.Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, reader.getDriveCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();
        com.google.api.services.drive.model.File file = driveService.files().get(spreadsheetId)
                .setFields("modifiedTime")
                .execute();
        return file.getModifiedTime().getValue(); // returns epoch millis
    }
} 