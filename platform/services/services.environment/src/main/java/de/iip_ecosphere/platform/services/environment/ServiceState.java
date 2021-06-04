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

package de.iip_ecosphere.platform.services.environment;

/**
 * Service states. See also platform handbook, state machine for valid service state transitions.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum ServiceState {
    
    /**
     * The service implementation is available but not in any other state.
     */
    AVAILABLE,
    
    /**
     * The service is being deployed, but it is not "created" and not running.
     */
    DEPLOYING,
    
    /**
     * The local service instance being observed has been created.
     */
    CREATED,
    
    /**
     * The service is starting. The transition from {@link #DEPLOYING} to {@link #STARTING} may be direct/short.
     */
    STARTING,
    
    /**
     * The service is running under normal conditions, i.e., processing data.
     */
    RUNNING,
    
    /**
     * Something failed. The service is not behaving normally. May go back to {@code #RUNNING}, {@link #RECOVERING},
     * or {@link #STOPPED}.
     */
    FAILED,
    
    /**
     * The service is stopping.
     */
    STOPPING,
    
    /**
     * The service is stopped, in particular not {@link #RUNNING}/processing..
     */
    STOPPED,
    
    /**
     * The service is passivating for an adaptation.
     */
    PASSIVATING,
    
    /**
     * The service is passivated and can be adapted/migrated safely.
     */
    PASSIVATED,
    
    /**
     * The service is {@link #PASSIVATED} and in progress of being migrated.
     */
    MIGRATING,
    
    /**
     * The service is re-activating from {@link #PASSIVATED} or {@link #MIGRATING}. 
     */
    ACTIVATING,
    
    /**
     * The service is recovering from a failure, i.e., not {@link #RUNNING}/processing.
     */
    RECOVERING,

    /**
     * The service is recovered and shall now switch back to the previous non-failure/recovering state, in particular 
     * {@link #RUNNING}/processing.
     */
    RECOVERED,
    
    /**
     * The service is being reconfigured. Depending on the reconfiguration, this may go via {@link #PASSIVATING}, 
     * {@link #PASSIVATED}, {@link #ACTIVATING} or it may directly come from/go to {@link #RUNNING}.
     */
    RECONFIGURING,
    
    /**
     * After {@link #STOPPED} the service is not going back to {@link #STARTING} rather than being undeployed and
     * disposed.
     */
    UNDEPLOYING,
    
    /**
     * The state of the service is not known.
     */
    UNKOWN
    
}