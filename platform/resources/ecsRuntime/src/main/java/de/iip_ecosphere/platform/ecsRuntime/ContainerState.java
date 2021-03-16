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
 * Defines container states.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum ContainerState {

    /**
     * The container is on the device and ready to be deployed. Next state shall be {@link #DEPLOYING}.
     */
    AVAILABLE,
    
    /**
     * The container is brought to life. Next state shall be {@link #DEPLOYED}.
     */
    DEPLOYING,

    /**
     * The container is running. Next state may be {@link #FAILED}, {@link #MIGRATING}, {@link #UPDATING} 
     * or {@link #STOPPING}.
     */
    DEPLOYED,
    
    /**
     * The container failed for some reason. Next state may be {@link #DEPLOYED}, {@link #MIGRATING}, {@link #UPDATING} 
     * or {@link #STOPPING}. 
     */
    FAILED,
    
    /**
     * The container is not operating as it is about or in the process of being migrated. Next state may be 
     * {@link #DEPLOYED}, {@link #FAILING} or {@link #STOPPING}. 
     */
    MIGRATING,
    
    /**
     * The container is not operating as it is being updated with a more recent version. Next state may be 
     * {@link #DEPLOYED}, {@link #FAILING} or {@link #STOPPING}. 
     */
    UPDATING,
    
    /**
     * The container is going out of operation. Next state shall be {@link #STOPPED}. 
     */
    STOPPING,
    
    /**
     * The container was stopped and may be disposed or started again. Next state may be {@link #UNDEPLOYING} 
     * or {@link #UNDEPLOYING}. 
     */
    STOPPED,
    
    /**
     * The container is about to be removed from this resource. Next state may be {@link #UNKOWN}.
     */
    UNDEPLOYING,
    
    /**
     * The state of the container is not known.
     */
    UNKOWN

}
