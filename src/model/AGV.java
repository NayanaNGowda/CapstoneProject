package model;

public class AGV {
    private String id;
    private double batteryLevel;
    private boolean busy;
    private Position position;
    private String status;
    
    public AGV(String id, Position startPosition) {
        this.id = id;
        this.batteryLevel = 100.0;
        this.busy = false;
        this.position = startPosition;
        this.status = "IDLE";
    }
    
    public synchronized void moveTo(Position destination) throws InterruptedException {
        double distance = position.distanceTo(destination);
        status = "MOVING";
        
        // Updating AGV position
        double steps = 20;
        double startX = position.getX();
        double startY = position.getY();
        double dx = (destination.getX() - startX) / steps;
        double dy = (destination.getY() - startY) / steps;
        
        for (int i = 0; i < steps; i++) {
            // Creating new position object to update AGV position only
            position = new Position(startX + (dx * (i + 1)), startY + (dy * (i + 1)));
            // Battery consumption for each AGV is 0.005% per distance unit
            this.batteryLevel -= (distance / steps) * 0.005;
            if (batteryLevel < 0) batteryLevel = 0;
            Thread.sleep(100);
        }
        
        // Setting final position for AGV
        this.position = new Position(destination.getX(), destination.getY());
        status = "IDLE";
    }
    
    public Position getPosition() { return position; }
    public String getId() { return id; }
    public double getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(double level) { 
        this.batteryLevel = level; 
    }
    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) { 
        this.busy = busy;
        this.status = busy ? "BUSY" : "IDLE";
    }
    public String getStatus() { return status; }
    public void setStatus(String status) {
    	this.status=status;
    }
    
    
    @Override
    public String toString() {
        String battIcon = batteryLevel > 50 ? "ðŸ”‹" : batteryLevel > 20 ? "ðŸª«" : "âš ï¸�";
        return String.format("AGV-%s %s %.0f%%", id, battIcon, batteryLevel);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AGV agv = (AGV) obj;
        return id.equals(agv.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}