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
import java.util.Set;

/**
 * Describes an artifact consisting of services.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ArtifactDescriptor {

    /**
     * Returns the unique id of the artifact.
     * 
     * @return the artifact id
     */
    public String getId();
    
    /**
     * Returns the name of the artifact.
     * 
     * @return the name, may be the file name
     */
    public String getName();
    
    /**
     * Returns the ids of all services provided by the artifact.
     * 
     * @return the ids
     */
    public Set<String> getServiceIds();
    
    /**
     * Returns the URI the artifact was loaded from.
     * 
     * @return the URI
     */
    public URI getUri();
    
    /**
     * Returns the services provided by the artifact independent of their state.
     * 
     * @return the service descriptors
     */
    public Collection<? extends ServiceDescriptor> getServices();
    
    /**
     * Returns a service descriptor.
     * 
     * @param id the id of the service
     * @return the related service descriptor or <b>null</b> if the service is not provided by this artifact
     */
    public ServiceDescriptor getService(String id); 
    
}
