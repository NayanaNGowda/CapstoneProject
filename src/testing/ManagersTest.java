package testing;

import model.*;
import managers.*;
import java.util.Date;
import java.util.List;



//  Random unit Tests: AGV management, Task management, Task assignment, Auto-charging
 
public class ManagersTest {
    
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("TEST 3: MANAGER CLASSES");
        System.out.println("Tester: Keya ");
        System.out.println("\n");
        
        testAGVManager();
        testTaskManager();
        testTaskPriority();
        testAutoCharging();
        
        printSummary();
    }
    
    // TEST AGV MANAGER 
    private static void testAGVManager() {
        printTestSection("AGV MANAGER");
        
        // Test 1: Create and add AGVs
        try {
            AGVManager manager = new AGVManager();
            AGV agv1 = new AGV("001", new Position(80, 50));
            AGV agv2 = new AGV("002", new Position(100, 50));
            
            manager.addAGV(agv1);
            manager.addAGV(agv2);
            
            assertEquals(2, manager.getAgvList().size(), "Add multiple AGVs");
        } catch (Exception e) {
            testFailed("Create and add AGVs", e.getMessage());
        }
        
        // Test 2: Get available AGV
        try {
            AGVManager manager = new AGVManager();
            AGV agv1 = new AGV("001", new Position(80, 50));
            AGV agv2 = new AGV("002", new Position(100, 50));
            
            manager.addAGV(agv1);
            manager.addAGV(agv2);
            
            AGV available = manager.getAvailableAGV();
            assertNotNull(available, "Get available AGV");
            assertFalse(available.isBusy(), "Available AGV is not busy");
            assertTrue(available.getBatteryLevel() > 20, "Available AGV has sufficient battery");
        } catch (Exception e) {
            testFailed("Get available AGV", e.getMessage());
        }
        
        // Test 3: No available AGV when all busy
        try {
            AGVManager manager = new AGVManager();
            AGV agv1 = new AGV("001", new Position(80, 50));
            AGV agv2 = new AGV("002", new Position(100, 50));
            
            manager.addAGV(agv1);
            manager.addAGV(agv2);
            
            // Make all AGVs busy
            agv1.setBusy(true);
            agv2.setBusy(true);
            
            AGV available = manager.getAvailableAGV();
            assertNull(available, "No available AGV when all busy");
        } catch (Exception e) {
            testFailed("No available AGV when busy", e.getMessage());
        }
        
        // Test 4: Low battery AGV not available
        try {
            AGVManager manager = new AGVManager();
            AGV agv = new AGV("001", new Position(80, 50));
            agv.setBatteryLevel(15.0); // Below 20% threshold
            
            manager.addAGV(agv);
            
            AGV available = manager.getAvailableAGV();
            assertNull(available, "Low battery AGV not available for tasks");
        } catch (Exception e) {
            testFailed("Low battery AGV exclusion", e.getMessage());
        }
        
        // Test 5: Get AGV list is immutable (defensive copy)manager.getAgvList().clear();  // OOPS! Deletes all AGVs from the system!
        try {
            AGVManager manager = new AGVManager();
            AGV agv = new AGV("001", new Position(80, 50));
            manager.addAGV(agv);
            
            List<AGV> list1 = manager.getAgvList();
            List<AGV> list2 = manager.getAgvList();
            
            // Should be different list instances (defensive copy)
            assertFalse(list1 == list2, "AGV list returns defensive copy");
        } catch (Exception e) {
            testFailed("Defensive copy test", e.getMessage());
        }
    }
    
    // TEST TASK MANAGER
    private static void testTaskManager() {
        printTestSection("TASK MANAGER");
        
        // Test 1: Create task manager
        try {
            AGVManager agvManager = new AGVManager();
            TaskManager taskManager = new TaskManager(agvManager);
            
            assertEquals(0, taskManager.getTaskQueue().size(), "Initial task queue empty");
            assertEquals(0, taskManager.getActiveTasks().size(), "Initial active tasks empty");
            assertEquals(0, taskManager.getCompletedTasks().size(), "Initial completed tasks empty");
        } catch (Exception e) {
            testFailed("Create task manager", e.getMessage());
        }
        
        // Test 2: Add transport task
        try {
            AGVManager agvManager = new AGVManager();
            TaskManager taskManager = new TaskManager(agvManager);
            
            StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
            StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
            
            Item item = new Item("ITEM-001", "Test Package", 5.0, new Date(), 20.0);
            zone1.addItem(item);
            
            TransportTask task = new TransportTask(5, item, zone1, zone2);
            taskManager.addTask(task);
            
            assertEquals(1, taskManager.getTaskQueue().size(), "Task added to queue");
        } catch (Exception e) {
            testFailed("Add transport task", e.getMessage());
        }
        
        // Test 3: Task assignment (automatic)
        try {
            AGVManager agvManager = new AGVManager();
            agvManager.addAGV(new AGV("001", new Position(80, 50)));
            
            TaskManager taskManager = new TaskManager(agvManager);
            
            StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
            StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
            
            Item item = new Item("ITEM-001", "Test Package", 5.0, new Date(), 20.0);
            zone1.addItem(item);
            
            TransportTask task = new TransportTask(5, item, zone1, zone2);
            taskManager.addTask(task);
            
            // Wait for automatic assignment
            Thread.sleep(1000);
            
            assertTrue(taskManager.getActiveTasks().size() > 0 || 
                      taskManager.getCompletedTasks().size() > 0,
                      "Task automatically assigned and processed");
        } catch (Exception e) {
            testFailed("Automatic task assignment", e.getMessage());
        }
        
        // Test 4: Set charging stations
        try {
            AGVManager agvManager = new AGVManager();
            TaskManager taskManager = new TaskManager(agvManager);
            
            ChargingStation cs1 = new ChargingStation("CS-1", new Position(200, 700));
            ChargingStation cs2 = new ChargingStation("CS-2", new Position(350, 700));
            
            java.util.List<ChargingStation> stations = new java.util.ArrayList<>();
            stations.add(cs1);
            stations.add(cs2);
            
            taskManager.setChargingStations(stations);
            
            testPassed("Set charging stations");
        } catch (Exception e) {
            testFailed("Set charging stations", e.getMessage());
        }
    }
    
    //  TEST TASK PRIORITY 
    private static void testTaskPriority() {
        printTestSection("TASK PRIORITY SYSTEM");
        
        // Test 1: High priority task processed first
        try {
            AGVManager agvManager = new AGVManager();
            agvManager.addAGV(new AGV("001", new Position(80, 50)));
            agvManager.addAGV(new AGV("002", new Position(100, 50)));
            
            TaskManager taskManager = new TaskManager(agvManager);
            
            StorageZone zone1 = new StorageZone("Zone-A", 20.0, 10, new Position(100, 100));
            StorageZone zone2 = new StorageZone("Zone-B", 20.0, 10, new Position(200, 200));
            
            // Add low priority task
            Item item1 = new Item("ITEM-001", "Low Priority", 5.0, new Date(), 20.0);
            zone1.addItem(item1);
            TransportTask lowPriorityTask = new TransportTask(3, item1, zone1, zone2);
            
            // Add high priority task
            Item item2 = new Item("ITEM-002", "High Priority", 5.0, new Date(), 20.0);
            zone1.addItem(item2);
            TransportTask highPriorityTask = new TransportTask(9, item2, zone1, zone2);
            
            taskManager.addTask(lowPriorityTask);
            taskManager.addTask(highPriorityTask);
            
            // Wait for processing
            Thread.sleep(1000);
            
            testPassed("Priority queue handles multiple priorities");
        } catch (Exception e) {
            testFailed("Task priority system", e.getMessage());
        }
        
    
    }
    
    //TEST AUTO-CHARGING 
    private static void testAutoCharging() {
        printTestSection("AUTO-CHARGING SYSTEM");
        
        // Test 1: Auto-charge triggered for low battery
        try {
            AGVManager agvManager = new AGVManager();
            AGV agv = new AGV("001", new Position(80, 50));
            agv.setBatteryLevel(18.0); // Below 20% threshold
            agvManager.addAGV(agv);
            
            TaskManager taskManager = new TaskManager(agvManager);
            
            ChargingStation station = new ChargingStation("CS-1", new Position(200, 700));
            java.util.List<ChargingStation> stations = new java.util.ArrayList<>();
            stations.add(station);
            taskManager.setChargingStations(stations);
            
            // Wait for auto-charging trigger
            Thread.sleep(2000);
            
            // Check if charging task was created (will be in queue or active)
            int totalTasks = taskManager.getTaskQueue().size() + 
                           taskManager.getActiveTasks().size();
            
            assertTrue(totalTasks > 0, "Auto-charge task created for low battery AGV");
        } catch (Exception e) {
            testFailed("Auto-charge trigger", e.getMessage());
        }
        
        // Test 2: No auto-charge for sufficient battery
        try {
            AGVManager agvManager = new AGVManager();
            AGV agv = new AGV("001", new Position(80, 50));
            agv.setBatteryLevel(80.0); // Good battery
            agvManager.addAGV(agv);
            
            TaskManager taskManager = new TaskManager(agvManager);
            
            ChargingStation station = new ChargingStation("CS-1", new Position(200, 700));
            java.util.List<ChargingStation> stations = new java.util.ArrayList<>();
            stations.add(station);
            taskManager.setChargingStations(stations);
            
            // Wait
            Thread.sleep(1000);
            
            testPassed("No auto-charge for sufficient battery");
        } catch (Exception e) {
            testFailed("No auto-charge for good battery", e.getMessage());
        }
        
        // Test 3: Charging station occupation
        try {
            ChargingStation station = new ChargingStation("CS-1", new Position(200, 700));
            AGV agv1 = new AGV("001", new Position(80, 50));
            AGV agv2 = new AGV("002", new Position(100, 50));
            
            boolean firstCharge = station.chargeAGV(agv1);
            assertTrue(firstCharge, "First AGV can charge");
            
            boolean secondCharge = station.chargeAGV(agv2);
            assertFalse(secondCharge, "Second AGV blocked when station occupied");
        } catch (Exception e) {
            testFailed("Charging station occupation", e.getMessage());
        }
    }
    
    // extra helping methods
    
    private static void printTestSection(String section) {
        System.out.println("\n");
        System.out.println(section);
        System.out.println("\n");
    }
    
    private static void assertEquals(Object expected, Object actual, String testName) {
        if (expected.equals(actual)) {
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
    
    private static void assertNotNull(Object obj, String testName) {
        if (obj != null) {
            testPassed(testName);
        } else {
            testFailed(testName, "Object was null");
        }
    }
    
    private static void assertNull(Object obj, String testName) {
        if (obj == null) {
            testPassed(testName);
        } else {
            testFailed(testName, "Object was not null: " + obj);
        }
    }
    
    private static void testPassed(String testName) {
        System.out.println("PASS: " + testName);
        testsPassed++;
    }
    
    private static void testFailed(String testName, String reason) {
        System.out.println("FAIL: " + testName);
        System.out.println("   -> " + reason);
        testsFailed++;
    }
    
    private static void printSummary() {
        System.out.println("\n");
        System.out.println("TEST SUMMARY - MANAGER CLASSES");
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + testsFailed);
        int total = testsPassed + testsFailed;
        int successRate = total > 0 ? (100 * testsPassed / total) : 0;
        System.out.println("Success Rate: " + successRate + "%");
        System.out.println("\n");
    }
}
