package managers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemLogger {
    private static SystemLogger instance;
    private AGVLog agvLog;
    private TaskLog taskLog;
    private EnvironmentLog environmentLog;
    private String logDirectory;
    private String currentDate; // Track current date for daily log rotation
    
    private SystemLogger() {
        logDirectory = "logs";
        createLogDirectory();
        
        // Date-based filename: task_2024-11-13.txt
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = dateFormat.format(new Date());
        
        // Initialize all log subsystems with date-based filenames
        agvLog = new AGVLog(logDirectory, currentDate);
        taskLog = new TaskLog(logDirectory, currentDate);
        environmentLog = new EnvironmentLog(logDirectory, currentDate);
        
        System.out.println("📂 Log files initialized in '" + logDirectory + "/' directory");
        System.out.println("📅 Log date: " + currentDate);
    }
    
    public static synchronized SystemLogger getInstance() {
        if (instance == null) {
            instance = new SystemLogger();
        }
        return instance;
    }
    
    private void createLogDirectory() {
        java.io.File dir = new java.io.File(logDirectory);
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println("📂 Created logs directory");
        }
    }
    
    /**
     * Check if date has changed and rotate logs if necessary
     * Call this periodically (e.g., at the start of each day)
     */
    public synchronized void checkAndRotateLogs() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String newDate = dateFormat.format(new Date());
        
        if (!newDate.equals(currentDate)) {
            System.out.println("📅 Date changed from " + currentDate + " to " + newDate);
            System.out.println("🔄 Rotating log files...");
            
            // Close current logs
            closeLogFiles();
            
            // Update current date
            currentDate = newDate;
            
            // Create new logs with new date
            agvLog = new AGVLog(logDirectory, currentDate);
            taskLog = new TaskLog(logDirectory, currentDate);
            environmentLog = new EnvironmentLog(logDirectory, currentDate);
            
            System.out.println("✅ Log rotation complete");
        }
    }
    
    // ========== AGV LOGGING METHODS ==========
    public void logAGVMovement(String agvId, String from, String to) {
        checkAndRotateLogs();
        agvLog.logMovement(agvId, from, to);
    }
    
    public void logAGVBattery(String agvId, double batteryLevel, String action) {
        checkAndRotateLogs();
        agvLog.logBattery(agvId, batteryLevel, action);
    }
    
    public void logAGVStatusChange(String agvId, String oldStatus, String newStatus) {
        checkAndRotateLogs();
        agvLog.logStatusChange(agvId, oldStatus, newStatus);
    }
    
    public void logAGVChargingStart(String agvId, String stationId, double batteryLevel) {
        checkAndRotateLogs();
        agvLog.logChargingStart(agvId, stationId, batteryLevel);
    }
    
    public void logAGVChargingComplete(String agvId, double initialBattery) {
        checkAndRotateLogs();
        agvLog.logChargingComplete(agvId, initialBattery);
    }
    
    // ========== TASK LOGGING METHODS ==========
    public void logTaskCreated(int taskId, String taskName, int priority) {
        checkAndRotateLogs();
        taskLog.logTaskCreated(taskId, taskName, priority);
    }
    
    public void logTaskAssigned(int taskId, String agvId) {
        checkAndRotateLogs();
        taskLog.logTaskAssigned(taskId, agvId);
    }
    
    public void logTaskStarted(int taskId, String taskName, String agvId) {
        checkAndRotateLogs();
        taskLog.logTaskStarted(taskId, taskName, agvId);
    }
    
    public void logTaskCompleted(int taskId, String status) {
        checkAndRotateLogs();
        taskLog.logTaskCompleted(taskId, status);
    }
    
    // ========== ENVIRONMENT LOGGING METHODS ==========
    public void logTemperatureAlert(String zoneName, double temperature) {
        checkAndRotateLogs();
        environmentLog.logTemperatureAlert(zoneName, temperature);
    }
    
    public void logItemTransport(String itemName, String from, String to) {
        checkAndRotateLogs();
        environmentLog.logItemTransport(itemName, from, to);
    }
    
    public void logStorageEvent(String event, String details) {
        checkAndRotateLogs();
        environmentLog.logStorageEvent(event, details);
    }
    
    // ========== LEGACY COMPATIBILITY ==========
    public void logSystemEvent(String event, String details) {
        checkAndRotateLogs();
        environmentLog.logStorageEvent(event, details);
    }
    
    public void logError(String component, String error) {
        checkAndRotateLogs();
        String log = String.format("[%s] ❌ ERROR - %s: %s",
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), component, error);
        System.err.println(log);
    }
    
    public void logWarning(String component, String warning) {
        checkAndRotateLogs();
        String log = String.format("[%s] ⚠️ WARNING - %s: %s",
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), component, warning);
        System.out.println(log);
    }
    
    // ========== CLEANUP ==========
    public void closeLogFiles() {
        agvLog.close();
        taskLog.close();
        environmentLog.close();
        System.out.println("📂 All log files closed successfully");
    }
    
    /**
     * Get current log date
     */
    public String getCurrentLogDate() {
        return currentDate;
    }
}