package ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LogViewer provides a UI component for viewing and searching system logs
 * Separated from main UI for better modularity
 */
public class LogViewer extends BorderPane {
    
    private ComboBox<String> logTypeCombo;
    private ComboBox<String> logFileCombo;
    private TextField searchField;
    private TextArea logContentArea;
    private Label statusLabel;
    private Button openExternalBtn;
    
    private static final String LOG_DIRECTORY = "logs";
    
    public LogViewer() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refreshLogFiles();
    }
    
    private void initializeComponents() {
        // Log type selector
        logTypeCombo = new ComboBox<>();
        logTypeCombo.getItems().addAll("All Logs", "AGV Logs", "Task Logs", "Environment Logs");
        logTypeCombo.setValue("All Logs");
        logTypeCombo.setPrefWidth(150);
        
        // Log file selector
        logFileCombo = new ComboBox<>();
        logFileCombo.setPromptText("Select a log file...");
        logFileCombo.setPrefWidth(300);
        
        // Search field
        searchField = new TextField();
        searchField.setPromptText(" Search logs (e.g., AGV-001, Task-5, CHARGING)");
        searchField.setPrefWidth(350);
        
        // Buttons
        Button refreshBtn = new Button(" Refresh");
        refreshBtn.setOnAction(e -> refreshLogFiles());
        
        Button searchBtn = new Button(" Search");
        searchBtn.setOnAction(e -> performSearch());
        
        Button clearSearchBtn = new Button(" Clear");
        clearSearchBtn.setOnAction(e -> clearSearch());
        
        openExternalBtn = new Button(" Open in External App");
        openExternalBtn.setDisable(true);
        openExternalBtn.setOnAction(e -> openInExternalApp());
        
        // Log content area
        logContentArea = new TextArea();
        logContentArea.setEditable(false);
        logContentArea.setWrapText(true);
        logContentArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        
        // Status label
        statusLabel = new Label(" Select a log type and file to view");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
    }
    
    private void setupLayout() {
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(10));
        topPanel.setStyle("-fx-background-color: #f5f5f5;");
        
        // First row: Log type and file selection
        HBox selectionRow = new HBox(10);
        selectionRow.getChildren().addAll(
            new Label("Log Type:"), logTypeCombo,
            new Label("Log File:"), logFileCombo
        );
        
        // Second row: Search controls
        HBox searchRow = new HBox(10);
        Button refreshBtn = new Button(" Refresh");
        refreshBtn.setOnAction(e -> refreshLogFiles());
        
        Button searchBtn = new Button(" Search");
        searchBtn.setOnAction(e -> performSearch());
        
        Button clearSearchBtn = new Button(" Clear");
        clearSearchBtn.setOnAction(e -> clearSearch());
        
        searchRow.getChildren().addAll(
            searchField, searchBtn, clearSearchBtn, refreshBtn, openExternalBtn
        );
        
        topPanel.getChildren().addAll(selectionRow, searchRow, statusLabel);
        
        // Set layout
        setTop(topPanel);
        setCenter(logContentArea);
    }
    
    private void setupEventHandlers() {
        // Filter log files when type changes
        logTypeCombo.setOnAction(e -> filterLogFiles());
        
        // Load log content when file is selected
        logFileCombo.setOnAction(e -> loadLogFile());
        
        // Search on Enter key
        searchField.setOnAction(e -> performSearch());
    }
    
    private void refreshLogFiles() {
        logFileCombo.getItems().clear();
        
        File logDir = new File(LOG_DIRECTORY);
        if (!logDir.exists() || !logDir.isDirectory()) {
            statusLabel.setText(" Log directory not found. Logs will be created when system runs.");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }
        
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (logFiles == null || logFiles.length == 0) {
            statusLabel.setText(" No log files found. Run the system to generate logs.");
            statusLabel.setStyle("-fx-text-fill: #666;");
            return;
        }
        
        // Sort files by last modified (newest first)
        java.util.Arrays.sort(logFiles, (f1, f2) -> 
            Long.compare(f2.lastModified(), f1.lastModified()));
        
        for (File file : logFiles) {
            logFileCombo.getItems().add(file.getName());
        }
        
        filterLogFiles();
        statusLabel.setText(" Found " + logFiles.length + " log file(s)");
        statusLabel.setStyle("-fx-text-fill: green;");
    }
    
    private void filterLogFiles() {
        String selectedType = logTypeCombo.getValue();
        List<String> allFiles = new ArrayList<>(logFileCombo.getItems());
        
        logFileCombo.getItems().clear();
        
        for (String fileName : allFiles) {
            boolean shouldShow = false;
            
            switch (selectedType) {
                case "All Logs":
                    shouldShow = true;
                    break;
                case "AGV Logs":
                    shouldShow = fileName.startsWith("agv_log_");
                    break;
                case "Task Logs":
                    shouldShow = fileName.startsWith("task_log_");
                    break;
                case "Environment Logs":
                    shouldShow = fileName.startsWith("environment_log_");
                    break;
            }
            
            if (shouldShow) {
                logFileCombo.getItems().add(fileName);
            }
        }
        
        if (!logFileCombo.getItems().isEmpty()) {
            logFileCombo.setValue(logFileCombo.getItems().get(0));
        }
    }
    
    private void loadLogFile() {
        String selectedFile = logFileCombo.getValue();
        if (selectedFile == null) {
            return;
        }
        
        try {
            Path logPath = Paths.get(LOG_DIRECTORY, selectedFile);
            String content = Files.readString(logPath);
            logContentArea.setText(content);
            
            // Count lines
            long lineCount = content.lines().count();
            statusLabel.setText(" Loaded: " + selectedFile + " (" + lineCount + " lines)");
            statusLabel.setStyle("-fx-text-fill: green;");
            
            openExternalBtn.setDisable(false);
            
        } catch (IOException e) {
            logContentArea.setText(" Error loading log file: " + e.getMessage());
            statusLabel.setText(" Failed to load file");
            statusLabel.setStyle("-fx-text-fill: red;");
            openExternalBtn.setDisable(true);
        }
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            statusLabel.setText(" Enter a search term");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }
        
        String selectedFile = logFileCombo.getValue();
        if (selectedFile == null) {
            statusLabel.setText(" Select a log file first");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }
        
        try {
            Path logPath = Paths.get(LOG_DIRECTORY, selectedFile);
            List<String> allLines = Files.readAllLines(logPath);
            
            // Case-insensitive search
            String searchLower = searchTerm.toLowerCase();
            List<String> matchingLines = allLines.stream()
                .filter(line -> line.toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
            
            if (matchingLines.isEmpty()) {
                logContentArea.setText(" No results found for: \"" + searchTerm + "\"");
                statusLabel.setText(" 0 matches found");
                statusLabel.setStyle("-fx-text-fill: orange;");
            } else {
                StringBuilder result = new StringBuilder();
                result.append(" Search Results for: \"").append(searchTerm).append("\"\n");
                result.append("=".repeat(80)).append("\n\n");
                
                for (String line : matchingLines) {
                    result.append(line).append("\n");
                }
                
                logContentArea.setText(result.toString());
                statusLabel.setText(" Found " + matchingLines.size() + " matching line(s)");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
            
        } catch (IOException e) {
            logContentArea.setText(" Error searching log file: " + e.getMessage());
            statusLabel.setText(" Search failed");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void clearSearch() {
        searchField.clear();
        loadLogFile(); // Reload original content
    }
    
    private void openInExternalApp() {
        String selectedFile = logFileCombo.getValue();
        if (selectedFile == null) {
            return;
        }
        
        try {
            File logFile = new File(LOG_DIRECTORY, selectedFile);
            
            // Use Desktop API to open with default text editor
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (logFile.exists()) {
                    desktop.open(logFile);
                    statusLabel.setText(" Opened in external application");
                    statusLabel.setStyle("-fx-text-fill: green;");
                }
            } else {
                // Fallback: Show file location
                statusLabel.setText(" File location: " + logFile.getAbsolutePath());
                statusLabel.setStyle("-fx-text-fill: blue;");
            }
            
        } catch (IOException e) {
            statusLabel.setText(" Cannot open file: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}