/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
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
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A service provider interface for managing containers in the IIP-Ecosphere platform. The interface is rather simple 
 * as it shall be usable through an AAS. The id of a container used here must not be identical to some 
 * container-specific ids or the name in  {@link ContainerDescriptor#getName()}, e.g., it may contain the version.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ContainerManager {

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
     * {@link #addContainer(String, String)}. The container shall after some time be in state 
     * {@link ContainerState#STARTING}, {@link ContainerState#RUNNING} or {@link ContainerState#FAILED}.
     * 
     * @param id the id of the container
     * @throws ExecutionException in case that starting the container fails for some reason
     */
    public void startContainer(String id) throws ExecutionException;
    
    /**
     * Stops the container. The container shall afterwards be in state {@link ContainerState#STOPPED}.
     * 
     * @param id the id of the container to stop
     * @throws ExecutionException if stopping the container fails
     */
    public void stopContainer(String id) throws ExecutionException;
    
    /**
     * Migrates the container.
     * 
     * @param id the id of the container
     * @param location the target location, e.g., a device
     * @throws ExecutionException if migration fails
     */
    public void migrateContainer(String id, URI location) throws ExecutionException;
    
    /**
     * Removes the container from the management domain of this instance. This operation shall only remove the 
     * implementation of a non-operational container and, thus, perform a state transition to 
     * {@link ContainerState#UNDEPLOYING} and ultimately the container shall be removed and its descriptor shall not
     * be available anymore.
     * 
     * @param id the id of the container to remove
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
     * Returns the ids of all available containers.
     * 
     * @return the ids
     */
    public Set<String> getIds();
    
    /**
     * Returns the available (installed) containers independent of their state.
     * 
     * @return the container descriptors
     */
    public Collection<? extends ContainerDescriptor> getContainers();
    
    /**
     * Returns a container descriptor.
     * 
     * @param id the id of the container (may be <b>null</b> or invalid)
     * @return the related container descriptor or <b>null</b> if the container is not known at all
     */
    public ContainerDescriptor getContainer(String id); 
    
}
