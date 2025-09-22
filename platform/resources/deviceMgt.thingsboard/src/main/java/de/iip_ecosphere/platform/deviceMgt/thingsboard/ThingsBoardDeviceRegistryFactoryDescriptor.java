package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactoryDescriptor;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

import org.thingsboard.rest.client.RestClient;

public class ThingsBoardDeviceRegistryFactoryDescriptor extends SingletonPluginDescriptor<DeviceRegistry>
    implements DeviceRegistryFactoryDescriptor {

    public static final String BASE_URL = "http://localhost:8080";
    public static final String USERNAME = "tenant@thingsboard.org";
    public static final String PASSWORD = "tenant";

    /**
     * Creates the instance via JSL.
     */
    public ThingsBoardDeviceRegistryFactoryDescriptor() {
        super(PLUGIN_ID, null, DeviceRegistry.class, null);
    }
    
    @Override
    protected PluginSupplier<DeviceRegistry> initPluginSupplier(PluginSupplier<DeviceRegistry> pluginSupplier) {
        return p -> createDeviceRegistryInstance();
    }    
    
    @Override
    public DeviceRegistry createDeviceRegistryInstance() {
        RestClient restClient = new RestClient(BASE_URL);
        restClient.login(USERNAME, PASSWORD);
        return new ThingsBoardDeviceRegistry(restClient);
    }

}
