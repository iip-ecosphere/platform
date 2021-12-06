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

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.DefaultServiceImpl;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.Rec13;
import iip.interfaces.SimpleDataTransformer3Service;

/**
 * A simple test transformer. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class  SimpleTransformer2MonikaImpl extends DefaultServiceImpl implements SimpleDataTransformer3Service {

    private DataIngestor<Rec13> ingestor;
    
    /**
     * Fallback constructor.
     */
    public SimpleTransformer2MonikaImpl() {
        super(ServiceKind.TRANSFORMATION_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimpleTransformer2MonikaImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
    * Called when data arrived that shall be processed (synchronously).
    *
    * @param data the arrived data
    * @return the transformation result, <b>null</b> for no data
    */
    public Rec13 transformRec13Rec13(Rec13 data) {
        Rec13 result = new Rec13();
        result.setIntField(data.getIntField());
        result.setStringField(data.getStringField() + " SyncT2");
        return result;
    }

    // no override here as methods are alternatives for sync/async interface

    /**
    * Called when data arrived that shall be processed (asynchronously).
    *
    * @param data the arrived data 
    */
    public void processRec13(Rec13 data) {
        if (null != ingestor) {
            Rec13 result = new Rec13();
            result.setIntField(data.getIntField());
            result.setStringField(data.getStringField() + " ASyncT2");
            ingestor.ingest(result);
        }
    }

    /**
     * Called by the platform to attach an asynchronous data ingestor for type "Rec13".
     *
     * @param ingestor the "Rec13" ingestor instance
     */
    public void attachprocessRec13_SimpleTransformerIngestor(DataIngestor<Rec13> ingestor) {
        this.ingestor = ingestor; 
    }
    
}
