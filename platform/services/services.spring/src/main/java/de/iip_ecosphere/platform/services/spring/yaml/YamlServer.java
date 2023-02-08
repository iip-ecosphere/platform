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

import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.services.environment.AbstractYamlServer;
import de.iip_ecosphere.platform.services.spring.descriptor.Server;

/**
 * Server process specification of servers to be started/stopped with an application.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlServer extends AbstractYamlServer implements Server {
    
    private String cls;
    private List<String> args = new ArrayList<>();

    @Override
    public String getCls() {
        return cls;
    }

    @Override
    public List<String> getArguments() {
        return args;
    }

    /**
     * Defines the (integration) Java class to be executed.
     * 
     * @param cls the class
     */
    public void setCls(String cls) {
        this.cls = cls;
    }

    /**
     * Defines the arguments to be passed to the server implementation upon startup.
     * 
     * @param args the arguments
     */
    public void setArguments(List<String> args) {
        this.args = args;
    }

}
