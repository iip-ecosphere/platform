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

package de.iip_ecosphere.platform.ecsRuntime;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Basic container operations that a container manager as well as an AAS client shall provide.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ContainerOperations {

    /**
     * Adds a container to the management domain of this instance, e.g., by downloading it from a container registry.
     * This defines the {@code id} of the container within the management domain of this instance. After a successful
     * execution, the container {@code id} is returned and shall be available and the container shall be in state 
     * {@link ContainerState#AVAILABLE}.
     * 
     * @param location the location from where to download the container, e.g., an URL
     * @return the id of the container
     * @throws ExecutionException in case that adding the container fails for some reason
     */
    public String addContainer(URI location) throws ExecutionException;

    /**
     * Starts a container. The container must be already within the management domain of this instance by
     * {@link #addContainer(URI)}. The container shall after some time be in state 
     * {@link ContainerState#STARTING}, {@link ContainerState#RUNNING} or {@link ContainerState#FAILED}.
     * 
     * @param id the id of the container
     * @throws ExecutionException in case that starting the container fails for some reason
     */
    public void startContainer(String id) throws ExecutionException;
    
    /**
     * Stops the container. The container shall afterwards be in state {@link ContainerState#STOPPED}.
     * 
     * @param id the id of the container to stop, or as fallback the canonical URI of the container descriptor used 
     *     for adding
     * @throws ExecutionException if stopping the container fails
     */
    public void stopContainer(String id) throws ExecutionException;
    
    /**
     * Migrates the container.
     * 
     * @param id the id of the container
     * @param resourceId the target resource id, e.g., a device
     * @throws ExecutionException if migration fails
     */
    public void migrateContainer(String id, String resourceId) throws ExecutionException;
    
    /**
     * Removes the container from the management domain of this instance. This operation shall only remove the 
     * implementation of a non-operational container and, thus, perform a state transition to 
     * {@link ContainerState#UNDEPLOYING} and ultimately the container shall be removed and its descriptor shall not
     * be available anymore.
     * 
     * @param id the id of the container to remove, or as fallback the canonical URI of the container descriptor used 
     *     for adding
     * @throws ExecutionException if undeploying the container fails, e.g., because it is still running
     */
    public void undeployContainer(String id) throws ExecutionException;
    
    /**
     * Updates the container by the container in the given {@code location}. This operation is responsible for stopping
     * the running container (if needed), replacing it, starting the new container.
     * 
     * @param id the id of the container to be updated
     * @param location the location of the new container, e.g., an URL
     * @throws ExecutionException if the given container cannot be updated for some reason
     */
    public void updateContainer(String id, URI location) throws ExecutionException;
    
    /**
     * Returns the state of the container.
     * 
     * @param id the id of the container
     * @return the state of the container
     */
    public ContainerState getState(String id);

    /**
     * Returns the name of the underlying container system, e.g. Docker.
     * 
     * @return the name
     */
    public String getContainerSystemName();

    /**
     * Returns the version of the underlying container system. As this version may differ from the syntax conventions
     * of {@link Version}, we just return here a string.
     * 
     * @return the version
     */
    public String getContainerSystemVersion();

}
