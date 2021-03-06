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

package de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Implements a container descriptor for Kubernetes-based container management.
 * 
 * @author Ahmad Alomosh, SSE
 */
public class KubernetesContainerDescriptor extends AbstractContainerDescriptor {
    
    /**
     * Creates a container descriptor instance.
     */
    public KubernetesContainerDescriptor() {
    }

    /**
     * Creates a container descriptor instance.
     * 
     * @param id the container id
     * @param name the (file) name of the container
     * @param version the version of the container
     * @throws IllegalArgumentException if id, name or version is invalid, i.e., null or empty
     */
    protected KubernetesContainerDescriptor(String id, String name, Version version) {
        super(id, name, version);
    }
    
    @Override
    public void setId(String id) {
        super.setId(id);
    }
    
    @Override
    public void setName(String name) {
        super.setName(name);
    }
    
    @Override
    public void setVersion(Version version) {
        super.setVersion(version);
    }
    
    @Override
    public void setState(ContainerState state) {
        super.setState(state);
    }
    
}
