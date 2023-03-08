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

import java.util.Collection;
import java.util.Set;

import de.iip_ecosphere.platform.support.iip_aas.IipVersion;

/**
 * A service provider interface for managing containers in the IIP-Ecosphere platform. The interface is rather simple 
 * as it shall be usable through an AAS. The id of a container used here must not be identical to some 
 * container-specific ids or the name in  {@link ContainerDescriptor#getName()}, e.g., it may contain the version.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ContainerManager extends ContainerOperations {

    /**
     * Returns the ids of all available containers.
     * 
     * @return the ids
     */
    public Set<String> getIds();
    
    /**
     * Returns the available (installed) containers independent of their state.
     * 
     * @return the container descriptors
     */
    public Collection<? extends ContainerDescriptor> getContainers();
    
    /**
     * Returns a container descriptor.
     * 
     * @param id the id of the container (may be <b>null</b> or invalid)
     * @return the related container descriptor or <b>null</b> if the container is not known at all
     */
    public ContainerDescriptor getContainer(String id); 
        
    /**
     * Returns the version of this ECS runtime.
     * 
     * @return the version, by default the platform version
     */
    public default String getVersion() {
        IipVersion versionInfo = IipVersion.getInstance();
        return versionInfo.getVersion();
    }

    /**
     * Returns the descriptive name of this ECS runtime.
     * 
     * @return the descriptive name
     */
    public default String getRuntimeName() {
        return "IIP-Ecosphere default ECS-Runtime";
    }

}
