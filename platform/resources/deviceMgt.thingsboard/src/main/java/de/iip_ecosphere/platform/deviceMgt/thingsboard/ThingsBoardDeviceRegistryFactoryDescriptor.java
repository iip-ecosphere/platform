package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactoryDescriptor;
import org.thingsboard.rest.client.RestClient;

public class ThingsBoardDeviceRegistryFactoryDescriptor implements DeviceRegistryFactoryDescriptor {

    public static final String BASE_URL = "http://localhost:8080";
    public static final String USERNAME = "tenant@thingsboard.org";
    public static final String PASSWORD = "tenant";

    @Override
    public DeviceRegistry createDeviceRegistryInstance() {
        RestClient restClient = new RestClient(BASE_URL);
        restClient.login(USERNAME, PASSWORD);
        return new ThingsBoardDeviceRegistry(restClient);
    }

}
