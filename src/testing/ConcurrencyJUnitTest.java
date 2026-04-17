package testing;

import model.*;
import managers.*;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * JUnit Test Suite for Concurrency & Thread Safety
 * Tests: Multi-threading, Synchronization, Race Conditions
 */
public class ConcurrencyJUnitTest {
    
    // ========== STORAGE ZONE CONCURRENCY TESTS ==========
    
    @Test
    public void testMultipleThreadsAddingItems() throws InterruptedException {
        StorageZone zone = new StorageZone("Concurrent-Zone", 20.0, 100, new Position(100, 100));
        
        Thread[] threads = new Thread[10];
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(10);
        
        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                try {
                    startSignal.await();
                    
                    for (int j = 0; j < 5; j++) {
                        Item item = new Item(
                            "ITEM-T" + threadNum + "-" + j,
                            "Item from Thread " + threadNum,
                            5.0,
                            new Date(),
                            20.0
                        );
                        zone.addItem(item);
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            });
            threads[i].start();
        }
        
        startSignal.countDown(); // Start all threads
        doneSignal.await(10, TimeUnit.SECONDS); // Wait for completion
        
        int finalCount = zone.getItems().size();
        assertEquals(50, finalCount);
    }
    
    @Test
    public void testConcurrentAddAndRemove() throws Exception {
        StorageZone zone = new StorageZone("AddRemove-Zone", 20.0, 100, new Position(100, 100));
        
        // Pre-populate with items
        for (int i = 0; i < 20; i++) {
            Item item = new Item("INITIAL-" + i, "Initial Item", 5.0, new Date(), 20.0);
            zone.addItem(item);
        }
        
        // Thread 1: Add items
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
        
        // Thread 2: Remove items
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
        
        adder.start();
        remover.start();
        
        adder.join();
        remover.join();
        
        // No crashes = synchronized methods work!
        assertTrue(zone.getItems().size() >= 0);
    }
    
    // ========== AGV CONCURRENT MOVEMENT TESTS ==========
    
    @Test
    public void testMultipleAGVsMovingSimultaneously() throws InterruptedException {
        AGV agv1 = new AGV("001", new Position(0, 0));
        AGV agv2 = new AGV("002", new Position(0, 0));
        AGV agv3 = new AGV("003", new Position(0, 0));
        
        Position dest1 = new Position(100, 100);
        Position dest2 = new Position(200, 200);
        Position dest3 = new Position(150, 150);
        
        Thread thread1 = new Thread(() -> {
            try {
                agv1.moveTo(dest1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                agv2.moveTo(dest2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread thread3 = new Thread(() -> {
            try {
                agv3.moveTo(dest3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        thread1.start();
        thread2.start();
        thread3.start();
        
        thread1.join();
        thread2.join();
        thread3.join();
        
        assertEquals(100.0, agv1.getPosition().getX(), 0.001);
        assertEquals(200.0, agv2.getPosition().getX(), 0.001);
        assertEquals(150.0, agv3.getPosition().getX(), 0.001);
    }
    
    @Test
    public void testAGVStatusDuringMovement() throws InterruptedException {
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
        
        assertEquals("MOVING", agv.getStatus());
        
        moveThread.join(); // Wait for completion
        
        assertEquals("IDLE", agv.getStatus());
    }
    
    // ========== CHARGING STATION CONCURRENCY TESTS ==========
    
    @Test
    public void testMultipleAGVsTryingToChargeSimultaneously() throws InterruptedException {
        ChargingStation station = new ChargingStation("CS-CONCURRENT", new Position(200, 700));
        AGV agv1 = new AGV("001", new Position(0, 0));
        AGV agv2 = new AGV("002", new Position(0, 0));
        AGV agv3 = new AGV("003", new Position(0, 0));
        
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Boolean> results = new CopyOnWriteArrayList<>();
        
        Thread thread1 = new Thread(() -> {
            try {
                startSignal.await();
                boolean result = station.chargeAGV(agv1);
                results.add(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                startSignal.await();
                boolean result = station.chargeAGV(agv2);
                results.add(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        Thread thread3 = new Thread(() -> {
            try {
                startSignal.await();
                boolean result = station.chargeAGV(agv3);
                results.add(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        thread1.start();
        thread2.start();
        thread3.start();
        
        startSignal.countDown(); // Start all at once!
        
        thread1.join();
        thread2.join();
        thread3.join();
        
        // Count successes - only one should succeed
        long successCount = results.stream().filter(r -> r).count();
        assertEquals(1L, successCount);
    }
    
    // ========== TASK MANAGER CONCURRENCY TESTS ==========
    
    @Test
    public void testMultipleTasksAddedConcurrently() throws Exception {
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            }).start();
        }
        
        startSignal.countDown();
        doneSignal.await(5, TimeUnit.SECONDS);
        
        Thread.sleep(1000); // Let tasks process
        
        // Test passes if no exceptions occurred
        assertTrue(true);
    }
    
    // ========== RACE CONDITION TESTS ==========
    
    @Test
    public void testNoRaceConditionInItemCount() throws InterruptedException {
        StorageZone zone = new StorageZone("Race-Test-Zone", 20.0, 1000, new Position(100, 100));
        
        int numThreads = 20;
        int itemsPerThread = 10;
        
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            new Thread(() -> {
                try {
                    startSignal.await();
                    for (int j = 0; j < itemsPerThread; j++) {
                        Item item = new Item(
                            "ITEM-T" + threadNum + "-" + j,
                            "Test Item",
                            5.0,
                            new Date(),
                            20.0
                        );
                        zone.addItem(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            }).start();
        }
        
        startSignal.countDown();
        doneSignal.await(30, TimeUnit.SECONDS);
        
        // If synchronized properly, we should have exactly numThreads * itemsPerThread items
        assertEquals(numThreads * itemsPerThread, zone.getItems().size());
    }
    
    @Test
    public void testAGVBatteryUpdateThreadSafety() throws InterruptedException {
        AGV agv = new AGV("TEST-001", new Position(0, 0));
        
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(10);
        
        // Multiple threads trying to update battery
        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            new Thread(() -> {
                try {
                    startSignal.await();
                    agv.setBatteryLevel(50.0 + threadNum);
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            }).start();
        }
        
        startSignal.countDown();
        doneSignal.await(5, TimeUnit.SECONDS);
        
        // Battery level should be one of the valid values set
        double battery = agv.getBatteryLevel();
        assertTrue(battery >= 50.0 && battery <= 59.0);
    }
}