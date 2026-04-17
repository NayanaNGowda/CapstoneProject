package model;

import managers.SystemLogger;

public abstract class Task implements Runnable {
    private static int counter = 0;
    private int id;
    private String name;
    private int priority;
    private AGV assignedAGV;
//    private StorageZone storageZone; was useful when load , unload 
    private volatile String status;
//    volatile means changes to this variable are visible to all threads immediately.
//    So if one thread sets running = false, other threads see the change without synchronization.
//    private volatile boolean running;
//    can be part of agv but here the thought was may be we can put agv at rest depending on the task done
    private Position restArea;
    protected SystemLogger logger;
    public Task() {
    	
    }
    
    public Task(String name, int priority) {
        this.id = ++counter;
        this.name = name;
        this.priority = priority;
        this.status = "WAITING";
//        this.running = true;
        this.logger = SystemLogger.getInstance();
    }
    
    @Override
    public void run() {
        try {
            this.status = "RUNNING";
            logger.logTaskStarted(id, name, assignedAGV != null ? assignedAGV.getId() : "None");
            
            this.execute();
            
            // Return AGV to rest area after task completion (except for charge tasks)
//            if (assignedAGV != null && restArea != null && !this.getClass().getSimpleName().equals("ChargeTask")) {
                System.out.println(" AGV-" + this.assignedAGV.getId() + " returning to rest area");
                assignedAGV.moveTo(restArea);
//            }
            
            this.status = "COMPLETED";
            System.out.println(" Task " + id + " (" + name + ") completed");
        } catch (Exception e) {
            status = "FAILED";
            logger.logError("Task-" + id, e.getMessage());
            System.err.println("â�Œ Task " + id + " failed: " + e.getMessage());
        }
    }
    
    public abstract void execute() throws Exception;
    
    public int getId() { return id; }
    public String getName() { return name; }
    public int getPriority() { return priority; }
    public AGV getAssignedAGV() { return assignedAGV; }
    public void setAssignedAGV(AGV agv) { this.assignedAGV = agv; }
//    public StorageZone getStorageZone() { return storageZone; }
//    public void setStorageZone(StorageZone zone) { this.storageZone = zone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
//    public boolean isRunning() { return running; }
    public void setRestArea(Position restArea) { this.restArea = restArea; }
    public void setName(String name) {
    	this.name=name;
    }
    @Override
    public String toString() {
        return "Task-" + id + ": " + name + " [" + status + "] Priority:" + priority;
    }

	public void setPriority(int priority) {
		this.priority=priority;
		// TODO Auto-generated method stub
		
	}
}