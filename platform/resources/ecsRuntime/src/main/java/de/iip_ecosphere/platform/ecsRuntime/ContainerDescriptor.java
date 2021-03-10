/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.services.Version;

/**
 * Describes a container.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ContainerDescriptor {
    
    /**
     * Returns the container id.
     * 
     * @return the container id
     */
    public String getId();
    
    /**
     * The name of the container.
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * The version of the container.
     * 
     * @return the version
     */
    public Version getVersion();
    
    /**
     * Returns the state the container is currently in.
     * 
     * @return the state
     */
    public ContainerState getState();

}
