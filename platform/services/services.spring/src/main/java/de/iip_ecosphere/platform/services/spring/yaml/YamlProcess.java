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

package de.iip_ecosphere.platform.services.spring.yaml;

import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.services.spring.descriptor.Endpoint;
import de.iip_ecosphere.platform.services.spring.descriptor.ProcessSpec;
import de.iip_ecosphere.platform.services.spring.descriptor.Service;

/**
 * If the service is not completely implemented rather than delegates functionality to an additional process that
 * must be started and managed along with the service. The process implementation (whatever it is) will be extracted 
 * from {@link #getHomePath()}. For the execution in a shell, the home directory will be set to the folder where the 
 * files in {@link #getHomePath()} are located.
 *  
 * @author Holger Eichelberger, SSE
 */
public class YamlProcess extends de.iip_ecosphere.platform.services.environment.YamlProcess implements ProcessSpec {

    private YamlEndpoint serviceStreamEndpoint;
    private YamlEndpoint streamEndpoint;
    private YamlEndpoint aasEndpoint;
    private int waitTime = 0;
    
    @Override
    public List<String> getCmdArg(int port, String protocol) {
        return substCmdArg(getCmdArg(), port, protocol);
    }

    /**
     * Returns additional/optional command line arguments required to start the service. The port placeholder
     * {@link Endpoint#PORT_PLACEHOLDER} will be replaced with the command port the platform is using to send
     * administrative commands to the service (see {@link de.iip_ecosphere.platform.services.environment.Service}).
     *
     * @param cmdArg the command line arguments to be used as basis for substitution
     * @param port the port used for the command communication
     * @param protocol the protocol used for the command communication
     * @return the resolved command line arguments (may be empty for none)
     */
    public static List<String> substCmdArg(List<String> cmdArg, int port, String protocol) {
        List<String> result = new ArrayList<String>();
        for (String arg : cmdArg) {
            arg = arg.replace(Endpoint.PORT_PLACEHOLDER, String.valueOf(port));
            arg = arg.replace(Service.PROTOCOL_PLACEHOLDER, String.valueOf(protocol));
            arg = toSubstFileName(arg);
            result.add(arg);
        }
        return result;
    }

    @Override
    public YamlEndpoint getServiceStreamEndpoint() {
        return serviceStreamEndpoint;
    }

    @Override
    public YamlEndpoint getStreamEndpoint() {
        return streamEndpoint;
    }
    
    @Override
    public YamlEndpoint getAasEndpoint() {
        return aasEndpoint;
    }

    @Override
    public int getWaitTime() {
        return waitTime;
    }

    /**
     * Defines communication endpoint (port/host) for streaming on the service side (to communicate with the 
     * process side). [required by SnakeYaml] 
     * 
     * @param serviceStreamEndpoint the communication endpoint
     */
    public void setServiceStreamEndpoint(YamlEndpoint serviceStreamEndpoint) {
        this.serviceStreamEndpoint = serviceStreamEndpoint;
    }

    /**
     * Defines communication endpoint (port/host) on the process side the service shall communicate with. 
     * [required by SnakeYaml] 
     * 
     * @param streamEndpoint the communication endpoint
     */
    public void setStreamEndpoint(YamlEndpoint streamEndpoint) {
        this.streamEndpoint = streamEndpoint;
    }
    
    /**
     * Defines communication endpoint (port/host) the service shall communicate with. [required by SnakeYaml] 
     * 
     * @param aasEndpoint the communication endpoint
     */
    public void setAasEndpoint(YamlEndpoint aasEndpoint) {
        this.aasEndpoint = aasEndpoint;
    }

    /**
     * Defines the time to wait for the process before going on with starting other services.
     * 
     * @param waitTime the wait time in ms, ignored if not positive
     */
    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

}
