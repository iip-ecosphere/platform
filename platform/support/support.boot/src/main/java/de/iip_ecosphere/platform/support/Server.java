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
 * A generic server, something that can be started or stopped and, finally, disposed.
 * A platform managed server is instantiated via a public constructor receiving a {@code String[]} with
 * command-line arguments - intended for handcrafted servers - or (less priority) a public non-arg 
 * constructor - intended for generated servers.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Server {

    /**
     * Start the server without waiting time/blocking.
     * 
     * @return <b>this</b>
     */
    public Server start();
    
    /**
     * Stop the server. So far, we make no statement whether re-starting the server is possible, but it is safe to 
     * assume that re-starting is not foreseen.
     * 
     * @param dispose shall also allocated resources of this server be disposed
     */
    public void stop(boolean dispose);

    /**
     * Stops a given server instance.
     * 
     * @param server the server instance, may be <b>null</b>
     * @param dispose shall also allocated resources of this server be disposed
     */
    public static void stop(Server server, boolean dispose) {
        if (null != server) {
            server.stop(dispose);
        }
    }

}
