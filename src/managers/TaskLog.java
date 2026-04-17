package managers;
import java.io.IOException;
import java.util.Date;
import java.io.BufferedWriter;
import java.io.FileWriter;
class TaskLog extends SystemLog {
    
    public TaskLog(String logDirectory, String timestamp) {
        super(logDirectory, "task_log_" + timestamp + ".txt");
    }
    
//    since this is checked exception throws could also been used
    @Override
    protected void initializeLogFile() {
        try {
            logWriter = new BufferedWriter(new FileWriter(logFilePath, true));
            writeHeader();
            System.out.println(" Task Log initialized: " + logFilePath);
        } catch (IOException e) {
            System.err.println(" Error initializing Task log: " + e.getMessage());
        }
    }
    
    @Override
    protected void writeHeader() throws IOException {
        logWriter.write("=".repeat(60));
        logWriter.newLine();
        logWriter.write("  TASK LOG");
        logWriter.newLine();
        logWriter.write("  Session started: " + dateFormat.format(new Date()));
        logWriter.newLine();
        logWriter.write("=".repeat(60));
        logWriter.newLine();
        logWriter.newLine();
        logWriter.flush();
    }
    
    public synchronized void logTaskCreated(int taskId, String taskName, int priority) {
        String log = String.format("%s  CREATED - Task-%d: %s (Priority: %d)",
            getTimestamp(), taskId, taskName, priority);
        writeToFile(log);
        System.out.println(" " + log);
    }
    
    public synchronized void logTaskAssigned(int taskId, String agvId) {
        String log = String.format("%s ASSIGNED - Task-%d → AGV-%s",
            getTimestamp(), taskId, agvId);
        writeToFile(log);
        System.out.println(" " + log);
    }
    
    public synchronized void logTaskStarted(int taskId, String taskName, String agvId) {
        String log = String.format("%s  STARTED - Task-%d (%s) by AGV-%s",
            getTimestamp(), taskId, taskName, agvId);
        writeToFile(log);
        System.out.println(" " + log);
    }
    
    public synchronized void logTaskCompleted(int taskId, String status) {
        String log = String.format("%s %s %s - Task-%d",
            getTimestamp(), status, status, taskId);
        writeToFile(log);
        System.out.println(" " + log);
    }
}

