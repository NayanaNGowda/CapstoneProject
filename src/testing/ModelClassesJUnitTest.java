package testing;

import model.*;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.Date;

/**
 * JUnit Test Suite for Model Classes
 * Tests: Position, AGV, Item, ChargingStation, Task classes
 */
public class ModelClassesJUnitTest {
    
    // ========== POSITION CLASS TESTS ==========
    
    @Test
    public void testPositionCreation() {
        Position pos = new Position(100, 200);
        assertEquals(100.0, pos.getX(), 0.001);
        assertEquals(200.0, pos.getY(), 0.001);
    }
    
    @Test
    public void testPositionDistanceCalculation() {
        Position p1 = new Position(0, 0);
        Position p2 = new Position(3, 4);
        double distance = p1.distanceTo(p2);
        assertEquals(5.0, distance, 0.001);
    }
    
    @Test
    public void testPositionStringFormat() {
        Position pos = new Position(150, 250);
        String formatted = pos.getPosition();
        assertEquals("(150, 250)", formatted);
    }
    
    // ========== AGV CLASS TESTS ==========
    
    @Test
    public void testAGVInitialization() {
        AGV agv = new AGV("001", new Position(0, 0));
        assertEquals("001", agv.getId());
        assertEquals(100.0, agv.getBatteryLevel(), 0.001);
        assertEquals("IDLE", agv.getStatus());
        assertFalse(agv.isBusy());
    }
    
    @Test
    public void testAGVBatteryManagement() {
        AGV agv = new AGV("002", new Position(0, 0));
        agv.setBatteryLevel(75.0);
        assertEquals(75.0, agv.getBatteryLevel(), 0.001);
        
        agv.setBatteryLevel(15.0);
        assertEquals(15.0, agv.getBatteryLevel(), 0.001);
    }
    
    @Test
    public void testAGVBusyStatus() {
        AGV agv = new AGV("003", new Position(0, 0));
        
        agv.setBusy(true);
        assertTrue(agv.isBusy());
        assertEquals("BUSY", agv.getStatus());
        
        agv.setBusy(false);
        assertFalse(agv.isBusy());
        assertEquals("IDLE", agv.getStatus());
    }
    
