package managers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract base class for all logging operations
 * Provides common functionality for file writing and formatting
 */
public abstract class SystemLog {
    protected BufferedWriter logWriter;
    protected SimpleDateFormat dateFormat;
    protected String logFilePath;
    
    public SystemLog(String logDirectory, String logFileName) {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.logFilePath = logDirectory + "/" + logFileName;
        initializeLogFile();
    }
    
    protected abstract void initializeLogFile();
    protected abstract void writeHeader() throws IOException;
    
    /**
     * Common method for writing to log file
     */
    protected synchronized void writeToFile(String message) {
        try {
            if (logWriter != null) {
                logWriter.write(message);
                logWriter.newLine();
                logWriter.flush();
            }
        } catch (IOException e) {
            System.err.println("❌ Error writing to log file: " + e.getMessage());
        }
    }
    
    /**
     * Format timestamp for log entries
     */
    protected String getTimestamp() {
        return "[" + dateFormat.format(new Date()) + "]";
    }
    
    /**
     * Close log file
     */
    public void close() {
        try {
            if (logWriter != null) {
                writeFooter();
                logWriter.close();
            }
        } catch (IOException e) {
            System.err.println("❌ Error closing log file: " + e.getMessage());
        }
    }
    
    protected void writeFooter() throws IOException {
        logWriter.write("\n" + "=".repeat(60));
        logWriter.newLine();
        logWriter.write("  Session ended: " + dateFormat.format(new Date()));
        logWriter.newLine();
        logWriter.write("=".repeat(60));
        logWriter.newLine();
    }
}