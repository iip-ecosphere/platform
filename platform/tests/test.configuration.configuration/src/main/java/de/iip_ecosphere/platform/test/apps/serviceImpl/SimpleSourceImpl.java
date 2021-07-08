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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.DefaultServiceImpl;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.Rec1;
import iip.interfaces.SimpleDataSourceService;

/**
 * A simple test source ingesting data according to a timer schema. Analogously, a connector can be linked to a
 * data ingestor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimpleSourceImpl extends DefaultServiceImpl implements SimpleDataSourceService {

    private Timer timer = new Timer();
    private Random random = new Random();
    
    /**
     * Fallback constructor.
     */
    public SimpleSourceImpl() {
        super(ServiceKind.SOURCE_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimpleSourceImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public Rec1 createRec1() {
        return null;
    }

    @Override
    public void attachcreateRec1_SimpleSourceIngestor(final DataIngestor<Rec1> ingestor) {
        if (null != ingestor) {
            timer.schedule(new TimerTask() {
                
                @Override
                public void run() {
                    Rec1 rec = new Rec1();
                    rec.setIntField(random.nextInt());
                    rec.setStringField("STRING " + random.nextFloat());
                    ingestor.ingest(new Rec1());
                }
            }, 0, 1000);
        }
    }

}
