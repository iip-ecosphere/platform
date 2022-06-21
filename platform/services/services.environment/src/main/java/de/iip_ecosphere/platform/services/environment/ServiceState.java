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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
    UNKNOWN; 
    
    private static Map<ServiceState, Set<ServiceState>> validTransitions = new HashMap<>();

    static {
        // error, unknown is always possible
        addValidTransition(UNKNOWN, AVAILABLE);
        addValidTransition(AVAILABLE, DEPLOYING, CREATED, STARTING, UNDEPLOYING); // preliminary: created, starting 
        addValidTransition(STARTING, STOPPING); // test fallback :/
        addValidTransition(DEPLOYING, CREATED, STARTING); // preliminary: starting 
        addValidTransition(CREATED, STARTING);
        addValidTransition(STARTING, RUNNING);
        addValidTransition(RUNNING, STOPPING, RECONFIGURING, PASSIVATING);
        addValidTransition(RECONFIGURING, RUNNING, PASSIVATING);
        addValidTransition(PASSIVATING, PASSIVATED);
        addValidTransition(PASSIVATED, MIGRATING, ACTIVATING);
        addValidTransition(MIGRATING, ACTIVATING);
        addValidTransition(ACTIVATING, RUNNING);
        addValidTransition(FAILED, RECOVERING);
        addValidTransition(RECOVERING, RECOVERED);
        addValidTransition(RECOVERED, RUNNING);
        addValidTransition(STOPPING, STOPPED);
        addValidTransition(STOPPED, AVAILABLE, STARTING);
        addValidTransition(UNDEPLOYING, UNKNOWN);
    }
    
    /**
     * Adds a valid transition. Transitions to {@link #FAILED} or {@link #UNKNOWN} are implicitly
     * valid.
     * 
     * @param source the source state to transition from
     * @param targets the target state(s) to transition to
     */
    private static void addValidTransition(ServiceState source, ServiceState... targets) {
        Set<ServiceState> validTrans = validTransitions.get(source);
        if (null == validTrans) {
            validTrans = new HashSet<ServiceState>();
            validTransitions.put(source, validTrans);
        }
        for (ServiceState t : targets) {
            validTrans.add(t);
        }
    }
    
    /**
     * Returns whether a transition from this state to {@code target} is valid.
     * 
     * @param target the target state 
     * @return {@code true} for valid, {@code false} else
     */
    public boolean isValidTransition(ServiceState target) {
        boolean result = false;
        if (FAILED == target || UNKNOWN == target || this == target) { // including self-transition
            result = true;
        } else {
            Set<ServiceState> validTargets = validTransitions.get(this);
            if (null != validTargets) {
                result = validTargets.contains(target);
            }
        }
        return result;
    }
    
    /**
     * Validates a service state transition and throws an exception if the transition is invalid.
     * 
     * @param source the source state
     * @param target the target state
     * @throws ExecutionException if {@code source} is <b>null</b> or a transition from {@code source} to {@code target}
     *    is not valid
     */
    public static void validateTransition(ServiceState source, ServiceState target) throws ExecutionException {
        if (null == source) {
            throw new ExecutionException("No source state given: null", null);
        }
        if (!source.isValidTransition(target)) {
            throw new ExecutionException("State transition from " + source + " to " + target + " is not valid", null);
        }
    }
    
}