    @Test
    public void testAGVMovement() throws InterruptedException {
        AGV agv = new AGV("004", new Position(0, 0));
        Position destination = new Position(100, 100);
        double initialBattery = agv.getBatteryLevel();
        
        Thread moveThread = new Thread(() -> {
            try {
                agv.moveTo(destination);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        moveThread.start();
        moveThread.join();
        
        assertEquals(100.0, agv.getPosition().getX(), 0.001);
        assertEquals(100.0, agv.getPosition().getY(), 0.001);
        assertTrue(agv.getBatteryLevel() < initialBattery);
    }
    
    // ========== ITEM CLASS TESTS ==========
    
    @Test
    public void testItemCreation() {
        Item item = new Item("ITEM-001", "Frozen Pizza", 2.5, new Date(), -20.0);
        assertEquals("ITEM-001", item.getId());
        assertEquals("Frozen Pizza", item.getName());
        assertEquals(2.5, item.getWeight(), 0.001);
        assertEquals(-20.0, item.getRequiredTemperature(), 0.001);
    }
    
    @Test
    public void testItemTemperatureVariations() {
        Item frozen = new Item("ITEM-002", "Ice Cream", 1.0, new Date(), -20.0);
        Item ambient = new Item("ITEM-003", "Canned Food", 0.5, new Date(), 20.0);
        Item hot = new Item("ITEM-004", "Hot Package", 3.0, new Date(), 30.0);
        
        assertEquals(-20.0, frozen.getRequiredTemperature(), 0.001);
        assertEquals(20.0, ambient.getRequiredTemperature(), 0.001);
        assertEquals(30.0, hot.getRequiredTemperature(), 0.001);
    }
    
    @Test
    public void testItemEquality() {
        Item item1 = new Item("ITEM-005", "Test", 1.0, new Date(), 20.0);
        Item item2 = new Item("ITEM-005", "Test", 1.0, new Date(), 20.0);
        Item item3 = new Item("ITEM-006", "Test", 1.0, new Date(), 20.0);
        
        assertTrue(item1.equals(item2));
        assertFalse(item1.equals(item3));
    }
    
    // ========== CHARGING STATION TESTS ==========
    
    @Test
    public void testChargingStationInitialization() {
        ChargingStation station = new ChargingStation("CS-001", new Position(200, 700));
        assertEquals("CS-001", station.getId());
        assertFalse(station.isOccupied());
    }
    
    @Test
    public void testSingleAGVCharging() {
        ChargingStation station = new ChargingStation("CS-002", new Position(200, 700));
        AGV agv = new AGV("AGV-001", new Position(0, 0));
        
        boolean canCharge = station.chargeAGV(agv);
        assertTrue(canCharge);
        assertTrue(station.isOccupied());
    }
    
    @Test
    public void testMultipleAGVChargingPrevention() {
        ChargingStation station = new ChargingStation("CS-003", new Position(200, 700));
        AGV agv1 = new AGV("AGV-001", new Position(0, 0));
        AGV agv2 = new AGV("AGV-002", new Position(0, 0));
        
        boolean firstCharge = station.chargeAGV(agv1);
        boolean secondCharge = station.chargeAGV(agv2);
        
        assertTrue(firstCharge);
        assertFalse(secondCharge);
    }
    
    @Test
    public void testStationRelease() {
        ChargingStation station = new ChargingStation("CS-004", new Position(200, 700));
        AGV agv = new AGV("AGV-001", new Position(0, 0));
        
        station.chargeAGV(agv);
        assertTrue(station.isOccupied());
        
        station.releaseStation();
        assertFalse(station.isOccupied());
    }
    
    // ========== TASK CLASSES TESTS ==========
    
    @Test
    public void testTransportTaskCreation() throws Exception {
        StorageZone sourceZone = new StorageZone("Source-Zone", 20.0, 10, new Position(100, 100));
        StorageZone targetZone = new StorageZone("Target-Zone", 20.0, 10, new Position(200, 200));
        Item item = new Item("ITEM-001", "Test Package", 5.0, new Date(), 20.0);
        sourceZone.addItem(item);
        
        TransportTask task = new TransportTask(7, item, sourceZone, targetZone);
        
        assertEquals(7, task.getPriority());
        assertEquals("WAITING", task.getStatus());
        assertTrue(task.getName().contains("Transport"));
        assertTrue(task.getName().contains(item.getName()));
    }
    
    @Test
    public void testTaskPriorityComparison() throws Exception {
        StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
        StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
        
        Item item1 = new Item("ITEM-001", "Package 1", 5.0, new Date(), 20.0);
        Item item2 = new Item("ITEM-002", "Package 2", 5.0, new Date(), 20.0);
        zone1.addItem(item1);
        zone1.addItem(item2);
        
        TransportTask lowPriority = new TransportTask(3, item1, zone1, zone2);
        TransportTask highPriority = new TransportTask(9, item2, zone1, zone2);
        
        assertTrue(highPriority.getPriority() > lowPriority.getPriority());
    }
    
    @Test
    public void testChargeTaskCreation() {
        ChargingStation station = new ChargingStation("CS-001", new Position(200, 700));
        AGV agv = new AGV("AGV-001", new Position(0, 0));
        agv.setBatteryLevel(15.0);
        
        ChargeTask chargeTask = new ChargeTask(10, station, agv);
        
        assertEquals(10, chargeTask.getPriority());
        assertEquals("WAITING", chargeTask.getStatus());
        assertTrue(chargeTask.getName().contains("Charge"));
        assertTrue(chargeTask.getName().contains(agv.getId()));
        assertEquals(agv, chargeTask.getTargetAGV());
    }
    
    @Test
    public void testTaskIDUniqueness() throws Exception {
        StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
        StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
        
        Item item1 = new Item("ITEM-001", "Package 1", 5.0, new Date(), 20.0);
        Item item2 = new Item("ITEM-002", "Package 2", 5.0, new Date(), 20.0);
        zone1.addItem(item1);
        zone1.addItem(item2);
        
        TransportTask task1 = new TransportTask(5, item1, zone1, zone2);
        TransportTask task2 = new TransportTask(5, item2, zone1, zone2);
        
        assertNotEquals(task1.getId(), task2.getId());
    }
    
    @Test
    public void testTaskStatusChanges() throws Exception {
        StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
        StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
        Item item = new Item("ITEM-001", "Package 1", 5.0, new Date(), 20.0);
        zone1.addItem(item);
        
        TransportTask task = new TransportTask(5, item, zone1, zone2);
        
        assertEquals("WAITING", task.getStatus());
        
        task.setStatus("RUNNING");
        assertEquals("RUNNING", task.getStatus());
        
        task.setStatus("COMPLETED");
        assertEquals("COMPLETED", task.getStatus());
    }
    
    @Test
    public void testTaskAGVAssignment() throws Exception {
        StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
        StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
        Item item = new Item("ITEM-001", "Package 1", 5.0, new Date(), 20.0);
        zone1.addItem(item);
        
        TransportTask task = new TransportTask(5, item, zone1, zone2);
        AGV agv = new AGV("AGV-001", new Position(80, 50));
        
        assertNull(task.getAssignedAGV());
        
        task.setAssignedAGV(agv);
        assertEquals(agv, task.getAssignedAGV());
    }
    
    @Test
    public void testTransportTaskItemRetrieval() throws Exception {
        StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
        StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
        Item item = new Item("ITEM-001", "Frozen Pizza", 2.5, new Date(), -20.0);
        zone1.addItem(item);
        
        TransportTask task = new TransportTask(5, item, zone1, zone2);
        
        assertEquals(item, task.getItem());
        assertEquals(zone1, task.getSourceZone());
        assertEquals(zone2, task.getTargetZone());
    }
}