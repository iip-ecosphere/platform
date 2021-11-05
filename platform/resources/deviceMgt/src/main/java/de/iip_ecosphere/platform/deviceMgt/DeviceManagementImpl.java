package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactory;

import java.util.concurrent.ExecutionException;

/**
 * Default DeviceManagemt implementation, which gets build by the DeviceManagementFactory
 * with the help of the loaded service providers.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceManagementImpl implements DeviceManagement {

    private DeviceFirmwareOperations firmwareOperations;
    private DeviceRemoteManagementOperations remoteManagementOperations;
    private DeviceResourceConfigOperations resourceConfigOperations;

    private final DeviceRegistry deviceRegistry;

    /**
     * Constructor for DeviceManagementImpl. Asks {@link DeviceRegistryFactory} to
     * get the latest DeviceRegistry.
     *
     * @param firmwareOperations the DeviceFirmwareOperations to use
     * @param remoteManagementOperations the DeviceRemoteManagementOperations to use
     * @param resourceConfigOperations the DeviceResourceConfigOperations to use
     */
    public DeviceManagementImpl(DeviceFirmwareOperations firmwareOperations,
                                DeviceRemoteManagementOperations remoteManagementOperations,
                                DeviceResourceConfigOperations resourceConfigOperations) {
        this.firmwareOperations = firmwareOperations;
        this.remoteManagementOperations = remoteManagementOperations;
        this.resourceConfigOperations = resourceConfigOperations;
        this.deviceRegistry = DeviceRegistryFactory.getDeviceRegistry();
    }

    /**
     * Sends request to given firmwareOperations if it the device is available.
     *
     * @param id the id of the device
     * @throws ExecutionException if the operation fails
     */
    @Override
    public void updateRuntime(String id) throws ExecutionException {
        if (deviceRegistry.getDevice(id) == null) {
            return;
        }
        this.firmwareOperations.updateRuntime(id);
    }

    /**
     * Sends request to given remoteManagementOperations.
     *
     * @param id the id of the device
     * @return SSHConnectionDetails if the device is available, otherwise null
     * @throws ExecutionException if the operation fails
     */
    @Override
    public SSHConnectionDetails establishSsh(String id) throws ExecutionException {
        if (deviceRegistry.getDevice(id) == null) {
            return null;
        }

        return remoteManagementOperations.establishSsh(id);
    }

    /**
     * Sends request to given remoteManagementOperations if the device is available.
     *
     * @param id the id of the device
     * @param configPath the configPath stored in the storage
     * @throws ExecutionException if the operation fails
     */
    @Override
    public void setConfig(String id, String configPath) throws ExecutionException {
        if (deviceRegistry.getDevice(id) == null) {
            return;
        }
        resourceConfigOperations.setConfig(id, configPath);
    }
}
