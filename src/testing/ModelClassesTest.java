package testing;

import model.*;
import java.util.Date;

/**
 * TEST FILE: Model Classes Testing
 * Tests: Position, AGV, Item, ChargingStation
 */
public class ModelClassesTest {
    
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("\n ");
        System.out.println("    TEST 1: MODEL CLASSES                           ");
        System.out.println("    Tester: Team Member 1                           ");
        System.out.println(" \n");
        
        testPositionClass();
        testAGVClass();
        testItemClass();
        testChargingStationClass();
        testTaskClasses();
        
        printSummary();
    }
    
    // ========== TEST POSITION CLASS ==========
    private static void testPositionClass() {
        printTestSection("POSITION CLASS");
        
        try {
            Position pos = new Position(100, 200);
            assertEquals(100.0, pos.getX(), "Position X coordinate");
            assertEquals(200.0, pos.getY(), "Position Y coordinate");
        } catch (Exception e) {
            testFailed("Position creation", e.getMessage());
        }
        
        try {
            Position p1 = new Position(0, 0);
            Position p2 = new Position(3, 4);
            double distance = p1.distanceTo(p2);
            assertEquals(5.0, distance, "Position distance calculation");
        } catch (Exception e) {
            testFailed("Distance calculation", e.getMessage());
        }
        
        try {
            Position pos = new Position(150, 250);
            String formatted = pos.getPosition();
            assertEquals("(150, 250)", formatted, "Position string format");
        } catch (Exception e) {
            testFailed("Position format", e.getMessage());
        }
    }
    
    // ========== TEST AGV CLASS ==========
    private static void testAGVClass() {
        printTestSection("AGV CLASS");
        
        try {
            AGV agv = new AGV("001", new Position(0, 0));
            assertEquals("001", agv.getId(), "AGV ID");
            assertEquals(100.0, agv.getBatteryLevel(), "AGV initial battery (100%)");
            assertEquals("IDLE", agv.getStatus(), "AGV initial status");
            assertFalse(agv.isBusy(), "AGV initially not busy");
        } catch (Exception e) {
            testFailed("AGV initialization", e.getMessage());
        }
        
        try {
            AGV agv = new AGV("002", new Position(0, 0));
            agv.setBatteryLevel(75.0);
            assertEquals(75.0, agv.getBatteryLevel(), "AGV battery update");
            agv.setBatteryLevel(15.0);
            assertEquals(15.0, agv.getBatteryLevel(), "AGV low battery");
        } catch (Exception e) {
            testFailed("Battery management", e.getMessage());
        }
        
        try {
            AGV agv = new AGV("003", new Position(0, 0));
            agv.setBusy(true);
            assertTrue(agv.isBusy(), "AGV set to busy");
            assertEquals("BUSY", agv.getStatus(), "AGV status when busy");
            agv.setBusy(false);
            assertFalse(agv.isBusy(), "AGV set to idle");
            assertEquals("IDLE", agv.getStatus(), "AGV status when idle");
        } catch (Exception e) {
            testFailed("Busy status management", e.getMessage());
        }
        
        try {
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
            
            assertEquals(100.0, agv.getPosition().getX(), "AGV final X position");
            assertEquals(100.0, agv.getPosition().getY(), "AGV final Y position");
            assertTrue(agv.getBatteryLevel() < initialBattery, "Battery decreased after movement");
            
        } catch (Exception e) {
            testFailed("AGV movement", e.getMessage());
        }
    }
    
    // ========== TEST ITEM CLASS ==========
    private static void testItemClass() {
        printTestSection("ITEM CLASS");
        
        try {
            Item item = new Item("ITEM-001", "Frozen Pizza", 2.5, new Date(), -20.0);
            assertEquals("ITEM-001", item.getId(), "Item ID");
            assertEquals("Frozen Pizza", item.getName(), "Item name");
            assertEquals(2.5, item.getWeight(), "Item weight");
            assertEquals(-20.0, item.getRequiredTemperature(), "Item temperature requirement");
        } catch (Exception e) {
            testFailed("Item creation", e.getMessage());
        }
        
        try {
            Item frozen = new Item("ITEM-002", "Ice Cream", 1.0, new Date(), -20.0);
            Item ambient = new Item("ITEM-003", "Canned Food", 0.5, new Date(), 20.0);
            Item hot = new Item("ITEM-004", "Hot Package", 3.0, new Date(), 30.0);
            
            assertEquals(-20.0, frozen.getRequiredTemperature(), "Frozen item temperature");
            assertEquals(20.0, ambient.getRequiredTemperature(), "Ambient item temperature");
            assertEquals(30.0, hot.getRequiredTemperature(), "Hot item temperature");
        } catch (Exception e) {
            testFailed("Item temperature variations", e.getMessage());
        }
        
        try {
            Item item1 = new Item("ITEM-005", "Test", 1.0, new Date(), 20.0);
            Item item2 = new Item("ITEM-005", "Test", 1.0, new Date(), 20.0);
            Item item3 = new Item("ITEM-006", "Test", 1.0, new Date(), 20.0);
            
            assertTrue(item1.equals(item2), "Items with same ID are equal");
            assertFalse(item1.equals(item3), "Items with different ID are not equal");
        } catch (Exception e) {
            testFailed("Item equality", e.getMessage());
        }
    }
    
    // ========== TEST CHARGING STATION CLASS ==========
    private static void testChargingStationClass() {
        printTestSection("CHARGING STATION CLASS");
        
        try {
            ChargingStation station = new ChargingStation("CS-001", new Position(200, 700));
            assertEquals("CS-001", station.getId(), "Station ID");
            assertFalse(station.isOccupied(), "Station initially available");
        } catch (Exception e) {
            testFailed("Station initialization", e.getMessage());
        }
        
        try {
            ChargingStation station = new ChargingStation("CS-002", new Position(200, 700));
            AGV agv = new AGV("AGV-001", new Position(0, 0));
            boolean canCharge = station.chargeAGV(agv);
            assertTrue(canCharge, "AGV can charge at available station");
            assertTrue(station.isOccupied(), "Station occupied after charging starts");
        } catch (Exception e) {
            testFailed("Single AGV charging", e.getMessage());
        }
        
        try {
            ChargingStation station = new ChargingStation("CS-003", new Position(200, 700));
            AGV agv1 = new AGV("AGV-001", new Position(0, 0));
            AGV agv2 = new AGV("AGV-002", new Position(0, 0));
            
            boolean firstCharge = station.chargeAGV(agv1);
            boolean secondCharge = station.chargeAGV(agv2);
            
            assertTrue(firstCharge, "First AGV can charge");
            assertFalse(secondCharge, "Second AGV cannot charge at occupied station");
        } catch (Exception e) {
            testFailed("Multiple AGV charging prevention", e.getMessage());
        }
        
        try {
            ChargingStation station = new ChargingStation("CS-004", new Position(200, 700));
            AGV agv = new AGV("AGV-001", new Position(0, 0));
            
            station.chargeAGV(agv);
            assertTrue(station.isOccupied(), "Station occupied");
            
            station.releaseStation();
            assertFalse(station.isOccupied(), "Station available after release");
        } catch (Exception e) {
            testFailed("Station release", e.getMessage());
        }
    }
    
    // ========== TEST TASK CLASSES ==========
    private static void testTaskClasses() {
        printTestSection("TASK CLASSES (Task, TransportTask, ChargeTask)");
printTestSection("TASK CLASSES (Task, TransportTask, ChargeTask)");
        
        // Test 1: TransportTask creation and properties
        try {
            StorageZone sourceZone = new StorageZone("Source-Zone", 20.0, 10, new Position(100, 100));
            StorageZone targetZone = new StorageZone("Target-Zone", 20.0, 10, new Position(200, 200));
            Item item = new Item("ITEM-001", "Test Package", 5.0, new Date(), 20.0);
            sourceZone.addItem(item);
            
            TransportTask task = new TransportTask(7, item, sourceZone, targetZone);
            
            assertEquals(7, task.getPriority(), "Task priority");
            assertEquals("WAITING", task.getStatus(), "Initial task status");
            assertTrue(task.getName().contains("Transport"), "Task name contains 'Transport'");
            assertTrue(task.getName().contains(item.getName()), "Task name contains item name");
            
        } catch (Exception e) {
            testFailed("TransportTask creation", e.getMessage());
        }
        
        // Test 2: Task priority comparison
        try {
            StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
            StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
            
            Item item1 = new Item("ITEM-001", "Package 1", 5.0, new Date(), 20.0);
            Item item2 = new Item("ITEM-002", "Package 2", 5.0, new Date(), 20.0);
            zone1.addItem(item1);
            zone1.addItem(item2);
            
            TransportTask lowPriority = new TransportTask(3, item1, zone1, zone2);
            TransportTask highPriority = new TransportTask(9, item2, zone1, zone2);
            
            assertTrue(highPriority.getPriority() > lowPriority.getPriority(), 
                      "High priority task has higher priority value");
            
        } catch (Exception e) {
            testFailed("Task priority comparison", e.getMessage());
        }
        
        // Test 3: ChargeTask creation
        try {
            ChargingStation station = new ChargingStation("CS-001", new Position(200, 700));
            AGV agv = new AGV("AGV-001", new Position(0, 0));
            agv.setBatteryLevel(15.0);
            
            ChargeTask chargeTask = new ChargeTask(10, station, agv);
            
            assertEquals(10, chargeTask.getPriority(), "ChargeTask high priority");
            assertEquals("WAITING", chargeTask.getStatus(), "Initial charge task status");
            assertTrue(chargeTask.getName().contains("Charge"), "Task name contains 'Charge'");
            assertTrue(chargeTask.getName().contains(agv.getId()), "Task name contains AGV ID");
            assertEquals(agv, chargeTask.getTargetAGV(), "ChargeTask has correct target AGV");
            
        } catch (Exception e) {
            testFailed("ChargeTask creation", e.getMessage());
        }
        
        // Test 4: Task ID uniqueness
        try {
            StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
            StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
            
            Item item1 = new Item("ITEM-001", "Package 1", 5.0, new Date(), 20.0);
            Item item2 = new Item("ITEM-002", "Package 2", 5.0, new Date(), 20.0);
            zone1.addItem(item1);
            zone1.addItem(item2);
            
            TransportTask task1 = new TransportTask(5, item1, zone1, zone2);
            TransportTask task2 = new TransportTask(5, item2, zone1, zone2);
            
            assertFalse(task1.getId() == task2.getId(), "Each task has unique ID");
            
        } catch (Exception e) {
            testFailed("Task ID uniqueness", e.getMessage());
        }
        
        // Test 5: Task status changes
        try {
            StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
            StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
            Item item = new Item("ITEM-001", "Package 1", 5.0, new Date(), 20.0);
            zone1.addItem(item);
            
            TransportTask task = new TransportTask(5, item, zone1, zone2);
            
            assertEquals("WAITING", task.getStatus(), "Initial status: WAITING");
            
            task.setStatus("RUNNING");
            assertEquals("RUNNING", task.getStatus(), "Status changed to RUNNING");
            
            task.setStatus("COMPLETED");
            assertEquals("COMPLETED", task.getStatus(), "Status changed to COMPLETED");
            
        } catch (Exception e) {
            testFailed("Task status changes", e.getMessage());
        }
        
        // Test 6: Task AGV assignment
        try {
            StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
            StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
            Item item = new Item("ITEM-001", "Package 1", 5.0, new Date(), 20.0);
            zone1.addItem(item);
            
            TransportTask task = new TransportTask(5, item, zone1, zone2);
            AGV agv = new AGV("AGV-001", new Position(80, 50));
            
            assertNull(task.getAssignedAGV(), "Initially no AGV assigned");
            
            task.setAssignedAGV(agv);
            assertEquals(agv, task.getAssignedAGV(), "AGV assigned to task");
            
        } catch (Exception e) {
            testFailed("Task AGV assignment", e.getMessage());
        }
        
        // Test 7: TransportTask item retrieval
        try {
            StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
            StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
            Item item = new Item("ITEM-001", "Frozen Pizza", 2.5, new Date(), -20.0);
            zone1.addItem(item);
            
            TransportTask task = new TransportTask(5, item, zone1, zone2);
            
            assertEquals(item, task.getItem(), "Task has correct item");
            assertEquals(zone1, task.getSourceZone(), "Task has correct source zone");
            assertEquals(zone2, task.getTargetZone(), "Task has correct target zone");
            
        } catch (Exception e) {
            testFailed("TransportTask item retrieval", e.getMessage());
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private static void printTestSection(String section) {
        System.out.println("\n ");
        System.out.println("   " + String.format("%-46s", section) + "   ");
        System.out.println(" ");
    }
    
    private static void assertEquals(Object expected, Object actual, String testName) {
        if (expected.equals(actual)) {
            testPassed(testName);
        } else {
            testFailed(testName, "Expected: " + expected + ", Got: " + actual);
        }
    }
    
    private static void assertEquals(double expected, double actual, String testName) {
        if (Math.abs(expected - actual) < 0.001) {
            testPassed(testName);
        } else {
            testFailed(testName, "Expected: " + expected + ", Got: " + actual);
        }
    }
    
    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            testPassed(testName);
        } else {
            testFailed(testName, "Expected: true, Got: false");
        }
    }
    
    private static void assertFalse(boolean condition, String testName) {
        if (!condition) {
            testPassed(testName);
        } else {
            testFailed(testName, "Expected: false, Got: true");
        }
    }
    
    private static void assertNull(Object obj, String testName) {
        if (obj == null) {
            testPassed(testName);
        } else {
            testFailed(testName, "Expected: null, Got: " + obj);
        }
    }
    
    private static void testPassed(String testName) {
        System.out.println("  PASS: " + testName);
        testsPassed++;
    }
    
    private static void testFailed(String testName, String reason) {
        System.out.println("  FAIL: " + testName);
        System.out.println("       " + reason);
        testsFailed++;
    }
    
    private static void printSummary() {
        System.out.println("\n ");
        System.out.println("    TEST SUMMARY - MODEL CLASSES                   ");
        System.out.println("    Tests Passed: " + testsPassed);
        System.out.println("    Tests Failed: " + testsFailed);
        int total = testsPassed + testsFailed;
        int successRate = total > 0 ? (100 * testsPassed / total) : 0;
        System.out.println("    Success Rate: " + successRate + "%");
        System.out.println(" \n");
    }
}
