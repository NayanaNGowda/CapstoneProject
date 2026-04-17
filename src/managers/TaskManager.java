package managers;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import model.AGV;
import model.ChargeTask;
import model.ChargingStation;
import model.Position;
import model.Task;

public class TaskManager {
    private Queue<Task> taskQueue;
    private List<Task> activeTasks;
    private List<Task> completedTasks;
    private AGVManager agvManager;
    private SystemLogger logger;
    private List<ChargingStation> chargingStations;
    
    public TaskManager(AGVManager agvManager) {
        this.taskQueue = new PriorityQueue<>((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
        this.activeTasks = new ArrayList<>();
        this.completedTasks = new ArrayList<>();
        this.agvManager = agvManager;
        this.logger = SystemLogger.getInstance();
        this.chargingStations = new ArrayList<>();
        
        this.startTaskAssigner();
    }
    
    public void setChargingStations(List<ChargingStation> stations) {
        this.chargingStations = stations;
    }
    
    public synchronized void addTask(Task task) {
        taskQueue.offer(task);
        logger.logTaskCreated(task.getId(), task.getName(), task.getPriority());
        System.out.println(" Task added: " + task.getName() + " (Priority: " + task.getPriority() + ")");
    }
    
    private void startTaskAssigner() {
        Thread assignerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        assignNextTask();
                        autoTriggerCharging();
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // Stop the loop if thread was interrupted
                        break;
                    }
                }
            }
        });

        assignerThread.setDaemon(true);   // Background thread
        assignerThread.start();           // Begin execution
    }

    
    private synchronized void autoTriggerCharging() {
        if (chargingStations.isEmpty()) return;
        
        for (AGV agv : agvManager.getAgvList()) {
            // Auto-charge if battery < 20% and AGV is not busy and not already charging
            if (agv.getBatteryLevel() <= 20 && !agv.isBusy()) {
                
                // Check if charging task already exists for this AGV
                boolean alreadyCharging = false;
                
                // Check in queue
                for (Task t : taskQueue) {
                    if (t instanceof ChargeTask) {
                        ChargeTask ct = (ChargeTask) t;
                        if (ct.getTargetAGV() != null && ct.getTargetAGV().equals(agv)) {
                            alreadyCharging = true;
                            break;
                        }
                    }
                }
                
                // Check in active tasks
                if (!alreadyCharging) {
                    for (Task t : activeTasks) {
                        if (t instanceof ChargeTask) {
                            ChargeTask ct = (ChargeTask) t;
                            if (ct.getTargetAGV() != null && ct.getTargetAGV().equals(agv)) {
                                alreadyCharging = true;
                                break;
                            }
                        }
                    }
                }
                
                if (!alreadyCharging) {
                    // Find available charging station
                    ChargingStation availableStation = null;
                    for (ChargingStation station : chargingStations) {
                        if (!station.isOccupied()) {
                            availableStation = station;
                            break;
                        }
                    }
                    
                    if (availableStation != null) {
                        ChargeTask chargeTask = new ChargeTask(10, availableStation, agv); // High priority
                        taskQueue.offer(chargeTask);
                        logger.logTaskCreated(chargeTask.getId(), chargeTask.getName(), chargeTask.getPriority());
                        System.out.println(" AUTO-CHARGE: Task created for AGV-" + agv.getId() + 
                                         " (Battery: " + String.format("%.0f", agv.getBatteryLevel()) + "%)");
                    } else {
                        System.out.println(" WARNING: AGV-" + agv.getId() + " needs charging but all stations occupied!");
                    }
                }
            }
        }
    }
    
    private synchronized void assignNextTask() {
        // Clean up completed tasks
        activeTasks.removeIf(task -> {
            if (task.getStatus().equals("COMPLETED") || task.getStatus().equals("FAILED")) {
                completedTasks.add(task);
                if (task.getAssignedAGV() != null) {
                    task.getAssignedAGV().setBusy(false);
                }
                logger.logTaskCompleted(task.getId(), task.getStatus());
                return true;
            }
            return false;
        });
        
        // Assign new tasks
        if (!taskQueue.isEmpty()) {
            Task task = taskQueue.peek();
            AGV availableAGV = null;
            
            // For ChargeTask, use the target AGV directly
            if (task instanceof ChargeTask) {
                ChargeTask chargeTask = (ChargeTask) task;
                AGV targetAGV = chargeTask.getTargetAGV();
                
                if (targetAGV != null && !targetAGV.isBusy()) {
                    availableAGV = targetAGV;
                }
            } else {
                // For other tasks, get any available AGV with sufficient battery
                availableAGV = agvManager.getAvailableAGV();
                
                if (availableAGV != null && availableAGV.getBatteryLevel() <= 20) {
                    // Don't assign transport tasks to low battery AGVs
                    availableAGV = null;
                }
            }
            
            if (availableAGV != null) {
                taskQueue.poll();
                task.setAssignedAGV(availableAGV);
                task.setRestArea(new Position(100, 50)); // Pass rest area to task
                availableAGV.setBusy(true);
                activeTasks.add(task);
                
                // Log task assignment
                logger.logTaskAssigned(task.getId(), availableAGV.getId());
                
                Thread taskThread = new Thread(task);
                taskThread.setDaemon(true);
                taskThread.start();
                
                System.out.println(" Task-" + task.getId() + " assigned to AGV-" + availableAGV.getId() + 
                                 " (Battery: " + String.format("%.0f", availableAGV.getBatteryLevel()) + "%)");
            }
        }
    }
    
    public synchronized Queue<Task> getTaskQueue() { return new PriorityQueue<>(taskQueue); }
    public synchronized List<Task> getActiveTasks() { return new ArrayList<>(activeTasks); }
    public synchronized List<Task> getCompletedTasks() { return new ArrayList<>(completedTasks); }
}