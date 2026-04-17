package managers;

import java.util.ArrayList;
import java.util.List;

import model.AGV;
import model.Position;
import model.Task;

public class AGVManager {
    private List<AGV> agvList;
    
    public AGVManager() {
        this.agvList = new ArrayList<>();
    }
    
    public void addAGV(AGV agv) {
        agvList.add(agv);
        System.out.println(" AGV added: " + agv.getId());
    }
    
    //Using synchronization getting an available AGV to perform the task
    public synchronized AGV getAvailableAGV() {
        for (AGV agv : agvList) {
            if (!agv.isBusy() && agv.getBatteryLevel() > 20) {
                return agv;
            }
        }
        return null;
    }
    
    public List<AGV> getAgvList() { return new ArrayList<>(agvList); }


}