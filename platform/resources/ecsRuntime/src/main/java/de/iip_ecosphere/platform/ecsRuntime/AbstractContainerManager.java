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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A basic re-usable implementation of the container manager. Implementations shall override at least 
 * {@link #undeployContainer(String)}, {@link #migrateContainer(String, String)}
 * and call the implementation of this class to perform the changes.
 *
 * @param <C> the actual type of container descriptor
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractContainerManager<C extends ContainerDescriptor> implements ContainerManager {

    private Map<String, C> containers = Collections.synchronizedMap(new HashMap<>());

    @Override
    public Set<String> getIds() {
        return containers.keySet();
    }

    @Override
    public Collection<C> getContainers() {
        return containers.values();
    }

    @Override
    public C getContainer(String id) {
        return containers.get(id);
    }
    
    @Override
    public ContainerState getState(String id) {
        ContainerState result = ContainerState.UNKOWN;
        if (null != id) {
            ContainerDescriptor d = containers.get(id);
            if (null != d) {
                result = d.getState();
            }
        }
        return result;
    }
    
    /**
     * Adds a container.
     * 
     * @param id the container id
     * @param descriptor the container descriptor
     * @return {@code id}
     * @throws ExecutionException in case that the id is invalid or already known
     */
    protected String addContainer(String id, C descriptor) throws ExecutionException {
        checkId(id, "id");
        if (containers.containsKey(id)) {
            throw new ExecutionException("Container id '" + id + "' is already known", null);
        }
        containers.put(id, descriptor);
        return id;
    }

    @Override
    public void undeployContainer(String id) throws ExecutionException {
        checkId(id, "id");
        if (!containers.containsKey(id)) {
            throw new ExecutionException("Container id '" + id + "' is not known. Cannot undeploy container.", null);
        }
        ContainerDescriptor desc = containers.get(id);
        if (ContainerState.AVAILABLE == desc.getState() || ContainerState.STOPPED == desc.getState()) {
            containers.remove(id);
        } else {
            throw new ExecutionException("Container is in state " + desc.getState() 
                + ". Cannot undeploy container.", null);
        }
    }
    
    /**
     * Returns a service descriptor.
     * 
     * @param id the service id
     * @param idText the id text to be passed to {@link #checkId(String, String)}
     * @param activityText a description of the activity the service is requested for to construct an exception if 
     *   the service does not exist
     * @return the service (not <b>null</b>)
     * @throws ExecutionException if id is invalid or the service is unkown
     */
    protected C getContainer(String id, String idText, String activityText) 
        throws ExecutionException {
        checkId(id, idText);
        C result = containers.get(id);
        if (null == result) {
            throw new ExecutionException("Container id '" + id + "' is not known. Cannot " + activityText 
                + " container.", null);
        }
        return result;
    }
    
    /**
     * Checks the given {@code id} for basic validity.
     * 
     * @param id the id to check
     * @param text the text to include into the exception
     * @throws ExecutionException if {@code id} is not considered valid
     */
    protected void checkId(String id, String text) throws ExecutionException {
        if (null == id || id.length() == 0) {
            throw new ExecutionException("Container " + text + "must be given (not null or empty)", null);
        }
    }
    
    @Override
    public void migrateContainer(String id, URI location) throws ExecutionException {
        checkId(id, "id");
        if (!containers.containsKey(id)) {
            throw new ExecutionException("Container id '" + id + "' is not known. Cannot migrate container.", null);
        }
        ContainerDescriptor desc = containers.get(id);
        if (ContainerState.DEPLOYED == desc.getState()) {
            stopContainer(id);
        } else {
            throw new ExecutionException("Container " + id + " is in state " + desc.getState() 
                + ". Cannot undeploy container.", null);
        }
    }

}
