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

import java.util.concurrent.ExecutionException;

/**
 * Abstract {@link ServiceDescriptor} implementation, e.g., including a representation of the {@link ServiceState} 
 * statemachine. We do not protect the setters here explicitly, e.g., through a builder pattern as we assume that 
 * the respective messages will only be called within the package of the implementing manager.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractServiceDescriptor implements ServiceDescriptor {
    
    private String id;
    private String name;
    private String description;
    private Version version;
    private ArtifactDescriptor artifact;
    private ServiceState state;
    private ServiceKind kind = ServiceKind.TRANSFORMATION_SERVICE;
    private boolean isDeployable = true;
    
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
    protected void setArtifact(ArtifactDescriptor artifact) {
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
        return state;
    }

    @Override
    public void setState(ServiceState state) throws ExecutionException {
        // TODO statemachine?
        this.state = state;
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
    public ArtifactDescriptor getArtifact() {
        return artifact;
    }

}
