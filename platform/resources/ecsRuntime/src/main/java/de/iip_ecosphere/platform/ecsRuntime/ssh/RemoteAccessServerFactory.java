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

package de.iip_ecosphere.platform.ecsRuntime.ssh;

/**
 * Provides access to the RemoteAccessServer instances.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class RemoteAccessServerFactory {

    private static RemoteAccessServer server;

    /**
     * Create a remote access server.
     *
     * @return the created remote access server
     */
    public static RemoteAccessServer create() {
        if (null == server) {
            server = new RemoteAccessServer();
        }
        return server;
    }

}
