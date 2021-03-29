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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A basic re-usable implementation of the service manager. Implementations shall override at least 
 * {@link #removeService(String)}, {@link #switchToService(String, String)}, {@link #migrateService(String, String)}
 * and call the implementation of this class to perform the changes. Implementations shall call the notify methods 
 * in {@link ServicesAas}.
 *
 * @param <A> the actual type of the artifact descriptor
 * @param <S> the actual type of the service descriptor
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractServiceManager<A extends AbstractArtifactDescriptor<S>, 
    S extends AbstractServiceDescriptor> implements ServiceManager {

    private Map<String, A> artifacts = Collections.synchronizedMap(new HashMap<>());

    @Override
    public Set<String> getArtifactIds() {
        return artifacts.keySet();
    }
    
    @Override
    public Collection<A> getArtifacts() {
        return artifacts.values();
    }
    
    @Override
    public Set<String> getServiceIds() {
        Set<String> result = new HashSet<>();
        for (A a : getArtifacts()) {
            result.addAll(a.getServiceIds());
        }
        return result;
    }

    @Override
    public Collection<S> getServices() {
        Set<S> result = new HashSet<>();
        for (A a : getArtifacts()) {
            result.addAll(a.getServices());
        }
        return result;
    }

    @Override
    public A getArtifact(String artifactId) {
        return null == artifactId ? null : artifacts.get(artifactId);
    }
    
    @Override
    public S getService(String serviceId) {
        S result = null;
        for (A a : getArtifacts()) {
            result = a.getService(serviceId);
            if (null != result) {
                break;
            }
        }
        return result;
    }
    
    @Override
    public ServiceState getServiceState(String serviceId) {
        ServiceState result = ServiceState.UNKOWN;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getState();
        }
        return result;
    }
    
    /**
     * Adds an artifact.
     * 
     * @param artifactId the artifact id
     * @param descriptor the service descriptor
     * @return {@code artifactId}
     * @throws ExecutionException in case that the id is invalid or already known
     */
    protected String addArtifact(String artifactId, A descriptor) throws ExecutionException {
        checkId(artifactId, "artifactId");
        if (artifacts.containsKey(artifactId)) {
            throw new ExecutionException("Artifact id '" + artifactId + "' is already known", null);
        }
        artifacts.put(artifactId, descriptor);
        ServicesAas.notifyArtifactAdded(descriptor);
        return artifactId;
    }

    @Override
    public void removeArtifact(String artifactId) throws ExecutionException {
        checkId(artifactId, "artifactId");
        if (!artifacts.containsKey(artifactId)) {
            throw new ExecutionException("Artifact id '" + artifactId 
                + "' is not known. Cannot remove artifact.", null);
        }
        A aDesc = artifacts.remove(artifactId);
        ServicesAas.notifyArtifactRemoved(aDesc);
    }
    
    /**
     * Checks the given {@code id} for basic validity.
     * 
     * @param id the id to check
     * @param text the text to include into the exception
     * @throws ExecutionException if {@code id} is not considered valid
     */
    protected static void checkId(String id, String text) throws ExecutionException {
        if (null == id || id.length() == 0) {
            throw new ExecutionException(text + "must be given (not null or empty)", null);
        }
    }
    
    @Override
    public void switchToService(String serviceId, String targetId) throws ExecutionException {
        checkId(serviceId, "id");
        checkId(serviceId, "targetId");
        if (!serviceId.equals(targetId)) {
            stopService(serviceId);
            startService(targetId);
        }
    }

    @Override
    public void migrateService(String serviceId, URI location) throws ExecutionException {
        checkId(serviceId, "serviceId");
        S cnt = getServiceDescriptor(serviceId, "serviceId", "migrate");
        if (ServiceState.RUNNING == cnt.getState()) {
            stopService(serviceId);
        } else {
            throw new ExecutionException("Service " + serviceId + " is in state " + cnt.getState() 
                + ". Cannot migrate service.", null);
        }
    }

    /**
     * Returns a service descriptor.
     * 
     * @param artifactId the artifact id
     * @param idText the id text to be passed to {@link #checkId(String, String)}
     * @param activityText a description of the activity the service is requested for to construct an exception if 
     *   the service does not exist
     * @return the service (not <b>null</b>)
     * @throws ExecutionException if id is invalid or the service is unknown
     */
    protected A getArtifactDescriptor(String artifactId, String idText, String activityText) throws ExecutionException {
        checkId(artifactId, idText);
        A result = artifacts.get(artifactId);
        if (null == result) {
            throw new ExecutionException("Artifact id '" + artifactId + "' is not known. Cannot " + activityText 
                + " service.", null);
        }
        return result;
    }

    /**
     * Returns a service descriptor.
     * 
     * @param serviceId the service id
     * @param idText the id text to be passed to {@link #checkId(String, String)}
     * @param activityText a description of the activity the service is requested for to construct an exception if 
     *   the service does not exist
     * @return the service (not <b>null</b>)
     * @throws ExecutionException if id is invalid or the service is unknown
     */
    protected S getServiceDescriptor(String serviceId, String idText, String activityText) throws ExecutionException {
        checkId(serviceId, idText);
        S result = getService(serviceId);
        if (null == result) {
            throw new ExecutionException("Service id '" + serviceId + "' is not known. Cannot " + activityText 
                + " service.", null);
        }
        return result;
    }

    @Override
    public void setServiceState(String serviceId, ServiceState state) throws ExecutionException {
        setState(getServiceDescriptor(serviceId, "serviceId", "setState"), state);
    }
    
    /**
     * Changes the service state and notifies {@link ServicesAas}.
     * 
     * @param service the service
     * @param state the new state
     * @throws ExecutionException if changing the state fails
     */
    protected void setState(ServiceDescriptor service, ServiceState state) throws ExecutionException {
        service.setState(state);
        ServicesAas.notifyServiceStateChanged(service);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<TypedDataDescriptor> getParameters(String serviceId) {
        List<TypedDataDescriptor> result = null;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getParameters();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TypedDataConnectorDescriptor> getInputDataConnectors(String serviceId) {
        List<TypedDataConnectorDescriptor> result = null;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getInputDataConnectors();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TypedDataConnectorDescriptor> getOutputDataConnectors(String serviceId) {
        List<TypedDataConnectorDescriptor> result = null;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getOutputDataConnectors();
        }
        return result;
    }

}
