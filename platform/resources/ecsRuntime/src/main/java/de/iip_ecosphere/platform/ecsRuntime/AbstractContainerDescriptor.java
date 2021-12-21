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

import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Abstract {@link ContainerDescriptor} implementation, e.g., including a representation of the {@link ServiceState} 
 * statemachine.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractContainerDescriptor implements ContainerDescriptor {
    
    private String id;
    private String name;
    private Version version;
    private ContainerState state;
    private URI uri;
    
    /**
     * Creates a container descriptor instance.
     */
    public AbstractContainerDescriptor() {
    }
    
    /**
     * Creates a container descriptor instance.
     * 
     * @param id the container id
     * @param name the (file) name of the container
     * @param version the version of the container
     * @param uri the URI where the descriptor was loaded from
     * @throws IllegalArgumentException if {@code id}, {@code name}, {@code version} or {@code uri} is invalid, e.g., 
     *     <b>null</b> or empty
     */
    protected AbstractContainerDescriptor(String id, String name, Version version, URI uri) {
        if (null == id || id.length() == 0) {
            throw new IllegalArgumentException("id must not be null or empty");
        }
        if (null == name || name.length() == 0) {
            throw new IllegalArgumentException("name must not be null or empty");
        }
        if (null == version) {
            throw new IllegalArgumentException("version must not be null");
        }
        this.id = id;
        this.name = name;
        this.version = version;
        this.state = ContainerState.AVAILABLE;
        setUri(uri);
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
    
    /**
     * Defines the container's id. Typically, the id of a container shall not be modified at all. If this is needed
     * for some reason, implementing classes may use this with care. Use the constructor instead.
     * 
     * @param id the container id
     */
    protected void setId(String id) {
        this.id = id;
    }
    
    /**
     * Defines the container's name. Typically, the name of a container shall not be modified at all. If this is needed
     * for some reason, implementing classes may use this with care. Use the constructor instead.
     * 
     * @param name the container name
     */
    protected void setName(String name) {
        this.name = name;
    }
    /**
     * Defines the container's version.
     * @param version
     */
    protected void setVersion(Version version) {
        this.version = version;
    }
    
    /**
     * Changes the container state.
     * 
     * @param state the new container state
     */
    protected void setState(ContainerState state) {
        if (null != state) {
            this.state = state;
        }
    }

    @Override
    public ContainerState getState() {
        return state;
    }
    
    @Override
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI where the container was loaded from.
     * 
     * @param uri the URI where the descriptor was loaded from
     * @throws IllegalArgumentException if {@code uri} is invalid, e.g., <b>null</b>
     */
    protected void setUri(URI uri) {
        if (null == uri) {
            throw new IllegalArgumentException("uri must not be null");
        }
        this.uri = uri.normalize();
    }
    
}
