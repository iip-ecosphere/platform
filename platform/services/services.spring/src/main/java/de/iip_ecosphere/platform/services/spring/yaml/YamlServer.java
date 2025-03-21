/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.spring.yaml;

import java.util.List;

import de.iip_ecosphere.platform.services.spring.descriptor.Endpoint;
import de.iip_ecosphere.platform.services.spring.descriptor.Server;

/**
 * Server process specification of servers to be started/stopped with an application.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlServer extends de.iip_ecosphere.platform.services.environment.YamlServer 
    implements Server {

    private long memory = -1;
    
    @Override
    public List<String> getCmdArg(int port, String protocol) {
        return YamlProcess.substCmdArg(getCmdArg(), port, protocol);
    }

    @Override
    public Endpoint getServiceStreamEndpoint() {
        return null;
    }

    @Override
    public Endpoint getStreamEndpoint() {
        return null;
    }

    @Override
    public Endpoint getAasEndpoint() {
        return null;
    }

    @Override
    public int getWaitTime() {
        return 0;
    }

    @Override
    public long getMemory() {
        return memory;
    }

    /**
     * Defines the desired memory for instances of this service.
     * 
     * @param memory the desired memory in <a href="https://en.wikipedia.org/wiki/Mebibyte">Mebibytes</a> (i.e., "m"), 
     *   ignored if not positive
     */
    public void setMemory(long memory) {
        this.memory = memory;
    }

}
