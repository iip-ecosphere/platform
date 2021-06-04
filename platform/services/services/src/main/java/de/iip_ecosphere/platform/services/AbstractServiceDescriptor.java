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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Abstract {@link ServiceDescriptor} implementation, e.g., including a representation of the {@link ServiceState} 
 * statemachine. We do not protect the setters here explicitly, e.g., through a builder pattern as we assume that 
 * the respective messages will only be called within the package of the implementing manager.
 * 
 * @param <A> the type of artifact descriptor
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractServiceDescriptor<A extends ArtifactDescriptor> implements ServiceDescriptor {
    
    private String id;
    private String name;
    private String description;
    private Version version;
    private A artifact;
    private ServiceState state;
    private ServiceKind kind = ServiceKind.TRANSFORMATION_SERVICE;
    private boolean isDeployable = true;
    private List<TypedDataDescriptor> parameters = new ArrayList<>(); 
    private List<TypedDataConnectorDescriptor> input = new ArrayList<>(); 
    private List<TypedDataConnectorDescriptor> output = new ArrayList<>(); 
    
    /**
     * Creates an instance. Call {@link #setClassification(ServiceKind, boolean)} afterwards.
     * 
     * @param id the service id
     * @param name the name of this service
     * @param description the description of the service
     * @param version the version
     */
    protected AbstractServiceDescriptor(String id, String name, String description, Version version) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.state = ServiceState.AVAILABLE;
    }

    /**
     * Defines an artifact.
     * 
     * @param artifact the containing artifact descriptor
     */
    protected void setArtifact(A artifact) {
        this.artifact = artifact;
    }
    
    /**
     * Sets the classification of this service. If not called, default values will be used. 
     * 
     * @param kind the service kind
     * @param isDeployable whether the service can be deployed in distributed manner or not (fixed, centralized)
     */
    protected void setClassification(ServiceKind kind, boolean isDeployable) {
        this.kind = kind;
        this.isDeployable = isDeployable;
    }
    
    @Override
    public ServiceDescriptor getEnsembleLeader() {
        return null; // we assume that this is not implemented by default
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ServiceState getState() {
        return null != getEnsembleLeader() ? getEnsembleLeader().getState() : state;
    }

    @Override
    public void setState(ServiceState state) throws ExecutionException {
        if (null != getEnsembleLeader()) {
            getEnsembleLeader().setState(state);
        } else {
            this.state = state;
        }        
    }

    @Override
    public boolean isDeployable() {
        return isDeployable;
    }

    @Override
    public ServiceKind getKind() {
        return kind;
    }
    
    @Override
    public A getArtifact() {
        return artifact;
    }

    @Override
    public List<TypedDataDescriptor> getParameters() {
        return Collections.unmodifiableList(parameters); 
    }
    
    @Override
    public List<TypedDataConnectorDescriptor> getInputDataConnectors() {
        return Collections.unmodifiableList(input); 
    }

    @Override
    public List<TypedDataConnectorDescriptor> getOutputDataConnectors() {
        return Collections.unmodifiableList(output); 
    }
    
    /**
     * Adds a parameter descriptor.
     * 
     * @param parameter the descriptor
     */
    protected void addParameter(TypedDataDescriptor parameter) {
        parameters.add(parameter);
    }

    /**
     * Adds an input descriptor.
     * 
     * @param input the descriptor
     */
    protected void addInputDataConnector(TypedDataConnectorDescriptor input) {
        this.input.add(input);
    }

    /**
     * Adds an output descriptor.
     * 
     * @param output the descriptor
     */
    protected void addOutputDataConnector(TypedDataConnectorDescriptor output) {
        this.output.add(output);
    }

    /**
     * Returns all services in the same ensemble. [public, static for testing]
     * 
     * @param service the service to return the ensemble (in the same artifact) for
     * @return all services in the same ensemble
     */
    public static Set<ServiceDescriptor> ensemble(ServiceDescriptor service) {
        Set<ServiceDescriptor> result = new HashSet<ServiceDescriptor>();
        result.add(service);
        ServiceDescriptor leader = service.getEnsembleLeader();
        if (null != leader) {
            result.add(leader);
        } else {
            leader = service;
        }
        for (ServiceDescriptor s : service.getArtifact().getServices()) {
            if (leader == s.getEnsembleLeader()) {
                result.add(s);
            }
        }
        return result;
    }
    
    /**
     * Returns the names of all channel names of connectors within the ensemble of {@code service}.
     * 
     * @param service the service to return the channel names for
     * @return all channel names of connectors within the ensemble of {@code service}
     */
    public static Set<String> ensembleConnectorNames(ServiceDescriptor service) {
        return internalConnectorNames(ensemble(service));
    }

    /**
     * Returns the ids of all connectors within {@code services}.
     * 
     * @param services the services to return the internal channel names for
     * @return all ids of connectors within the ensemble of {@code service}
     */
    public static Set<String> internalConnectorNames(Collection<? extends ServiceDescriptor> services) {
        Set<String> in = new HashSet<String>();
        Set<String> out = new HashSet<String>();
        for (ServiceDescriptor s : services) {
            in.addAll(connectorIds(s.getInputDataConnectors()));
            out.addAll(connectorIds(s.getOutputDataConnectors()));
        }
        in.retainAll(out);
        return in;
    }

    /**
     * Returns all connector ids for the connectors in {@code cons}.
     * 
     * @param cons the connectors
     * @return the connector ids
     */
    public static Set<String> connectorIds(Collection<TypedDataConnectorDescriptor> cons) {
        return cons
            .stream()
            .filter(c -> c.getId() != null && c.getId().length() > 0)
            .map(c -> c.getId())
            .collect(Collectors.toSet());
    }

}
