/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.services.TransportConverterFactory;

/**
 * Basic service operations that shall also be available via an AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServiceOperations {
    
    public static final String EXC_ALREADY_KNOWN = "is already known";

    /**
     * Service start option, ensemble mapping (member-leader mapping), given as JSON map (string).
     */
    public static final String OPTION_ENSEMBLE = "ensemble";

    /**
     * Service start option, command line options, usually -D&lt;key&gt;=&lt;value&gt;, given as JSON list (string).
     */
    public static final String OPTION_ARGS = "args";

    /**
     * Service start option, mapping of service ids to server IP addresses JSON map (string).
     */
    public static final String OPTION_SERVERS = "servers";
    
    /**
     * Service start option, mapping of service ids to maps of name-value for re-configuring the service parameters 
     * directly after start.
     */
    public static final String OPTION_PARAMS = "params";

    /**
     * Service start option, mapping of service ids to memory limits in MBytes.
     */
    public static final String OPTION_MEMLIMITS = "memLimits";

    /**
     * Adds an artifact (and transitively the contained services) to the management domain of this instance, e.g., 
     * by downloading it from an artifact/service store. This defines the {@code id} of the service within the 
     * management domain of this instance. After a successful execution, the artifact {@code id} is returned, artifact 
     * and service(s) shall be available and the service(s) shall be in state {@link ServiceState#AVAILABLE}.
     * 
     * @return the id of the artifact
     * @param location the location from where to download the service, e.g., an URL
     * @throws ExecutionException in case that adding the service fails for some reason
     */
    public String addArtifact(URI location) throws ExecutionException;

    /**
     * Starts one or multiple service. The service implementation must be already within the management domain of this 
     * instance by {@link #addArtifact(URI)}. The service(s) shall after some time be in state 
     * {@link ServiceState#STARTING}, {@link ServiceState#RUNNING} or {@link ServiceState#FAILED}.
     * 
     * @param serviceId the id(s) of the service(s)
     * @throws ExecutionException in case that starting the service fails for some reason
     */
    public void startService(String... serviceId) throws ExecutionException;

    /**
     * Starts one or multiple service. The service implementation must be already within the management domain of this 
     * instance by {@link #addArtifact(URI)}. The service(s) shall after some time be in state 
     * {@link ServiceState#STARTING}, {@link ServiceState#RUNNING} or {@link ServiceState#FAILED}.
     * 
     * @param options optional map of optional options to be passed to the service manager, may modify the 
     *   service descriptors, e.g., to change services to be started together on demand, may be empty for none; options 
     *   are a name-value mapping with values that come either as primitive values or as JSON structures. For 
     *   pre-defined names, see {@link #OPTION_ARGS}, {@link #OPTION_ENSEMBLE}, {@link #OPTION_PARAMS}, 
     *   {@link #OPTION_SERVERS}, or {@link #OPTION_MEMLIMITS}. The service manager is responsible 
     *   for correct JSON de-serialization.
     * @param serviceId the id(s) of the service(s)
     * @throws ExecutionException in case that starting the service fails for some reason
     * @see #startService(String...)
     */
    public void startService(Map<String, String> options, String... serviceId) throws ExecutionException;

    /**
     * Stops one or multiple services. The service shall afterwards be in state {@link ServiceState#STOPPED}.
     * 
     * @param serviceId the id(s) of the service(s) to stop
     * @throws ExecutionException if stopping the service fails
     */
    public void stopService(String... serviceId) throws ExecutionException;
    
    /**
     * Migrates a service. However, it may be required to migrate/move the containing artifact. [adaptation]
     * 
     * @param serviceId the id of the service
     * @param resourceId the target resource id, e.g., a device
     * @throws ExecutionException if migration fails
     */
    public void migrateService(String serviceId, String resourceId) throws ExecutionException;

    /**
     * Removes the artifact (and transitively its services) from the management domain of this instance. This operation 
     * shall only remove the implementation of non-operational services and, thus, perform a state transition to 
     * {@link ServiceState#UNDEPLOYING} and ultimately the service(s) shall be removed and their descriptors (including 
     * the artifact descriptor) shall not be available anymore.
     * 
     * @param artifactId the id of the artifact to remove, or as fallback the canonical URI of the artifact
     * @throws ExecutionException if removing the service fails, e.g., because it is still running
     */
    public void removeArtifact(String artifactId) throws ExecutionException;
    
    /**
     * Updates the service by the service in the given {@code location}. This operation is responsible for stopping
     * the running service (if needed), replacing it, starting the new service.
     * 
     * @param serviceId the id of the service to be updated
     * @param location the location of the new service, e.g., an URL
     * @throws ExecutionException if the given service cannot be updated for some reason, e.g., because the replacement
     *   service is not an evolved version of the running service
     */
    public void updateService(String serviceId, URI location) throws ExecutionException;
    
    /**
     * Switches to an interface-compatible service. This method cares for stopping the old service, performing
     * a handover if adequate, starting the {@code target} service. [adaptation]
     * 
     * @param serviceId the id of the running service
     * @param targetId the id of the target service
     * @throws ExecutionException if switching the service cannot be performed for some reason
     */
    public void switchToService(String serviceId, String targetId) throws ExecutionException;
    
    /**
     * Activates the service. [adaptation]
     * 
     * @param serviceId the id of the running service
     * @throws ExecutionException in case that activating fails, e.g., because the service is already active 
     */
    public void activateService(String serviceId) throws ExecutionException;

    /**
     * Passivates the service. [adaptation]
     * 
     * @param serviceId the id of the running service
     * @throws ExecutionException in case that passivating fails, e.g., because the service is already passive 
     */
    public void passivateService(String serviceId) throws ExecutionException;
    
    /**
     * Reconfigures the underlying service. [adaptation]
     * 
     * @param serviceId the serviceId of the running service
     * @param values the (service-specific) name-value mapping that shall lead to a reconfiguration of the service; 
     *   values come either as primitive values or as JSON structures complying with the parameter descriptor. The 
     *   service is responsible for correct JSON de-serialization according to the respective 
     *   {@link TypedDataDescriptor descriptor}
     * @throws ExecutionException if reconfiguration fails
     */
    public void reconfigureService(String serviceId, Map<String, String> values) throws ExecutionException;

    /**
     * Sets the state of the service. [adaptation]
     * 
     * @param serviceId the id of the running service
     * @param state the new state of the service
     * @throws ExecutionException if changing to the target state is not possible 
     */
    public void setServiceState(String serviceId, ServiceState state) throws ExecutionException;

    /**
     * Returns the state of the service.
     * 
     * @param serviceId the id of the running service
     * @return the state of the service
     */
    public ServiceState getServiceState(String serviceId);
    
    /**
     * Returns the number of service instances of the same service in the same application.
     * 
     * @param serviceId the service id, application instance id is ignored if not given 
     * @return 0 if there is no other service instance, 1 if no applicationId is given in {@code serviceId} but there
     *    is a service with the same id, the number of instances otherwise
     */
    public int getServiceInstanceCount(String serviceId);
    
    /**
     * Mode for {@link ServiceOperations#streamLog(String, boolean)}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum StreamLogMode {
        START,
        TAIL,
        STOP,
        NONE
    }

    /**
     * Turns a string value to a {@link StreamLogMode}.
     * 
     * @param value the string value
     * @return the mode, fallback is {@link StreamLogMode#NONE}.
     */
    public static StreamLogMode toMode(String value) {
        StreamLogMode result = StreamLogMode.NONE;
        if (null != value) {
            try {
                result = StreamLogMode.valueOf(value);
            } catch (IllegalArgumentException e) {
                // -> default
            }
        }
        return result;
    }
    
    /**
     * Tries to attach the logs of {@code serviceId} to a sender determined by {@link TransportConverterFactory}.
     * 
     * @param serviceId the service id
     * @param mode the mode
     * @return the URIs where to find the (streamed) log as JSON list or empty
     * @throws ExecutionException if attaching fails
     */
    public default String streamLog(String serviceId, StreamLogMode mode) throws ExecutionException {
        return "[]";
    }

}
