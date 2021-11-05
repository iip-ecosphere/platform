package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.rest.client.RestClient;

import java.util.List;

public class PerformanceTests {

    public static final String A_DEVICE = "A_DEVICE";
    public static final String AN_IP = "AN_IP";

    private ThingsBoardDeviceRegistry registry;
    private RestClient restClient;

    /**
     * Configures the test before running.
     */
    @Before
    public void setUp() {
        ThingsBoardDeviceRegistryFactoryDescriptor factory =
                new ThingsBoardDeviceRegistryFactoryDescriptor();
        registry = (ThingsBoardDeviceRegistry) factory.createDeviceRegistryInstance();
        restClient = new RestClient(ThingsBoardDeviceRegistryFactoryDescriptor.BASE_URL);
        restClient.login(ThingsBoardDeviceRegistryFactoryDescriptor.USERNAME,
                ThingsBoardDeviceRegistryFactoryDescriptor.PASSWORD);
    }

    /**
     * Cleans up after testing.
     */
    @After
    public void tearDown() {
        registry.getIds().forEach(d -> {
            registry.removeDevice(d);
        });
    }

    /**
     * Tests the device adding speed.
     */
    @Test
    public void testAddSpeed() {
        long time = add(IntegrationTests.generateRandomIds(1));
        System.out.printf("Single Add finished in: %dms%n", time);
        time = add(IntegrationTests.generateRandomIds(1));
        System.out.printf("Single Add finished in: %dms%n", time);
        time = add(IntegrationTests.generateRandomIds(1));
        System.out.printf("Single Add finished in: %dms%n", time);

        int count = 500;
        time = add(IntegrationTests.generateRandomIds(count));
        System.out.printf("Mass (%dx) Add finished in: %dms", count , time);
    }

    /**
     * Tests the device removal speed.
     */
    @Test
    public void testRemoveSpeed() {
        for (int i = 0; i < 5; i++) {
            List<String> ids = IntegrationTests.generateRandomIds(1);
            add(ids);
            long time = remove(ids);
            System.out.printf("Single Remove finished in: %dms%n", time);
        }

        List<String> ids = IntegrationTests.generateRandomIds(500);
        add(ids);
        long time = remove(ids);
        System.out.printf("Mass (%dx) Remove finished in: %dms", 500 , time);
    }

    /**
     * Removes devices.
     * 
     * @param ids the ids of the devices
     * @return the elapsed wall time [ms]
     */
    private long remove(List<String> ids) {
        long start = System.currentTimeMillis();
        ids.forEach(id -> {
            registry.removeDevice(id);
        });
        long end = System.currentTimeMillis();
        long time = end - start;
        return time;
    }

    /**
     * Adds devices.
     * 
     * @param ids the ids of the devices
     * @return the elapsed wall time [ms]
     */
    private long add(List<String> ids) {
        long start = System.currentTimeMillis();
        ids.forEach(id -> registry.addDevice(id, AN_IP));
        long end = System.currentTimeMillis();
        return end - start;
    }


}
