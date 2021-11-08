package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.deviceMgt.Credentials;

import java.util.concurrent.ExecutionException;

/**
 * Aas operations needed for device management.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface DeviceManagementOperations {

    /**
     * Creates connection details (Credentials), a pair of username
     * and a password for the active remote access server.
     *
     * @return credentials for the remote access server
     * @throws ExecutionException if the execution fails
     */
    Credentials createRemoteConnectionCredentials() throws ExecutionException;

    /**
     * Gets the runtimeName of the device.
     *
     * @return the runtimeName
     * @throws ExecutionException if the execution fails
     */
    String getRuntimeName() throws ExecutionException;

    /**
     * Gets the runtimeVersion of the device.
     *
     * @return the runtimeVersion
     * @throws ExecutionException if the execution fails
     */
    Integer getRuntimeVersion() throws ExecutionException;

}
