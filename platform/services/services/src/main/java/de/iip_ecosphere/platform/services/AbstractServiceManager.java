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

package de.iip_ecosphere.platform.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A basic re-usable implementation of the service manager. Implementations shall override at least 
 * {@link #removeService(String)}, {@link #switchToService(String, String)}, {@link #migrateService(String, String)}
 * and call the implementation of this class to perform the changes.
 *
 * @param <D> the actual type of service descriptor
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractServiceManager<D extends ServiceDescriptor> implements ServiceManager {

    private Map<String, D> services = Collections.synchronizedMap(new HashMap<>());

    @Override
    public Set<String> getIds() {
        return services.keySet();
    }

    @Override
    public Collection<D> getServices() {
        return services.values();
    }

    @Override
    public D getService(String id) {
        return services.get(id);
    }
    
    @Override
    public ServiceState getState(String id) {
        ServiceState result = ServiceState.UNKOWN;
        if (null != id) {
            ServiceDescriptor d = services.get(id);
            if (null != d) {
                result = d.getState();
            }
        }
        return result;
    }
    
    /**
     * Adds a service.
     * 
     * @param id the service id
     * @param descriptor the service descriptor
     * @throws ExecutionException in case that the id is invalid or already known
     */
    protected void addService(String id, D descriptor) throws ExecutionException {
        checkId(id, "id");
        if (services.containsKey(id)) {
            throw new ExecutionException("Service id '" + id + "' is already known", null);
        }
        services.put(id, descriptor);
    }

    @Override
    public void removeService(String id) throws ExecutionException {
        checkId(id, "id");
        if (!services.containsKey(id)) {
            throw new ExecutionException("Service id '" + id + "' is not known. Cannot remove service.", null);
        }
        services.remove(id);
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
    public void switchToService(String id, String targetId) throws ExecutionException {
        checkId(id, "id");
        checkId(id, "targetId");
        if (!id.equals(targetId)) {
            if (services.containsKey(targetId)) {
                throw new ExecutionException("Target service id '" + id + "' is already known. Cannot switch service.", 
                    null);
            }
            if (!services.containsKey(id)) {
                throw new ExecutionException("Service id '" + id + "' is not known. Cannot switch service.", null);
            }
            services.put(targetId, services.remove(id));
        }
    }

    @Override
    public void migrateService(String id, String location) throws ExecutionException {
        removeService(id);
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
    protected ServiceDescriptor getServiceDescriptor(String id, String idText, String activityText) 
        throws ExecutionException {
        checkId(id, idText);
        ServiceDescriptor result = services.get(id);
        if (null == result) {
            throw new ExecutionException("Service id '" + id + "' is not known. Cannot " + activityText 
                + " service.", null);
        }
        return result;
    }

    @Override
    public void activate(String id) throws ExecutionException {
        getServiceDescriptor(id, "id", "activate").passivate();
    }

    @Override
    public void passivate(String id) throws ExecutionException {
        getServiceDescriptor(id, "id", "passivate").passivate();
    }

    @Override
    public void setState(String id, ServiceState state) throws ExecutionException {
        getServiceDescriptor(id, "id", "setState").setState(state);
    }
    
    @Override
    public void reconfigure(String id, Map<String, Object> values) throws ExecutionException {
        getServiceDescriptor(id, "id", "setState").reconfigure(values);
    }
    
}
