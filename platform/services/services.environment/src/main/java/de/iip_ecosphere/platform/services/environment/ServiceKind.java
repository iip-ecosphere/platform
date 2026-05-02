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

import java.util.Comparator;

/**
 * Defines the service kind.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum ServiceKind {

    /**
     * A source service providing some form of input data, e.g., a connector.
     */
    SOURCE_SERVICE(10),
    
    /**
     * A transformation service in the widest sense, may be an AI service.
     */
    TRANSFORMATION_SERVICE(2),
    
    /**
     * A sink service consuming data and not re-emitting data.
     */
    SINK_SERVICE(1),
    
    /**
     * A probe service receiving data and turning it into alarms or monitoring information.
     */
    PROBE_SERVICE(3),
    
    /**
     * A server process being executed in the environment of a service to utilize the communication 
     * capabilities of the service/transport/parameters. Internal service kind.
     */
    SERVER(0);
    
    /**
     * Comparator for default start sequence, lowest priority first.
     */
    public static final Comparator<ServiceKind> START_COMPARATOR = (k1, k2) 
        -> Integer.compare(k1.getStartPriority(), k2.getStartPriority());

    private int startPriority = 0;
    
    /**
     * Creates a service kind.
     * 
     * @param startPriority the default start priority of the service kind, non-negative integer, the lower the earlier
     */
    private ServiceKind(int startPriority) {
        this.startPriority = startPriority;
    }
    
    /**
     * Returns the start priority.
     * 
     * @return the default start priority of the service kind, non-negative integer, the lower the earlier
     */
    public int getStartPriority() {
        return startPriority;
    }

    /**
     * Returns whether <b>this</b> kind shall be started before {@code kind}.
     * 
     * @param kind the kind to compare
     * @param includeEquals whether equal start priorities shall be counted as before
     * @return {@code true} if <b>this</b> kind shall be started before {@code kind}, {@code false} else
     */
    public boolean before(ServiceKind kind, boolean includeEquals) {
        if (includeEquals && getStartPriority() == kind.getStartPriority()) {
            return true;
        }
        return getStartPriority() < kind.getStartPriority();
    }

    /**
     * Returns whether <b>this</b> kind shall be started after {@code kind}.
     * 
     * @param kind the kind to compare
     * @param includeEquals whether equal start priorities shall be counted as after
     * @return {@code true} if <b>this</b> kind shall be started after {@code kind}, {@code false} else
     */
    public boolean after(ServiceKind kind, boolean includeEquals) {
        if (includeEquals && getStartPriority() == kind.getStartPriority()) {
            return true;
        }
        return getStartPriority() > kind.getStartPriority();
    }

}
