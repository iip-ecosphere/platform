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

package de.iip_ecosphere.platform.examples.pythonCondaVenv;

import java.io.InputStream;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.PythonVenvTestOutput;
import iip.impl.SimplePythonCondaVenvDataReceiverImpl;

/**
 * A simple receiver implementation just printing out the received data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimplePythonCondaVenvReceiverImpl extends SimplePythonCondaVenvDataReceiverImpl {

    /**
     * Fallback constructor.
     */
    public SimplePythonCondaVenvReceiverImpl() {
        super(ServiceKind.SINK_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimplePythonCondaVenvReceiverImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public void processPythonVenvTestOutput(PythonVenvTestOutput data) {
        System.out.printf("RECEIVED id=%d val1=%.2f val2=%.2f conf=%.2f pred=%b processedBy='%s and %s'%n", data.getId(), data.getValue1(), 
            data.getValue2(), data.getConfidence(), data.getPrediction(), data.getEnv1(), data.getEnv2());
    }

}
