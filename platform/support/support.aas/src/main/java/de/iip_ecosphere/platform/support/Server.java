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

package de.iip_ecosphere.platform.support;

/**
 * A generic server, something that can be started or stopped.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Server {

    /**
     * Start the server without waiting time/blocking.
     */
    public void start();
    
    /**
     * Start the server. The given waiting time may be ignored if not supported by the server implementation.
     * 
     * @param minWaitingTime the minimum waiting time in ms the call shall be blocked and the server shall be up 
     */
    public void start(int minWaitingTime);
    
    /**
     * Stop the server.
     */
    public void stop();

}
