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

package test.de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.environment.services.TraceToAasService;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.status.TraceRecord;

import org.junit.Assert;

/**
 * Tests {@link TraceToAasService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceToAasServiceTest {

    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() {
        TraceToAasServiceMain.startup(null, -1, -1, -1);
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        TraceToAasServiceMain.shutdown();
    }

    /**
     * Tests the service.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testService() throws ExecutionException, IOException {
        TraceToAasService service = TraceToAasServiceMain.createService();
        final int cleanupTimeout = 2000;
        // allow for quick cleanup
        service.setTimeout(cleanupTimeout);
        service.setCleanupTimeout(cleanupTimeout);
        
        ApplicationSetup app = service.getApplicationSetup();
        service.setState(ServiceState.STARTING);
        Assert.assertEquals(ServiceState.RUNNING, service.getState());
        TimeUtils.waitFor(() -> !service.isAasStarted(), 5000, 300);
        
        System.out.println("Sending...");
        // send messages
        int[] img = new int[] {128, 128, 64, 12, 0, 8};
        TraceToAasServiceMain.MyData data = new TraceToAasServiceMain.MyData(img);
        Transport.sendTraceRecord(new TraceRecord("source", "send", data));
        TimeUtils.sleep(700);
        Transport.sendTraceRecord(new TraceRecord("receiver", "received", data));
        TimeUtils.sleep(700); // AAS needs some time, notification in parallel

        Aas aas = AasPartRegistry.retrieveAas(Starter.getSetup().getAas(), service.getAasUrn());
        Assert.assertNotNull(aas);
        Submodel submodel = aas.getSubmodel(PlatformAas.SUBMODEL_NAMEPLATE);
        Assert.assertNotNull(submodel);
        Property prop = submodel.getProperty(PlatformAas.NAME_PROPERTY_ID);
        Assert.assertNotNull(prop);
        Assert.assertEquals(app.getId(), prop.getValue());
        prop = submodel.getProperty(PlatformAas.NAME_PROPERTY_NAME);
        Assert.assertNotNull(prop);
        Assert.assertEquals(app.getName(), prop.getValue());

        SubmodelElementCollectionBuilder epBuilder = submodel.createSubmodelElementCollectionBuilder(
            "testEpContainer", false, false); // submodel does not matter
        TransportConverter.addEndpointToAas(epBuilder, null);
        TransportConverter.addEndpointToAas(epBuilder, new Endpoint(Schema.HTTP, Endpoint.LOCALHOST, 1234, ""));
        TransportConverter.addEndpointToAas(epBuilder, new Endpoint(Schema.WS, Endpoint.LOCALHOST, 1235, "/myPath"));
        epBuilder.build();
        
        submodel = aas.getSubmodel(TraceToAasService.SUBMODEL_TRACES);

        // initial comparison/testing
        if (service.isTraceInAas()) {
            Map<String, SubmodelElementCollection> elts = new HashMap<String, SubmodelElementCollection>();
            for (SubmodelElement e : submodel.submodelElements()) {
                if (e instanceof SubmodelElementCollection) {
                    SubmodelElementCollection coll = (SubmodelElementCollection) e;
                    prop = coll.getProperty(TraceToAasService.PROPERTY_SOURCE);
                    Assert.assertNotNull(prop);        
                    elts.put(prop.getValue().toString(), coll);
    
                    SubmodelElementCollection pColl = coll.getSubmodelElementCollection(
                        TraceToAasService.PROPERTY_PAYLOAD);
                    Assert.assertNotNull(pColl);
                    Assert.assertTrue(pColl.getElementsCount() > 0); // something is in -> MyData
                    elts.put(prop.getValue().toString(), coll);
                }
            }
            Assert.assertNotNull(elts.get("source"));
            Assert.assertNotNull(elts.get("receiver"));
        }
        
        System.out.println("Waiting for cleanup... (max " + cleanupTimeout + " ms)");
        TimeUtils.sleep(cleanupTimeout);
        service.cleanup(); // trigger cleanup, no new elements
        
        aas = AasPartRegistry.retrieveAas(Starter.getSetup().getAas(), service.getAasUrn());
        Assert.assertNotNull(aas);
        submodel = aas.getSubmodel(TraceToAasService.SUBMODEL_TRACES);
        int remainingCount = 0;
        for (SubmodelElement e : submodel.submodelElements()) {
            if (e instanceof SubmodelElementCollection) {
                remainingCount++;
            }
        }
        if (service.isTraceInAas()) {
            Assert.assertEquals(0, remainingCount); // shall be gone
        }
        service.setState(ServiceState.STOPPING);
    }

}
