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

package de.iip_ecosphere.platform.services.environment;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Basic implementation of the service interface (aligned with Python).
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractService implements Service {

    private String id;
    private String name;
    private Version version;
    private String description;
    private boolean isDeployable; 
    private ServiceKind kind;
    private ServiceState state;
    

    // checkstyle: stop parameter number check
    
    /**
     * Creates an abstract service.
     * 
     * @param id the id of the service
     * @param name the name of the service
     * @param version the version of the service
     * @param description a description of the service, may be empty
     * @param isDeployable whether the service is decentrally deployable
     * @param kind the service kind
     */
    protected AbstractService(String id, String name, Version version, String description, boolean isDeployable, 
        ServiceKind kind) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.isDeployable = isDeployable;
        this.kind = kind;
        this.state = ServiceState.AVAILABLE;
    }

    // checkstyle: resume parameter number check
    
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
        this.state = state; // for now, no state machine checking
    }

    @Override
    public boolean isDeployable() {
        return isDeployable;
    }

    @Override
    public ServiceKind getKind() {
        return kind;
    }

}
