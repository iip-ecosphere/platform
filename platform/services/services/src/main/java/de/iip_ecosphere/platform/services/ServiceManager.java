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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A service provider interface for managing services in the IIP-Ecosphere platform.
 * Service implementations shall be bound by code generation to a specific (stream) service computing approach, e.g.,
 * Spring Cloud Streams. The management of such services happens via this interface, which shall utilize the respective
 * management capabilites of the underlying service computing approach. The interface is rather simple as it shall
 * be usable through an AAS. The id of a service used here must not be identical to the name in 
 * {@link ServiceDescriptor#getName()}, e.g., it may contain the version. Implementations shall call the notify methods 
 * in {@link ServicesAas}.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServiceManager extends ServiceOperations {

    public static final String PROGRESS_COMPONENT_ID = "Service Manager";

    /**
     * Clones the artifact. As services belong to artifacts, it is not possible to migrate a single service (except
     * for if the artifact consists of a single service). [adaptation]
     * 
     * @param artifactId the id of the service
     * @param location the target location, e.g., a device
     * @throws ExecutionException if migration fails
     */
    public void cloneArtifact(String artifactId, URI location) throws ExecutionException;
    
    /**
     * Returns all information about parameter for the given {@code serviceId}. [adaptation]
     * 
     * @param serviceId the serviceId of the service
     * @return the descriptors for all supported parameters
     */
    public List<TypedDataDescriptor> getParameters(String serviceId);

    /**
     * Returns all input connector information for the given {@code serviceId}.
     * 
     * @param serviceId the serviceId of the service
     * @return the descriptors for all input data connectors
     */
    public List<TypedDataConnectorDescriptor> getInputDataConnectors(String serviceId);

    /**
     * Returns all output connector information for the given {@code serviceId}.
     * 
     * @param serviceId the serviceId of the service
     * @return the descriptors for all output data connectors
     */
    public List<TypedDataConnectorDescriptor> getOutputDataConnectors(String serviceId);

    /**
     * Returns the ids of all available artifacts.
     * 
     * @return the ids
     */
    public Set<String> getArtifactIds();
    
    /**
     * Returns the ids of all available services.
     * 
     * @return the ids
     */
    public Set<String> getServiceIds();

    /**
     * Returns the available (installed) artifacts.
     * 
     * @return the services
     */
    public Collection<? extends ArtifactDescriptor> getArtifacts();
    
    /**
     * Returns the available (installed) services in all artifacts.
     * 
     * @return the services
     */
    public Collection<? extends ServiceDescriptor> getServices();

    /**
     * Returns an artifact descriptor.
     * 
     * @param artifactId the id of the service (may be <b>null</b> or invalid)
     * @return the related artifact descriptor or <b>null</b> if the artifact does not exist
     */
    public ArtifactDescriptor getArtifact(String artifactId); 
    
    /**
     * Returns a service descriptor.
     * 
     * @param serviceId the id of the service (may be <b>null</b> or invalid)
     * @return the related service descriptor or <b>null</b> if the service does not exist
     */
    public ServiceDescriptor getService(String serviceId); 
    
}
