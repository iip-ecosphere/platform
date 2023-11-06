/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.test.apps.serviceImpl.routingTest;

import java.io.InputStream;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.RoutingCommand;
import iip.datatypes.RoutingCommandImpl;
import iip.datatypes.RoutingTestData;
import iip.impl.RoutingSinkImpl;

/**
 * The sink of the routing test app.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SinkImpl extends RoutingSinkImpl {

    /**
     * Fallback constructor.
     */
    public SinkImpl() {
        super(ServiceKind.SINK_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SinkImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public void processRoutingTestData(RoutingTestData data) {
        System.out.println("RECEIVED: " + data);
        if (data.getSerNr() % 10 == 0 && hasRoutingCommandIngestor()) {
            RoutingCommand cmd = new RoutingCommandImpl();
            cmd.setCmd("Batch completed");
            ingestRoutingCommand(cmd);
            System.out.println("Sending command: " + cmd);
        }
    }

}
