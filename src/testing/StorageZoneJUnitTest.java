package testing;

import model.*;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.Date;

/**
 * JUnit Test Suite for Storage Zone Management
 * Tests: Storage zone operations, inventory, capacity, temperature
 */
public class StorageZoneJUnitTest {

    // ========== STORAGE ZONE CREATION TESTS ==========

    @Test
    public void testColdZoneCreation() {
        StorageZone coldZone = new StorageZone("Cold-Zone-A", -20.0, 10, new Position(250, 150));
        assertEquals("Cold-Zone-A", coldZone.getZoneName());
        assertEquals(-20.0, coldZone.getTemperature(), 0.001);
        assertEquals(10, coldZone.getCapacity());
        assertEquals(0, coldZone.getItems().size());
    }

    @Test
    public void testRoomTemperatureZoneCreation() {
        StorageZone roomZone = new StorageZone("Room-Zone-A", 20.0, 15, new Position(250, 350));
        assertEquals(20.0, roomZone.getTemperature(), 0.001);
        assertEquals(15, roomZone.getCapacity());
    }

    @Test
    public void testHotZoneCreation() {
        StorageZone hotZone = new StorageZone("Hot-Zone-A", 30.0, 8, new Position(250, 550));
        assertEquals(30.0, hotZone.getTemperature(), 0.001);
        assertEquals(8, hotZone.getCapacity());
    }

    // ========== ADDING ITEMS TESTS ==========

    @Test
    public void testAddSingleItem() throws Exception {
        StorageZone zone = new StorageZone("Test-Zone", 20.0, 10, new Position(100, 100));
        Item item = new Item("ITEM-001", "Package-1", 5.0, new Date(), 20.0);
        zone.addItem(item); // Exception will be automatically handled by JUnit
        
        assertEquals(1, zone.getItems().size());
        assertTrue(zone.getItems().contains(item));
    }

    @Test
    public void testAddMultipleItems() throws Exception {
        StorageZone zone = new StorageZone("Test-Zone", 20.0, 10, new Position(100, 100));
        
        for (int i = 1; i <= 5; i++) {
            Item item = new Item("ITEM-" + i, "Package-" + i, 5.0, new Date(), 20.0);
            zone.addItem(item); // Exception will be automatically handled by JUnit
        }
        
        assertEquals(5, zone.getItems().size());
    }

    @Test
    public void testAddFrozenItemToColdZone() throws Exception {
        StorageZone coldZone = new StorageZone("Cold-Zone", -20.0, 10, new Position(100, 100));
        Item frozenItem = new Item("FROZEN-001", "Ice Cream", 2.0, new Date(), -20.0);
        coldZone.addItem(frozenItem); // Exception will be automatically handled by JUnit
        
        assertEquals(1, coldZone.getItems().size());
    }

    // ========== REMOVING ITEMS TESTS ==========

    @Test
    public void testRemoveExistingItem() throws Exception {
        StorageZone zone = new StorageZone("Test-Zone", 20.0, 10, new Position(100, 100));
        Item item = new Item("ITEM-001", "Package-1", 5.0, new Date(), 20.0);
        zone.addItem(item); // Exception will be automatically handled by JUnit
        
        assertEquals(1, zone.getItems().size());
        
        zone.removeItem(item); // Exception will be automatically handled by JUnit
        assertEquals(0, zone.getItems().size());
        assertFalse(zone.getItems().contains(item));
    }

    @Test(expected = Exception.class)
    public void testRemoveNonExistentItem() throws Exception {
        StorageZone zone = new StorageZone("Test-Zone", 20.0, 10, new Position(100, 100));
        Item item = new Item("ITEM-001", "Package-1", 5.0, new Date(), 20.0);
        zone.removeItem(item); // This should throw the expected exception
    }

