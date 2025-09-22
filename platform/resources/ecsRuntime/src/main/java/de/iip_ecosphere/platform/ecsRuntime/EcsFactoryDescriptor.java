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

/**
 * A factory descriptor for Java Service loading.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface EcsFactoryDescriptor {
    
    public static final String PLUGIN_ID = "container";
    
    /**
     * Creates the container manager instance.
     * 
     * @return the instance
     */
    public ContainerManager createContainerManagerInstance();
    
    /**
     * Returns the configuration instance.
     * 
     * @return the configuration
     */
    public EcsSetup getConfiguration();

}
