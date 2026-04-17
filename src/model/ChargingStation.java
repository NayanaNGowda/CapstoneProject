package model;

public class ChargingStation {
    private String id;
    private boolean occupied;
    private Position position;
    
    public ChargingStation(String id, Position position) {
        this.id = id;
        this.occupied = false;
        this.position = position;
    }
    
    public synchronized boolean chargeAGV(AGV agv) {
        if (occupied) return false;
        occupied = true;
        System.out.println(" Charging AGV " + agv.getId() + " at station " + id);
        return true;
    }
    
    public synchronized void releaseStation() {
        occupied = false;
    }
    
    public String getId() { return id; }
    public boolean isOccupied() { return occupied; }
    public Position getPosition() { return position; }
    public void setOccupied(boolean value) {
    	this.occupied=value;
    }
    
    @Override
    public String toString() {
        return id + (occupied ? " [OCCUPIED]" : " [AVAILABLE]");
    }
}

