package model;

import java.util.Date;

public class Item {
    private String id;
    private String name;
    private double weight;
    private Date expiryDate;
    private double requiredTemperature; // Temperature requirement for storage
    
    public Item(String id, String name, double weight, Date expiryDate, double requiredTemperature) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.expiryDate = expiryDate;
        this.requiredTemperature = requiredTemperature;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getWeight() { return weight; }
    public Date getExpiryDate() { return expiryDate; }
    public double getRequiredTemperature() { return requiredTemperature; }
    
    @Override
    public String toString() {
       
        return name + " "  + " (" + String.format("%.0f°C", requiredTemperature) + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item item = (Item) obj;
        return id.equals(item.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}