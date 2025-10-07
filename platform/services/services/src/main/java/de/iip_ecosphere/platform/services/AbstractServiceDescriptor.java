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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.ServiceStub;
import de.iip_ecosphere.platform.support.Version;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Abstract {@link ServiceDescriptor} implementation, e.g., including a representation of the {@link ServiceState} 
 * statemachine. We do not protect the setters here explicitly, e.g., through a builder pattern as we assume that 
 * the respective messages will only be called within the package of the implementing manager. 
 * 
 * Holds a {@link ServiceStub} while the underlying service instance is operational. Must be set by the service manager,
 * will be released when state goes to {@link ServiceState#STOPPING}.
 * 
 * @param <A> the type of artifact descriptor
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractServiceDescriptor<A extends ArtifactDescriptor> implements ServiceDescriptor {
    
    private String id;
    private String applicationId;
    private String name;
    private String description;
    private Version version;
    private A artifact;
    private ServiceState state;
    private ServiceKind kind = ServiceKind.TRANSFORMATION_SERVICE;
    private boolean isDeployable = true;
    private boolean isTopLevel = true;
    private List<TypedDataDescriptor> parameters = new ArrayList<>(); 
    private List<TypedDataConnectorDescriptor> input = new ArrayList<>(); 
    private List<TypedDataConnectorDescriptor> output = new ArrayList<>(); 
    private ServiceStub stub;
    private List<String> additionalArgs;
    private Map<Action, Runnable> actions; // -> abstract
    
    /**
     * Creates an instance. Call {@link #setClassification(ServiceKind, boolean, boolean)} afterwards.
     * 
     * @param id the service id
     * @param applicationId the application id, may be empty for default application/legacy
     * @param name the name of this service
     * @param description the description of the service
     * @param version the version
     */
    protected AbstractServiceDescriptor(String id, String applicationId, String name, String description, 
        Version version) {
        this.id = id;
        this.applicationId = applicationId;
        this.name = name;
        this.description = description;
        this.version = version;
        this.state = ServiceState.AVAILABLE;
    }

    /**
     * Does further initialization after {@link #AbstractServiceDescriptor(String, String, String, String, Version)}
     * to instantiate an instance from {@code template}. By default, adds parameters and connectors.
     * 
     * @param template the descriptor being used as template
     * @see #instantiateFrom(AbstractServiceDescriptor, boolean, boolean)
     */
    protected void instantiateFrom(AbstractServiceDescriptor<A> template) {
        instantiateFrom(template, true, true);
    }
    
    /**
     * Does further initialization after {@link #AbstractServiceDescriptor(String, String, String, String, Version)}
     * to instantiate an instance from {@code template}.
     * 
     * @param template the descriptor being used as template
     * @param addParameters add the parameters from {@code template}
     * @param addConnectors add the input/output connectors from {@code template}
     */
    protected void instantiateFrom(AbstractServiceDescriptor<A> template, 
        boolean addParameters, boolean addConnectors) {
        this.artifact = template.artifact;
        this.kind = template.kind;
        this.isDeployable = template.isDeployable;
        this.isTopLevel = template.isTopLevel;
        if (addParameters) {
            this.parameters.addAll(template.parameters); 
        }
        if (addConnectors) {
            this.input.addAll(template.input); 
            this.input.addAll(template.output); 
        }
        // not the stub
    }
    
    /**
     * Returns the utilized artifact descriptor class.
     * 
     * @return the class
     */
    protected abstract Class<A> getArtifactDescriptorClass();

    /**
     * Defines an artifact.
     * 
     * @param artifact the containing artifact descriptor
     */
    protected void setArtifact(ArtifactDescriptor artifact) {
        Class<A> cls = getArtifactDescriptorClass();
        if (cls.isInstance(artifact)) {
            this.artifact = cls.cast(artifact);
        } else {
            throw new IllegalArgumentException("artifact is not of type " 
                + getArtifactDescriptorClass().getClass().getName());
        }
    }
    
    /**
     * Sets the classification of this service. If not called, default values will be used. 
     * 
     * @param kind the service kind
     * @param isDeployable whether the service can be deployed in distributed manner or not (fixed, centralized)
     * @param isTopLevel whether the service is a top-level service or whether it is nested, e.g., family member
     */
    protected void setClassification(ServiceKind kind, boolean isDeployable, boolean isTopLevel) {
        this.kind = kind;
        this.isDeployable = isDeployable;
        this.isTopLevel = isTopLevel;
    }
    
    @Override
    public ServiceDescriptor getEnsembleLeader() {
        return null; // we assume that this is not implemented by default
    }
    
    @Override
    public String getId() {
        return id; // no stub here, we shall have consistent information
    }
    
    @Override
    public String getApplicationId() {
        return applicationId; // no stub here, we shall have consistent information
    }

    @Override
    public String getName() {
        return name; // no stub here, we shall have consistent information
    }

    @Override
    public Version getVersion() {
        return version; // no stub here, we shall have consistent information
    }

    @Override
    public String getDescription() {
        return description; // no stub here, we shall have consistent information
    }

    @Override
    public ServiceState getState() {
        ServiceState result;
        if (null != stub) {
            result = stub.getState();
            if (null == result) {
                result = state; // if AAS getter fails, e.g., service down
            } else {
                this.state = result; // keep the descriptor shadow state up to date
            }
        } else {
            result = state;
        }
        return result;
    }
    
    // checkstyle: checkstyle: stop exception type check

    @Override
    public void setState(ServiceState state) throws ExecutionException {
        ServiceState.validateTransition(this.state, state);
        if (null != stub) {
            try {
                stub.setState(state);
                // if service/stub made an implicit transition, take it up
                ServiceState tmp = stub.getState(); // may do a transition
                if (null != tmp) { // may fail if already gone, then ignore
                    state = tmp;
                }
            } catch (Throwable e) {
                // may fail, e.g., shutdown may just be faster, keep local; handover needed
                if (state != ServiceState.STOPPED && state != ServiceState.UNDEPLOYING) {
                    LoggerFactory.getLogger(getClass()).info("Cannot set state {} for service '{}' via AAS. "
                        + "Falling back to local state. Resason: {} {}", state, getId(), e.getClass(), e.getMessage());
                } // else: probably AAS server already gone, ignore
            }
        }
        this.state = state; // keep the descriptor shadow state up to date
    }
    
    // checkstyle: checkstyle: resume exception type check

    @Override
    public boolean isDeployable() {
        return isDeployable; // no stub here, we shall have consistent information
    }

    @Override
    public boolean isTopLevel() {
        return isTopLevel; // no stub here, we shall have consistent information
    }

    @Override
    public ServiceKind getKind() {
        return kind; // no stub here, we shall have consistent information
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
    
    @Override
    public List<TypedDataConnectorDescriptor> getDataConnectors() {
        List<TypedDataConnectorDescriptor> result = new ArrayList<>();
        result.addAll(input);
        result.addAll(output);
        return result; 
    }
    
    /**
     * Returns a service stub to control the service when it is running.
     *  
     * @return the service stub (may be <b>null</b> if the service is only available or about to be removed)
     */
    protected ServiceStub getStub() {
        return stub;
    }
    
    /**
     * Defines the actual service stub.
     * 
     * @param stub the stub
     */
    protected void setStub(ServiceStub stub) {
        this.stub = stub;
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

    @Override
    public void setAdditionalArguments(List<String> additionalArgs) {
        this.additionalArgs = additionalArgs;
    }
    
    @Override
    public List<String> getAdditionalArguments() {
        return this.additionalArgs;
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

    @Override
    public void attachAction(Action action, Runnable run) {
        if (null == actions) { // lazy init
            actions = new HashMap<>();
        }
        actions.put(action, run);        
    }

    /**
     * If there is a registered runnable for {@code action}, execute it.
     * 
     * @param action the action to look for
     */
    protected void executeAction(Action action) {
        Runnable act = actions == null ? null : actions.get(action);
        if (null != act) {
            act.run();
        }
    }    
    
}
