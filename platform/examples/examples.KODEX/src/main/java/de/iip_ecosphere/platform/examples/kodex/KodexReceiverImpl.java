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

package de.iip_ecosphere.platform.examples.kodex;

import java.io.InputStream;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.KRec13Anon;
import iip.impl.KodexDataReceiverImpl;

/**
 * A simple receiver implementation just printing out the received data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class KodexReceiverImpl extends KodexDataReceiverImpl {

    /**
     * Fallback constructor.
     */
    public KodexReceiverImpl() {
        super(ServiceKind.SINK_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public KodexReceiverImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public void processKRec13Anon(KRec13Anon data) {
        System.out.println("RECEIVED " + data.get_kip() + " " + data.getStringField() + " " + data.getIntField());
    }

}
