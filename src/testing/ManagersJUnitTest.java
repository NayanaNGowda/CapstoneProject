package testing;

import model.*;
import managers.*;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

// JUnit Test Suite for Manager Classes
// Tests: AGVManager, TaskManager, Priority System, Auto-charging

public class ManagersJUnitTest {

    private AGVManager agvManager;
    private TaskManager taskManager;

    @Before
    public void setUp() {
        agvManager = new AGVManager();
        taskManager = new TaskManager(agvManager);
    }

    // ===========================
    // AGV MANAGER TESTS
    // ===========================

    @Test
    public void testAddMultipleAGVs() {
        AGV agv1 = new AGV("001", new Position(80, 50));
        AGV agv2 = new AGV("002", new Position(100, 50));

        agvManager.addAGV(agv1);
        agvManager.addAGV(agv2);

        assertEquals(2, agvManager.getAgvList().size());
    }

    @Test
    public void testGetAvailableAGV() {
        AGV agv1 = new AGV("001", new Position(80, 50));
        AGV agv2 = new AGV("002", new Position(100, 50));

        agvManager.addAGV(agv1);
        agvManager.addAGV(agv2);

        AGV available = agvManager.getAvailableAGV();
        assertNotNull(available);
        assertFalse(available.isBusy());
        assertTrue(available.getBatteryLevel() > 20);
    }

    @Test
    public void testNoAvailableAGVWhenAllBusy() {
        AGV agv1 = new AGV("001", new Position(80, 50));
        AGV agv2 = new AGV("002", new Position(100, 50));

        agvManager.addAGV(agv1);
        agvManager.addAGV(agv2);

        agv1.setBusy(true);
        agv2.setBusy(true);

        assertNull(agvManager.getAvailableAGV());
    }

    @Test
    public void testLowBatteryAGVNotAvailable() {
        AGV agv = new AGV("001", new Position(80, 50));
        agv.setBatteryLevel(15.0);

        agvManager.addAGV(agv);

        assertNull(agvManager.getAvailableAGV());
    }
//    Get AGV list is immutable (defensive copy)manager.getAgvList().clear();  // OOPS! Deletes all AGVs from the system!
    @Test
    public void testAGVListDefensiveCopy() {
        AGV agv = new AGV("001", new Position(80, 50));
        agvManager.addAGV(agv);

        List<AGV> list1 = agvManager.getAgvList();
        List<AGV> list2 = agvManager.getAgvList();

        assertNotSame(list1, list2);
    }

    // ===========================
    // TASK MANAGER TESTS
    // ===========================

    @Test
    public void testTaskManagerInitialization() {
        assertEquals(0, taskManager.getTaskQueue().size());
        assertEquals(0, taskManager.getActiveTasks().size());
        assertEquals(0, taskManager.getCompletedTasks().size());
    }

    @Test
    public void testAddTransportTask() throws Exception {
        StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
        StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));

        Item item = new Item("ITEM-001", "Test Package", 5.0, new Date(), 20.0);
        zone1.addItem(item);

        TransportTask task = new TransportTask(5, item, zone1, zone2);
        taskManager.addTask(task);

        assertEquals(1, taskManager.getTaskQueue().size());
    }

    @Test
    public void testAutomaticTaskAssignment() throws Exception {
        agvManager.addAGV(new AGV("001", new Position(80, 50)));

        StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
        StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));

        Item item = new Item("ITEM-001", "Test Package", 5.0, new Date(), 20.0);
        zone1.addItem(item);

        TransportTask task = new TransportTask(5, item, zone1, zone2);
        taskManager.addTask(task);

        Thread.sleep(1000);

        assertTrue(taskManager.getActiveTasks().size() > 0 ||
                   taskManager.getCompletedTasks().size() > 0);
    }

    @Test
    public void testSetChargingStations() {
        ChargingStation cs1 = new ChargingStation("CS-1", new Position(200, 700));
        ChargingStation cs2 = new ChargingStation("CS-2", new Position(350, 700));

        List<ChargingStation> stations = new ArrayList<>();
        stations.add(cs1);
        stations.add(cs2);

        taskManager.setChargingStations(stations);
    }

    // ===========================
    // TASK PRIORITY TESTS
    // ===========================

    @Test
    public void testTaskPriorityOrdering() throws Exception {
        agvManager.addAGV(new AGV("001", new Position(80, 50)));
        agvManager.addAGV(new AGV("002", new Position(100, 50)));

        StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
        StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));

        Item item1 = new Item("ITEM-001", "Low Priority", 5.0, new Date(), 20.0);
        zone1.addItem(item1);
        TransportTask low = new TransportTask(3, item1, zone1, zone2);

        Item item2 = new Item("ITEM-002", "High Priority", 5.0, new Date(), 20.0);
        zone1.addItem(item2);
        TransportTask high = new TransportTask(9, item2, zone1, zone2);

        taskManager.addTask(low);
        taskManager.addTask(high);

        Thread.sleep(1000);

        assertTrue(high.getPriority() > low.getPriority());
    }

    // ===========================
    // AUTO-CHARGING TESTS
    // ===========================

    @Test
    public void testAutoChargeTriggerForLowBattery() throws InterruptedException {
        AGV agv = new AGV("001", new Position(80, 50));
        agv.setBatteryLevel(18.0);
        agvManager.addAGV(agv);

        ChargingStation station = new ChargingStation("CS-1", new Position(200, 700));
        List<ChargingStation> stations = new ArrayList<>();
        stations.add(station);

        taskManager.setChargingStations(stations);

        Thread.sleep(2000);

        int total = taskManager.getTaskQueue().size() +
                    taskManager.getActiveTasks().size();

        assertTrue(total > 0);
    }

    @Test
    public void testNoAutoChargeForSufficientBattery() throws InterruptedException {
        AGV agv = new AGV("001", new Position(80, 50));
        agv.setBatteryLevel(80.0);
        agvManager.addAGV(agv);

        ChargingStation station = new ChargingStation("CS-1", new Position(200, 700));
        List<ChargingStation> stations = new ArrayList<>();
        stations.add(station);

        taskManager.setChargingStations(stations);

        Thread.sleep(1000);

        assertEquals(0, taskManager.getActiveTasks().size());
    }

    @Test
    public void testChargingStationOccupation() {
        ChargingStation station = new ChargingStation("CS-1", new Position(200, 700));
        AGV agv1 = new AGV("001", new Position(80, 50));
        AGV agv2 = new AGV("002", new Position(100, 50));

        boolean first = station.chargeAGV(agv1);
        assertTrue(first);

        boolean second = station.chargeAGV(agv2);
        assertFalse(second);
    }
}