    @Test
    public void testRemoveItemFromMiddle() throws Exception {
        StorageZone zone = new StorageZone("Test-Zone", 20.0, 10, new Position(100, 100));
        Item item1 = new Item("ITEM-001", "Package-1", 5.0, new Date(), 20.0);
        Item item2 = new Item("ITEM-002", "Package-2", 5.0, new Date(), 20.0);
        Item item3 = new Item("ITEM-003", "Package-3", 5.0, new Date(), 20.0);
        
        zone.addItem(item1);
        zone.addItem(item2);
        zone.addItem(item3);
        zone.removeItem(item2);
        
        assertEquals(2, zone.getItems().size());
        assertTrue(zone.getItems().contains(item1));
        assertTrue(zone.getItems().contains(item3));
        assertFalse(zone.getItems().contains(item2));
    }

    // ========== CAPACITY MANAGEMENT TESTS ==========

    @Test
    public void testFillToCapacity() throws Exception {
        StorageZone zone = new StorageZone("Test-Zone", 20.0, 5, new Position(100, 100));
        
        for (int i = 1; i <= 5; i++) {
            Item item = new Item("ITEM-" + i, "Package-" + i, 5.0, new Date(), 20.0);
            zone.addItem(item); // Exception will be automatically handled by JUnit
        }
        
        assertEquals(5, zone.getItems().size());
    }

    @Test(expected = Exception.class)
    public void testExceedCapacity() throws Exception {
        StorageZone zone = new StorageZone("Test-Zone", 20.0, 3, new Position(100, 100));
        
        // Adding 3 items, which is the maximum capacity.
        for (int i = 1; i <= 3; i++) {
            Item item = new Item("ITEM-" + i, "Package-" + i, 5.0, new Date(), 20.0);
            zone.addItem(item); // Exception will be automatically handled by JUnit
        }
        
        // Adding an extra item, should throw exception as it exceeds the capacity.
        Item extraItem = new Item("ITEM-EXTRA", "Extra Package", 5.0, new Date(), 20.0);
        zone.addItem(extraItem); // This should throw the expected exception
    }

    @Test
    public void testAddAfterRemoval() throws Exception {
        StorageZone zone = new StorageZone("Test-Zone", 20.0, 2, new Position(100, 100));
        Item item1 = new Item("ITEM-001", "Package-1", 5.0, new Date(), 20.0);
        Item item2 = new Item("ITEM-002", "Package-2", 5.0, new Date(), 20.0);
        
        zone.addItem(item1);
        zone.addItem(item2);
        assertEquals(2, zone.getItems().size());
        
        zone.removeItem(item1);
        assertEquals(1, zone.getItems().size());
        
        Item item3 = new Item("ITEM-003", "Package-3", 5.0, new Date(), 20.0);
        zone.addItem(item3);
        assertEquals(2, zone.getItems().size());
    }

    // ========== TEMPERATURE ZONE TESTS ==========

    @Test
    public void testMultipleColdZones() {
        StorageZone coldA = new StorageZone("Cold-Zone-A", -20.0, 10, new Position(250, 150));
        StorageZone coldB = new StorageZone("Cold-Zone-B", -20.0, 10, new Position(850, 150));
        
        assertEquals(-20.0, coldA.getTemperature(), 0.001);
        assertEquals(-20.0, coldB.getTemperature(), 0.001);
    }

    @Test
    public void testMultipleRoomZones() {
        StorageZone roomA = new StorageZone("Room-Zone-A", 20.0, 10, new Position(250, 350));
        StorageZone roomB = new StorageZone("Room-Zone-B", 20.0, 10, new Position(850, 350));
        
        assertEquals(20.0, roomA.getTemperature(), 0.001);
        assertEquals(20.0, roomB.getTemperature(), 0.001);
    }

    @Test
    public void testTemperatureModification() {
        StorageZone zone = new StorageZone("Test-Zone", 20.0, 10, new Position(100, 100));
        assertEquals(20.0, zone.getTemperature(), 0.001);
        
        zone.setTemperature(25.0);
        assertEquals(25.0, zone.getTemperature(), 0.001);
    }

    @Test
    public void testUnsortedAreaCreation() {
        StorageZone unsortedArea = new StorageZone("Unsorted-Area", 25.0, 50, new Position(550, 300));
        
        assertEquals("Unsorted-Area", unsortedArea.getZoneName());
        assertEquals(25.0, unsortedArea.getTemperature(), 0.001);
        assertEquals(50, unsortedArea.getCapacity());
    }
}
