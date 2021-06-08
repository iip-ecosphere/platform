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

package de.iip_ecosphere.platform.services.spring.descriptor;

import java.util.List;

import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Information about an artifact containing services. The artifact is to be deployed. {@link #getId()} and 
 * {@link #getName()} must be given, both not empty. {@link #getServices()} may be empty, but if not the services
 * must be valid. 
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Artifact {

    /**
     * Returns the name of the artifact.
     * 
     * @return the name
     */
    public String getId();

    /**
     * Returns the name of the artifact.
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * Returns the version of this artifact.
     * 
     * @return the version
     */
    public Version getVersion();
    
    /**
     * Returns the contained services.
     * 
     * @return the services
     */
    public List<? extends Service> getServices();

    /**
     * Returns the declared types.
     * 
     * @return the types
     */
    public List<? extends Type> getTypes();

}
