package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.storage.PackageStorageSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageServerSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasBasedSetup;

/**
 * Device management setup.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceMgtSetup extends AasBasedSetup {

    private PackageStorageSetup configStorage;
    private PackageStorageSetup runtimeStorage;
    private StorageServerSetup storageServer;

    /**
     * Get the StorageSetup.
     *
     * @return the storageSetup
     */
    public PackageStorageSetup getRuntimeStorage() {
        return runtimeStorage;
    }

    /**
     * Set the Runtime-StorageSetup.
     *
     * @param runtimeStorage the StorageSetup of runtimes
     */
    public void setRuntimeStorage(PackageStorageSetup runtimeStorage) {
        this.runtimeStorage = runtimeStorage;
    }
    
    /**
     * Get the StorageSetup.
     *
     * @return the storageSetup
     */
    public PackageStorageSetup getConfigStorage() {
        return configStorage;
    }

    /**
     * Set the Configs-StorageSetup.
     *
     * @param configStorage the StorageSetup of configs
     */
    public void setConfigStorage(PackageStorageSetup configStorage) {
        this.configStorage = configStorage;
    }

    
    /**
     * Get the storage server setup.
     *
     * @return the storage server setup
     */
    public StorageServerSetup getStorageServer() {
        return storageServer;
    }

    /**
     * Set the storage server setup.
     *
     * @param storageServer the storage server setup
     */
    public void setStorageServer(StorageServerSetup storageServer) {
        this.storageServer = storageServer;
    }

}
