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
import java.util.Random;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.PythonSyncTestInput;
import iip.datatypes.PythonSyncTestInputImpl;
import iip.impl.SimplePythonSyncDataSourceImpl;

/**
 * A simple test source ingesting data according to a timer schema. Analogously, a connector can be linked to a
 * data ingestor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimplePythonSyncSourceImpl extends SimplePythonSyncDataSourceImpl {

    private Random random = new Random();
    private int counter = 0;
    
    /**
     * Fallback constructor.
     */
    public SimplePythonSyncSourceImpl() {
        super(ServiceKind.SOURCE_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimplePythonSyncSourceImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public PythonSyncTestInput producePythonSyncTestInput() {
        PythonSyncTestInput rec = new PythonSyncTestInputImpl();
        rec.setId(counter++);
        rec.setValue1(random.nextDouble());
        rec.setValue2(random.nextDouble());
        return rec;
    }

}
