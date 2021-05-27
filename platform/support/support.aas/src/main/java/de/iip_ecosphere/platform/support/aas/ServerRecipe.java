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

package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;

/**
 * A recipe to create a standalone AAS server. For local in-memory deployment or deployment to such a standalone AAS 
 * server, please check out the {@link DeploymentRecipe}.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServerRecipe {

    /**
     * Declares the type for persistence constants.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PersistenceType {
    }

    /**
     * Defines local persistence types. Real persistence types are not declared here
     * to reduce dependencies.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum LocalPersistenceType implements PersistenceType {
        
        /**
         * No persistence, keep the data in memory.
         */
        INMEMORY
        
    }
    
    /**
     * Returns a supported persistence type.
     * 
     * @param type the textual representation
     * @return the persistence type, {@link LocalPersistenceType#INMEMORY} as fallback
     */
    public PersistenceType toPersistenceType(String type);
    
    /**
     * Creates a AAS server. If {@code type} is local, a similar server as in {@link DeploymentRecipe} 
     * is created.
     * 
     * @param endpoint the server endpoint (host is ignored, i.e., localhost, but endpoint determines the base URL path)
     * @param persistence the persistence type
     * @param registryEndpoint the endpoint where the (running) AAS registry is located 
     * @param options for the server, names of implementation-specific options to be enabled, 
     *    may be empty for none
     * @return the server instance
     * @throws UnsupportedOperationException if the persistence options cannot be fulfilled, e.g., to create a server 
     *    instance on client side without the server parts installed
     */
    public AasServer createAasServer(Endpoint endpoint, PersistenceType persistence, Endpoint registryEndpoint, 
        String... options);

    /**
     * Creates a standalone AAS registry server. 
     * 
     * @param endpoint the server endpoint (host is ignored, i.e., localhost, but endpoint determines the base URL path)
     * @param persistence the persistence type
     * @param options for the server, names of implementation-specific options to be enabled, 
     *    may be empty for none
     * @return the server instance
     * @throws UnsupportedOperationException if the persistence options cannot be fulfilled, e.g., to create a server 
     *    instance on client side without the server parts installed
     */
    public Server createRegistryServer(Endpoint endpoint, PersistenceType persistence, String... options);
    
}
