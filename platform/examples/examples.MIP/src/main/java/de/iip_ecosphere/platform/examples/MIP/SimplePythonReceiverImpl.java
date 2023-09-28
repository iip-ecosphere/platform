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

package de.iip_ecosphere.platform.examples.MIP;

import java.io.InputStream;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.MipAiPythonOutput;
import iip.impl.SimplePythonDataReceiverImpl;

/**
 * A simple receiver implementation just printing out the received data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimplePythonReceiverImpl extends SimplePythonDataReceiverImpl {

    /**
     * Fallback constructor.
     */
    public SimplePythonReceiverImpl() {
        super(ServiceKind.SINK_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimplePythonReceiverImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public void processMipAiPythonOutput(MipAiPythonOutput data) {
        System.out.printf("RECEIVED mipcontext=%s mipdate=%s mipfrom=%s mipid_tag=%s mipreader=%s "
                + "mipraw_signal_clock=%s mipraw_signal_data1=%s mipraw_signal_data2=%s %n", 
            data.getAicontext(), data.getAidate(), data.getAifrom(), data.getAiid_tag(), data.getAireader(), 
            data.getAiraw_signal_clock(), data.getAiraw_signal_data1(), data.getAiraw_signal_data2());
    }

}
