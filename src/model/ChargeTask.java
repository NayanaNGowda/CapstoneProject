package model;

public class ChargeTask extends Task {
    private ChargingStation station;
    private AGV targetAGV;
    
    public ChargeTask(int priority, ChargingStation station, AGV targetAGV) {
        super("Charge AGV-" + targetAGV.getId() + " at " + station.getId(), priority);
        this.station = station;
        this.targetAGV = targetAGV;
    }
    
    @Override
    public void execute() throws Exception {
        AGV agv = this.targetAGV;
        this.setAssignedAGV(agv);
        
        
        if (agv == null) {
            throw new Exception("No AGV specified for charging");
        }
       
        double initialBattery = agv.getBatteryLevel();
        logger.logAGVBattery(agv.getId(), initialBattery, "Needs charging");
        System.out.println(" AGV-" + agv.getId() + " needs charging (Battery: " + 
                         String.format("%.1f", initialBattery) + "%)");
        
        // Move to charging station
        System.out.println(" Moving to " + station.getId());
        String startPos = agv.getPosition().getPosition();
        Position stationPos = new Position(station.getPosition().getX(), station.getPosition().getY());
        agv.moveTo(stationPos);
        logger.logAGVMovement(agv.getId(), startPos, station.getId());
        agv.setStatus("CHARGING");
        if (!station.chargeAGV(agv)) {
            throw new Exception("Charging station " + station.getId() + " is occupied");
        }
        
        logger.logAGVChargingStart(agv.getId(), station.getId(), initialBattery);
        System.out.println(" Charging started at " + station.getId());
        
        //  charging (5% per 500ms until 95%)
        while (agv.getBatteryLevel() < 95) {
            Thread.sleep(500);
            agv.setBatteryLevel(agv.getBatteryLevel() + 5);
            System.out.println(" Charging... " + String.format("%.0f", agv.getBatteryLevel()) + "%");
        }
        
        agv.setBatteryLevel(100);
        station.releaseStation();
        agv.setStatus("IDLE");
        logger.logAGVChargingComplete(agv.getId(), initialBattery);
        System.out.println(" AGV-" + agv.getId() + " fully charged! (" + 
                         String.format("%.0f", initialBattery) + "% → 100%)");
    }
    
    public AGV getTargetAGV() { return targetAGV; }
}