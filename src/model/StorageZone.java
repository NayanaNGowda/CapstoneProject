package model;

import java.util.ArrayList;
import java.util.List;

public class StorageZone {
    private String zoneName;
    private double temperature;
    private int capacity;
    private List<Item> items;
    private Position position;
    
    public StorageZone(String zoneName, double temperature, int capacity, Position position) {
        this.zoneName = zoneName;
        this.temperature = temperature;
        this.capacity = capacity;
        this.items = new ArrayList<>();
        this.position = position;
    }
    
    public synchronized void addItem(Item item) throws Exception {
        if (items.size() >= capacity) {
            throw new Exception("Storage zone " + zoneName + " is full!");
        }
        items.add(item);
        System.out.println(" Item " + item.getName() + " added to zone " + zoneName);
    }
    
    public synchronized void removeItem(Item item) throws Exception {
        if (!items.remove(item)) {
            throw new Exception("Item not found in storage zone " + zoneName);
        }
        System.out.println(" Item " + item.getName() + " removed from zone " + zoneName);
    }
    
    public String getZoneName() { return zoneName; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temp) { this.temperature = temp; }
    public int getCapacity() { return capacity; }
    public List<Item> getItems() { return new ArrayList<>(items); }
    public Position getPosition() { return position; }
    
    @Override
    public String toString() {
        return zoneName + " [" + items.size() + "/" + capacity + " items]";
    }
}