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

package de.iip_ecosphere.platform.deviceMgt.ssh;

/**
 * The SshProxyServer is able to create a proxy to the ssh server of the device of resourceId.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class SshProxyFactory {

    /**
     * Create a new {@code SshProxyFactory} for {@code resourceId}.
     *
     * Keep in mind, that this method won't care about old instances.
     * 
     * @param resourceId the resource id of the device
     * @return a new SshProxyServer
     */
    public static SshProxyServer createProxy(String resourceId) {
        // find edge ip
        SshProxyServer edge = new SshProxyServer("localhost", 5555, 0);
        return edge;
    }

}
