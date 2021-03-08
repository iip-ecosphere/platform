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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A basic re-usable implementation of the containermanager. Implementations shall override at least 
 * {@link #undeployContainer(String)}, {@link #migrateContainer(String, String)}
 * and call the implementation of this class to perform the changes.
 *
 * @param <D> the actual type of container descriptor
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractContainerManager<D extends ContainerDescriptor> implements ContainerManager {

    private Map<String, D> containers = Collections.synchronizedMap(new HashMap<>());

    @Override
    public Set<String> getIds() {
        return containers.keySet();
    }

    @Override
    public Collection<D> getContainers() {
        return containers.values();
    }

    @Override
    public D getContainer(String id) {
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
     * @throws ExecutionException in case that the id is invalid or already known
     */
    protected void addContainer(String id, D descriptor) throws ExecutionException {
        checkId(id, "id");
        if (containers.containsKey(id)) {
            throw new ExecutionException("Container id '" + id + "' is already known", null);
        }
        containers.put(id, descriptor);
    }

    @Override
    public void undeployContainer(String id) throws ExecutionException {
        checkId(id, "id");
        if (!containers.containsKey(id)) {
            throw new ExecutionException("Container id '" + id + "' is not known. Cannot undeploy container.", null);
        }
        containers.remove(id);
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
            throw new ExecutionException("Service " + text + "must be given (not null or empty)", null);
        }
    }
    
    @Override
    public void migrateContainer(String id, String location) throws ExecutionException {
        undeployContainer(id);
    }

}
