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

import de.iip_ecosphere.platform.services.spring.descriptor.Endpoint;

/**
 * Represents a communication endpoint.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlEndpoint implements Endpoint {

    private String portArg = "";
    private String hostArg = "";
    
    @Override
    public String getPortArg() {
        return portArg;
    }
    
    @Override
    public boolean isPortArgGeneric() {
        return containsSafe(portArg, PORT_PLACEHOLDER);
    }

    @Override
    public String getPortArg(int port) {
        return replaceSafe(portArg, PORT_PLACEHOLDER, String.valueOf(port));
    }

    @Override
    public String getHostArg() {
        return hostArg;
    }

    @Override
    public boolean isHostArgGeneric() {
        return containsSafe(hostArg, HOST_PLACEHOLDER);
    }

    @Override
    public String getHostArg(String hostname) {
        return replaceSafe(hostArg, HOST_PLACEHOLDER, hostname);
    }
    
    /**
     * Returns whether {@code text} contains {@code target}.
     *  
     * @param text the text to look for
     * @param target the target to search within {@code text}
     * @return if {@code text} and {@code target} are not <b>null</b>, whether {@code text} contains {@code target}
     */
    private static boolean containsSafe(String text, String target) {
        return null != text && null != target && text.contains(target);
    }

    /**
     * Replaces {@code target} in {@code text} by {@code replacement}.
     *  
     * @param text the text to perform the replacement on
     * @param target the target to search within {@code text}
     * @param replacement the text replacing {@code target}
     * @return if {@code text} is <b>null</b> then an empty string, if {@code replacement} is <b>null</b> {@code text} 
     *    else {@code text} with {@code target} replaced by {@code replacement}
     */
    private String replaceSafe(String text, String target, String replacement) {
        String result;
        if (null == text) {
            result = "";
        } else if (null == replacement) {
            result = text;
        } else {
            result = text.replace(target, replacement);
        }
        return result;
    }
    
    /**
     * Defines the command line argument to set the communication port for this relation upon service 
     * deployment/execution. [Required by SnakeYaml]
     * 
     * @param portArg the generic port argument, may contain {@value #PORT_PLACEHOLDER}
     */
    public void setPortArg(String portArg) {
        this.portArg = portArg;
    }

    /**
     * Defines the command line argument to set the host to communicate with for this relation upon service 
     * deployment/execution. [Required by SnakeYaml]
     * 
     * @param hostArg the host argument, may contain {@value #HOST_PLACEHOLDER}
     */
    public void setHostArg(String hostArg) {
        this.hostArg = hostArg;
    }
    
}
