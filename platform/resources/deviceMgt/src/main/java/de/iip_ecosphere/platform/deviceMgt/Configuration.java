package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.storage.PackageStorageSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasConfiguration;

/**
 * Basic Configuration.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class Configuration extends AasConfiguration {

    private PackageStorageSetup configStorage;
    private PackageStorageSetup runtimeStorage;

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
    
}
