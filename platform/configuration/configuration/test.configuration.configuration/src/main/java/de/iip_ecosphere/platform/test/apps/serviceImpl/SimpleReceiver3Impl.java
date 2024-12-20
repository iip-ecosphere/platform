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

package de.iip_ecosphere.platform.test.apps.serviceImpl;

import java.io.InputStream;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.Rec13;
import iip.impl.SimpleDataReceiver3Impl;

/**
 * A simple receiver implementation just printing out the received data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimpleReceiver3Impl extends SimpleDataReceiver3Impl {

    /**
     * Fallback constructor.
     */
    public SimpleReceiver3Impl() {
        super(ServiceKind.SINK_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimpleReceiver3Impl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public void processRec13(Rec13 data) {
        System.out.println("RECEIVED " + data.getStringField() + " " + data.getIntField());
    }

}
