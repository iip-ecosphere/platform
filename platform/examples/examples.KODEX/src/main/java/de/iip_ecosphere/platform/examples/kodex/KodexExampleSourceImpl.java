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
import java.util.Random;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.KRec13;
import iip.datatypes.KRec13Impl;
import iip.impl.SimpleKodexDataSourceImpl;

/**
 * A simple test source ingesting data according to a timer schema. Analogously, a connector can be linked to a
 * data ingestor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class KodexExampleSourceImpl extends SimpleKodexDataSourceImpl {

    private Random random = new Random();
    
    /**
     * Fallback constructor.
     */
    public KodexExampleSourceImpl() {
        super(ServiceKind.SOURCE_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public KodexExampleSourceImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    /**
    * Creates data to be ingested.
    *
    * @return the created data, <b>null</b> for no data
    */
    @Override
    public KRec13 produceKRec13() {
        KRec13 rec = new KRec13Impl();
        rec.setIntField(random.nextInt());
        rec.setStringField("SYNC");
        return rec;
    }

}
