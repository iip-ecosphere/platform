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

package de.iip_ecosphere.platform.examples.rtsa;

import java.io.InputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.RtsaTestInput;
import iip.datatypes.RtsaTestInputImpl;
import iip.impl.SimpleRtsaDataSourceImpl;

/**
 * A simple test source ingesting data according to a timer schema. Analogously, a connector can be linked to a
 * data ingestor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimpleRtsaSourceImpl extends SimpleRtsaDataSourceImpl {

    private Timer timer = new Timer();
    private Random random = new Random();
    private int counter = 0;
    
    /**
     * Fallback constructor.
     */
    public SimpleRtsaSourceImpl() {
        super(ServiceKind.SOURCE_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimpleRtsaSourceImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    // we also could do this as synchronous case with a polling time in the model
    @Override
    public void attachRtsaTestInputIngestor(DataIngestor<RtsaTestInput> ingestor) {
        if (null != ingestor) {
            timer.schedule(new TimerTask() {
                
                @Override
                public void run() {
                    RtsaTestInput rec = new RtsaTestInputImpl();
                    rec.setId(counter++);
                    rec.setValue1(random.nextDouble());
                    rec.setValue2(random.nextDouble());
                    ingestor.ingest(rec);
                }
            }, 0, 1000);
        }
    }

}