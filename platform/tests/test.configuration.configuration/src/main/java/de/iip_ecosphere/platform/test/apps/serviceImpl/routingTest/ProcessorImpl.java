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
import iip.datatypes.ConnOut;
import iip.datatypes.Rec1;
import iip.impl.RoutingProcessorImpl;

/**
 * The processor of the routing test app.
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
    public void processRec1(Rec1 data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void processConnOut(ConnOut data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void attachRec1Ingestor(DataIngestor<Rec1> ingestor) {
        // TODO Auto-generated method stub
        
    }

}
