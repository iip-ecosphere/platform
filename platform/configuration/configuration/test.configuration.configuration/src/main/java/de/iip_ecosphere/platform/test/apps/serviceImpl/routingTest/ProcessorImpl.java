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
import iip.datatypes.RoutingConnOut;
import iip.datatypes.RoutingTestData;
import iip.datatypes.RoutingTestDataImpl;
import iip.impl.RoutingProcessorImpl;

/**
 * The processor of the routing test app. Just merge the two input streams, the conn stream with negative 
 * serial numbers.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProcessorImpl extends RoutingProcessorImpl {
    
    /**
     * Fallback constructor.
     */
    public ProcessorImpl() {
        super(ServiceKind.TRANSFORMATION_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public ProcessorImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    @Override
    public void processRoutingTestData(RoutingTestData data) {
        System.out.println("Processor received: " + data);
        RoutingTestData result = new RoutingTestDataImpl();
        result.setSerNr(data.getSerNr());
        result.setStringField(data.getStringField());
        ingestRoutingTestData(data);
        System.out.println("Processor sent: " + result);
    }

    @Override
    public void processRoutingConnOut(RoutingConnOut data) {
        System.out.println("Processor received: " + data);
        RoutingTestData result = new RoutingTestDataImpl();
        result.setSerNr(-data.getSerNr());
        result.setStringField(data.getData());
        ingestRoutingTestData(result);
        System.out.println("Processor sent: " + result);
    }

    @Override
    public void processRoutingCommand(RoutingCommand data) {
        System.out.println("Processor received cmd: " + data);
    }

}
