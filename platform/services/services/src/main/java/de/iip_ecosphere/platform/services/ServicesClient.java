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

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * Services client operations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServicesClient extends ServiceOperations {

    /**
     * Returns the services associated to a given {@code artifactId}.
     * 
     * @param artifactId the artifactId to search for
     * @return the associated service ids, empty if none was found
     */
    public String[] getServices(String artifactId);
    
    /**
     * Returns the collection with all services of the resources this client was created for.
     * 
     * @return the services collection, may be <b>null</b> for none
     */
    public SubmodelElementCollection getServices();
    
    /**
     * Returns the collection with all artifacts of the resources this client was created for.
     * 
     * @return the artifacts collection, may be <b>null</b> for none
     */
    public SubmodelElementCollection getArtifacts();
    
}
