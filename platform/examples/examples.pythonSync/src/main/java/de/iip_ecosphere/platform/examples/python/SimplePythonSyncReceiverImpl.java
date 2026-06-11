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

package de.iip_ecosphere.platform.examples.python;

import java.io.InputStream;

import de.iip_ecosphere.platform.services.environment.AbstractGenericServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.PythonSyncTestOutput;
import iip.impl.SimplePythonSyncDataReceiverImpl;

/**
 * A simple receiver implementation just printing out the received data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimplePythonSyncReceiverImpl extends SimplePythonSyncDataReceiverImpl {

    /**
     * JSL descriptor for dynamic loading.
     */
    public static class Descriptor extends AbstractGenericServicePluginDescriptor<SimplePythonSyncReceiverImpl> {

        /**
         * Creates the instance for JSL.
         */
        public Descriptor() {
            super(PLUGIN_ID_APP_SERVICE_PREFIX + "SimplePythonSyncDataReceiver", SimplePythonSyncReceiverImpl.
                class);
        }

        @Override
        public SimplePythonSyncReceiverImpl createService(String serviceId, InputStream ymlFile) {
            return new SimplePythonSyncReceiverImpl(serviceId, ymlFile);
        }

        @Override
        public SimplePythonSyncReceiverImpl createService() {
            return new SimplePythonSyncReceiverImpl();
        }

    }
    
    /**
     * Fallback constructor.
     */
    public SimplePythonSyncReceiverImpl() {
        super(ServiceKind.SINK_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimplePythonSyncReceiverImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public void processPythonSyncTestOutput(PythonSyncTestOutput data) {
        System.out.printf("RECEIVED id=%d val1=%.2f val2=%.2f conf=%.2f pred=%b%n", data.getId(), data.getValue1(), 
            data.getValue2(), data.getConfidence(), data.getPrediction());
    }

}
