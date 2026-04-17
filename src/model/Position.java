package model;
public class Position {
    private double x;
    private double y;
    
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    
    public String getPosition() {
        return String.format("(%.0f, %.0f)", x, y);
    }
    
    // Method to calculate distance to destination
    public double distanceTo(Position other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }
}