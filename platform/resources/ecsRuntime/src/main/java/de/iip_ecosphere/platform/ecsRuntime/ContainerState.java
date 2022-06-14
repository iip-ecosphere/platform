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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
    UNKNOWN;

    private static Map<ContainerState, Set<ContainerState>> validTransitions = new HashMap<>();

    static {
        // failed, unknown is always possible
        addValidTransition(UNKNOWN, AVAILABLE);
        addValidTransition(AVAILABLE, DEPLOYING);
        addValidTransition(DEPLOYING, DEPLOYED);
        addValidTransition(DEPLOYED, MIGRATING, UPDATING, STOPPING);
        addValidTransition(MIGRATING, DEPLOYED, STOPPING);
        addValidTransition(UPDATING, DEPLOYED, STOPPING);
        addValidTransition(STOPPING, STOPPED);
        addValidTransition(STOPPED, UNDEPLOYING);

        addValidTransition(FAILED, DEPLOYED, MIGRATING, UPDATING, STOPPING);
    }
    
    /**
     * Adds a valid transition. Transitions to {@link #FAILED} or {@link #UNKOWN} are implicitly
     * valid.
     * 
     * @param source the source state to transition from
     * @param targets the target state(s) to transition to
     */
    private static void addValidTransition(ContainerState source, ContainerState... targets) {
        Set<ContainerState> validTrans = validTransitions.get(source);
        if (null == validTrans) {
            validTrans = new HashSet<ContainerState>();
            validTransitions.put(source, validTrans);
        }
        for (ContainerState t : targets) {
            validTrans.add(t);
        }
    }
    
    /**
     * Returns whether a transition from this state to {@code target} is valid.
     * 
     * @param target the target state 
     * @return {@code true} for valid, {@code false} else
     */
    public boolean isValidTransition(ContainerState target) {
        boolean result = false;
        if (FAILED == target || UNKNOWN == target) {
            result = true;
        } else {
            Set<ContainerState> validTargets = validTransitions.get(this);
            if (null != validTargets) {
                result = validTargets.contains(target);
            }
        }
        return result;
    }
    
    /**
     * Validates a container state transition and throws an exception if the transition is invalid.
     * 
     * @param source the source state
     * @param target the target state
     * @throws ExecutionException if {@code source} is <b>null</b> or a transition from {@code source} to {@code target}
     *    is not valid
     */
    public static void validateTransition(ContainerState source, ContainerState target) throws ExecutionException {
        if (null == source) {
            throw new ExecutionException("No source state given: null", null);
        }
        if (!source.isValidTransition(target)) {
            throw new ExecutionException("State transition from " + source + " to " + target + "is not valid", null);
        }
    }
    
}
