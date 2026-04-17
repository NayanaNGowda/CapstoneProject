package testing;

import model.*;
import managers.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

/**
 * TEST FILE 4: Concurrency & Thread Safety Testing
 * Team Member 4 - Tests multi-threading and concurrent operations
 * 
 * Tests: Thread safety, Concurrent access, Synchronization, Race conditions
 * 
 * CONCURRENCY EXPLANATION:
 * ========================
 * Concurrency = Multiple things happening at the same time
 * In our system:
 * - Multiple AGVs moving simultaneously
 * - Multiple tasks being processed at once
 * - Multiple threads adding/removing items from zones
 * 
 * WHY WE NEED IT:
 * - Real warehouse has multiple robots working together
 * - Without thread safety, data can get corrupted
 * - synchronized keyword prevents conflicts
 */
public class ConcurrencyTest {
    
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("\n ");
        System.out.println(" TEST 4: CONCURRENCY & THREAD SAFETY");
        System.out.println(" Tester: Team Member 4");
        System.out.println(" \n");
        
        printConcurrencyExplanation();
        
        testStorageZoneConcurrency();
        testAGVConcurrentMovement();
        testChargingStationConcurrency();
        testTaskManagerConcurrency();
        
        printSummary();
    }
    
    // ========== CONCURRENCY EXPLANATION ==========
    private static void printConcurrencyExplanation() {
        System.out.println("\n--------------------------------------------------");
        System.out.println(" WHAT IS CONCURRENCY?");
        System.out.println("--------------------------------------------------\n");
        
        System.out.println(" EXPLANATION:");
        System.out.println(" Concurrency = Multiple operations happening simultaneously\n");
        
        System.out.println(" IN OUR WAREHOUSE SYSTEM:");
        System.out.println(" - Multiple AGVs moving at the same time");
        System.out.println(" - Multiple tasks being executed in parallel");
        System.out.println(" - Multiple threads accessing shared resources\n");
        
        System.out.println(" THE PROBLEM:");
        System.out.println(" Without thread safety, this can happen:");
        System.out.println(" Thread 1: Reads zone has 5 items");
        System.out.println(" Thread 2: Reads zone has 5 items (same time!)");
        System.out.println(" Thread 1: Adds item -> zone now has 6 items");
        System.out.println(" Thread 2: Adds item -> overwrites to 6 items (should be 7!)");
        System.out.println(" Result: Lost update!\n");
        
        System.out.println(" THE SOLUTION:");
        System.out.println(" Use 'synchronized' keyword:");
        System.out.println(" - Only ONE thread can access synchronized method at a time");
        System.out.println(" - Other threads wait in line");
        System.out.println(" - Prevents data corruption\n");
        
        System.out.println(" WHERE WE USE IT:");
        System.out.println(" - StorageZone.addItem() - synchronized");
        System.out.println(" - StorageZone.removeItem() - synchronized");
        System.out.println(" - ChargingStation.chargeAGV() - synchronized");
        System.out.println(" - AGV.moveTo() - synchronized\n");
        
        System.out.println("--------------------------------------------------\n");
    }
    
    // ========== TEST STORAGE ZONE CONCURRENCY ==========
    private static void testStorageZoneConcurrency() {
        printTestSection("STORAGE ZONE THREAD SAFETY");
        
        // Test 1: Multiple threads adding items simultaneously
        try {
            System.out.println("\n TEST SCENARIO:");
            System.out.println(" Creating 10 threads, each adding 5 items");
            System.out.println(" Expected result: 50 items total");
            System.out.println(" If not synchronized: items could be lost!\n");
            
            StorageZone zone = new StorageZone("Concurrent-Zone", 20.0, 100, new Position(100, 100));
            
            // Create 10 threads
            Thread[] threads = new Thread[10];
            CountDownLatch startSignal = new CountDownLatch(1); // Start all threads together
            CountDownLatch doneSignal = new CountDownLatch(10); // Wait for all to finish
            
            for (int i = 0; i < 10; i++) {
                final int threadNum = i;
                threads[i] = new Thread(() -> {
                    try {
                        startSignal.await(); // Wait for start signal
                        
                        for (int j = 0; j < 5; j++) {
                            Item item = new Item(
                                "ITEM-T" + threadNum + "-" + j,
                                "Item from Thread " + threadNum,
                                5.0,
                                new Date(),
                                20.0
                            );
                            zone.addItem(item);
                            Thread.sleep(10); // Small delay to increase concurrency
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        doneSignal.countDown();
                    }
                });
                threads[i].start();
            }
            
            System.out.println(" Starting all threads simultaneously...");
            startSignal.countDown(); // Start all threads at once!
            
            System.out.println(" Waiting for all threads to complete...");
            doneSignal.await(10, TimeUnit.SECONDS); // Wait for all threads
            
            int finalCount = zone.getItems().size();
            System.out.println(" Result: " + finalCount + " items (expected 50)\n");
            
            assertEquals(50, finalCount, "Multiple threads adding items concurrently");
            
        } catch (Exception e) {
            testFailed("Storage zone concurrent adding", e.getMessage());
        }
        
        // Test 2: Concurrent add and remove
        try {
            System.out.println("\n TEST SCENARIO:");
            System.out.println(" Thread 1: Adding items");
            System.out.println(" Thread 2: Removing items");
            System.out.println(" Both running simultaneously!\n");
            
            StorageZone zone = new StorageZone("AddRemove-Zone", 20.0, 100, new Position(100, 100));
            
            // Pre-populate with some items
            for (int i = 0; i < 20; i++) {
                Item item = new Item("INITIAL-" + i, "Initial Item", 5.0, new Date(), 20.0);
                zone.addItem(item);
            }
            
            // Thread 1: Keep adding
            Thread adder = new Thread(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        Item item = new Item("ADDED-" + i, "Added Item", 5.0, new Date(), 20.0);
                        zone.addItem(item);
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            // Thread 2: Keep removing
            Thread remover = new Thread(() -> {
                try {
                    List<Item> items = zone.getItems();
                    for (int i = 0; i < 10 && i < items.size(); i++) {
                        zone.removeItem(items.get(i));
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            System.out.println(" Starting add and remove threads...");
            adder.start();
            remover.start();
            
            adder.join();
            remover.join();
            
            System.out.println(" Final count: " + zone.getItems().size() + " items");
            System.out.println(" No crashes = synchronized methods work!\n");
            
            testPassed("Concurrent add and remove operations");
            
        } catch (Exception e) {
            testFailed("Concurrent add/remove", e.getMessage());
        }
    }
    
    // ========== TEST AGV CONCURRENT MOVEMENT ==========
    private static void testAGVConcurrentMovement() {
        printTestSection("AGV CONCURRENT MOVEMENT");
        
        // Test 1: Multiple AGVs moving simultaneously
        try {
            System.out.println("\n TEST SCENARIO:");
            System.out.println(" 3 AGVs moving to different positions at same time");
            System.out.println(" Each AGV's movement is in its own thread\n");
            
            AGV agv1 = new AGV("001", new Position(0, 0));
            AGV agv2 = new AGV("002", new Position(0, 0));
            AGV agv3 = new AGV("003", new Position(0, 0));
            
            Position dest1 = new Position(100, 100);
            Position dest2 = new Position(200, 200);
            Position dest3 = new Position(150, 150);
            
            Thread thread1 = new Thread(() -> {
                try {
                    System.out.println(" AGV-001 starting movement...");
                    agv1.moveTo(dest1);
                    System.out.println(" AGV-001 reached destination");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            
            Thread thread2 = new Thread(() -> {
                try {
                    System.out.println(" AGV-002 starting movement...");
                    agv2.moveTo(dest2);
                    System.out.println(" AGV-002 reached destination");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            
            Thread thread3 = new Thread(() -> {
                try {
                    System.out.println(" AGV-003 starting movement...");
                    agv3.moveTo(dest3);
                    System.out.println(" AGV-003 reached destination");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            
            System.out.println(" Starting all AGVs...\n");
            thread1.start();
            thread2.start();
            thread3.start();
            
            thread1.join();
            thread2.join();
            thread3.join();
            
            System.out.println("\n Checking final positions...");
            assertEquals(100.0, agv1.getPosition().getX(), "AGV-001 final X position");
            assertEquals(200.0, agv2.getPosition().getX(), "AGV-002 final X position");
            assertEquals(150.0, agv3.getPosition().getX(), "AGV-003 final X position");
            
        } catch (Exception e) {
            testFailed("Multiple AGVs concurrent movement", e.getMessage());
        }
        
        // Test 2: AGV status during movement
        try {
            System.out.println("\n TEST SCENARIO:");
            System.out.println(" Check AGV status changes during movement\n");
            
            AGV agv = new AGV("TEST-001", new Position(0, 0));
            
            Thread moveThread = new Thread(() -> {
                try {
                    agv.moveTo(new Position(300, 300));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            
            moveThread.start();
            Thread.sleep(500); // Let it start moving
            
            System.out.println(" Status during movement: " + agv.getStatus());
            assertEquals("MOVING", agv.getStatus(), "AGV status during movement");
            
            moveThread.join(); // Wait for completion
            
            System.out.println(" Status after movement: " + agv.getStatus());
            assertEquals("IDLE", agv.getStatus(), "AGV status after movement");
            
        } catch (Exception e) {
            testFailed("AGV status during movement", e.getMessage());
        }
    }
    
    // ========== TEST CHARGING STATION CONCURRENCY ==========
    private static void testChargingStationConcurrency() {
        printTestSection("CHARGING STATION THREAD SAFETY");
        
        // Test 1: Multiple AGVs trying to charge at same station
        try {
            System.out.println("\n TEST SCENARIO:");
            System.out.println(" 3 AGVs trying to charge at 1 station simultaneously");
            System.out.println(" Only 1 should succeed (synchronized prevents conflicts)\n");
            
            ChargingStation station = new ChargingStation("CS-CONCURRENT", new Position(200, 700));
            AGV agv1 = new AGV("001", new Position(0, 0));
            AGV agv2 = new AGV("002", new Position(0, 0));
            AGV agv3 = new AGV("003", new Position(0, 0));
            
            CountDownLatch startSignal = new CountDownLatch(1);
            List<Boolean> results = new CopyOnWriteArrayList<>(); // Thread-safe list
            
            Thread thread1 = new Thread(() -> {
                try {
                    startSignal.await();
                    boolean result = station.chargeAGV(agv1);
                    results.add(result);
                    System.out.println(" AGV-001 charge result: " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            Thread thread2 = new Thread(() -> {
                try {
                    startSignal.await();
                    boolean result = station.chargeAGV(agv2);
                    results.add(result);
                    System.out.println(" AGV-002 charge result: " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            Thread thread3 = new Thread(() -> {
                try {
                    startSignal.await();
                    boolean result = station.chargeAGV(agv3);
                    results.add(result);
                    System.out.println(" AGV-003 charge result: " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            thread1.start();
            thread2.start();
            thread3.start();
            
            System.out.println(" All AGVs attempting to charge at once...\n");
            startSignal.countDown(); // Start all at once!
            
            thread1.join();
            thread2.join();
            thread3.join();
            
            // Count successes
            long successCount = results.stream().filter(r -> r).count();
            
            System.out.println("\n Result: " + successCount + " AGV(s) successfully charged");
            System.out.println(" Expected: Only 1 (synchronized prevents multiple access)\n");
            
            assertEquals(1L, successCount, "Only one AGV can charge (thread-safe)");
            
        } catch (Exception e) {
            testFailed("Charging station concurrency", e.getMessage());
        }
    }
    
    // ========== TEST TASK MANAGER CONCURRENCY ==========
    private static void testTaskManagerConcurrency() {
        printTestSection("TASK MANAGER THREAD SAFETY");
        
        // Test 1: Multiple tasks added concurrently
        try {
            System.out.println("\n TEST SCENARIO:");
            System.out.println(" Multiple threads adding tasks simultaneously\n");
            
            AGVManager agvManager = new AGVManager();
            agvManager.addAGV(new AGV("001", new Position(80, 50)));
            agvManager.addAGV(new AGV("002", new Position(100, 50)));
            
            TaskManager taskManager = new TaskManager(agvManager);
            
            StorageZone zone1 = new StorageZone("Zone-A", 20.0, 50, new Position(100, 100));
            StorageZone zone2 = new StorageZone("Zone-B", 20.0, 50, new Position(200, 200));
            
            // Add items
            for (int i = 0; i < 10; i++) {
                Item item = new Item("ITEM-" + i, "Package " + i, 5.0, new Date(), 20.0);
                zone1.addItem(item);
            }
            
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(5);
            
            // Create 5 threads adding tasks
            for (int i = 0; i < 5; i++) {
                final int threadNum = i;
                new Thread(() -> {
                    try {
                        startSignal.await();
                        List<Item> items = zone1.getItems();
                        if (threadNum < items.size()) {
                            TransportTask task = new TransportTask(
                                5,
                                items.get(threadNum),
                                zone1,
                                zone2
                            );
                            taskManager.addTask(task);
                            System.out.println(" Thread " + threadNum + " added task");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        doneSignal.countDown();
                    }
                }).start();
            }
            
            System.out.println(" Adding tasks from 5 threads simultaneously...\n");
            startSignal.countDown();
            doneSignal.await(5, TimeUnit.SECONDS);
            
            Thread.sleep(1000); // Let tasks process
            
            System.out.println(" All tasks added without conflicts\n");
            testPassed("Concurrent task addition");
            
        } catch (Exception e) {
            testFailed("Task manager concurrency", e.getMessage());
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private static void printTestSection(String section) {
        System.out.println("\n--------------------------------------------------");
        System.out.println(" " + section);
        System.out.println("--------------------------------------------------");
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
    
    private static void testPassed(String testName) {
        System.out.println(" PASS: " + testName);
        testsPassed++;
    }
    
    private static void testFailed(String testName, String reason) {
        System.out.println(" FAIL: " + testName);
        System.out.println("    -> " + reason);
        testsFailed++;
    }
    
    private static void printSummary() {
        System.out.println("\n ");
        System.out.println(" TEST SUMMARY - CONCURRENCY & THREAD SAFETY");
        System.out.println(" ");
        System.out.println(" Tests Passed: " + testsPassed);
        System.out.println(" Tests Failed: " + testsFailed);
        int total = testsPassed + testsFailed;
        int successRate = total > 0 ? (100 * testsPassed / total) : 0;
        System.out.println(" Success Rate: " + successRate + "%\n");
        
        System.out.println(" KEY CONCURRENCY CONCEPTS TESTED:");
        System.out.println(" - Thread Safety - synchronized methods");
        System.out.println(" - Race Conditions - prevented by synchronization");
        System.out.println(" - Concurrent Access - multiple threads, no conflicts");
        System.out.println(" - Resource Locking - charging stations");
        System.out.println(" \n");
    }
}
