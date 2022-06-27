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

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
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

    private DataIngestor<RoutingTestData> routingIngestor;
    
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
        if (null != routingIngestor) {
            RoutingTestData result = new RoutingTestDataImpl();
            result.setSerNr(data.getSerNr());
            result.setStringField(data.getStringField());
            routingIngestor.ingest(result);
        }
    }

    @Override
    public void attachRoutingTestDataIngestor(DataIngestor<RoutingTestData> ingestor) {
        this.routingIngestor = ingestor;
    }

    @Override
    public void processRoutingConnOut(RoutingConnOut data) {
        if (null != routingIngestor) {
            RoutingTestData result = new RoutingTestDataImpl();
            result.setSerNr(-data.getSerNr());
            result.setStringField(data.getData());
            routingIngestor.ingest(result);
        }
    }

}
