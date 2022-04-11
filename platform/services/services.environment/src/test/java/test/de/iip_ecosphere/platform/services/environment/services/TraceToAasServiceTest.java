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
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.EnvironmentSetup;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.services.ApplicationSetup;
import de.iip_ecosphere.platform.services.environment.services.TraceToAasService;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AbstractAasLifecycleDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.TraceRecord;

import org.junit.Assert;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

/**
 * Tests {@link TraceToAasService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceToAasServiceTest {

    private static Server qpid;
    private static MyLifecycleDescriptor aasDesc;
    private static Server registryServer;
    private static Server aasServer;

    /**
     * An internal lifecycle descriptor (mocking the platform).
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyLifecycleDescriptor extends AbstractAasLifecycleDescriptor {

        /**
         * Creates a descriptor instance.
         * 
         * @param setup the AAS setup
         */
        protected MyLifecycleDescriptor(AasSetup setup) {
            super("MyAas", () -> setup);
        }
        
    }
    
    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() {
        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        qpid = new TestQpidServer(broker);
        qpid.start();
        
        // adjust the setup 
        AasSetup aasSetup = AasSetup.createLocalEphemeralSetup(null, false);
        System.out.println(aasSetup.getRegistryEndpoint().toServerUri() + "/registry/api/v1/registry");
        
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = aasSetup.getRegistryEndpoint();
        PersistenceType pType = LocalPersistenceType.INMEMORY;
        LoggerFactory.getLogger(TraceToAasServiceTest.class).info(
            "Starting " + pType + " AAS registry on " + regEndpoint.toUri());
        registryServer = rcp.createRegistryServer(regEndpoint, pType);
        registryServer.start();
        Endpoint serverEndpoint = aasSetup.getServerEndpoint();
        LoggerFactory.getLogger(TraceToAasServiceTest.class).info(
            "Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        aasServer = rcp.createAasServer(aasSetup.getServerEndpoint(), pType, regEndpoint);
        aasServer.start();
        
        // just like the real platform, but with private local descriptor, no full lifecycle
        aasDesc = new MyLifecycleDescriptor(aasSetup);
        aasDesc.startup(new String[] {});
        
        // mock the setup
        EnvironmentSetup setup = Starter.getSetup();
        setup.setAasSetup(aasSetup);
        TransportSetup transSetup = new TransportSetup();
        transSetup.setHost("localhost");
        transSetup.setPort(broker.getPort());
        transSetup.setUser("user"); // preliminary in here
        transSetup.setPassword("pwd");
        setup.setTransport(transSetup);
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        aasDesc.shutdown();
        registryServer.stop(true);
        aasServer.stop(true);        
        qpid.stop(true);
    }
    
    /**
     * A simple data class.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyData {
        
        private long timestamp;
        private int[] image; // TODO probably byte, requires model update

        /**
         * For serialization.
         */
        private MyData() {
        }
        
        /**
         * Creates an instance.
         * 
         * @param image the "image" data
         */
        private MyData(int[] image) {
            this.timestamp = System.currentTimeMillis();
            this.image = image;
        }
        
        /**
         * Returns the timestamp. (retrieved, called by service)
         * 
         * @return the timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Returns the image data. (retrieved, called by service)
         * 
         * @return the image data
         */
        public int[] getImage() {
            return image;
        }
        
        // setters are usually there, shall also work without here
        
    }
    
    /**
     * Tests the service.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testService() throws ExecutionException, IOException {
        ApplicationSetup app = new ApplicationSetup();
        app.setName("myApp");
        app.setId("app-0");
        
        YamlService sDesc = new YamlService();
        sDesc.setName("TraceAasTest");
        sDesc.setVersion(new Version(TraceToAasService.VERSION));
        sDesc.setKind(ServiceKind.SINK_SERVICE);
        sDesc.setId("TraceAasTest");
        sDesc.setDeployable(true);
        
        TraceToAasService service = new TraceToAasService(app, sDesc);
        service.setState(ServiceState.STARTING);
        Assert.assertEquals(ServiceState.RUNNING, service.getState());
        // send messages
        int[] img = new int[] {128, 128, 64, 12, 0, 8};
        MyData data = new MyData(img);
        Transport.sendTraceRecord(new TraceRecord("source", "send", data));
        TimeUtils.sleep(700);
        Transport.sendTraceRecord(new TraceRecord("receiver", "received", data));
        TimeUtils.sleep(700); // AAS needs some time, notification in parallel

        Aas aas = AasPartRegistry.retrieveAas(Starter.getSetup().getAas(), service.getAasUrn());
        Assert.assertNotNull(aas);
        Submodel submodel = aas.getSubmodel(TraceToAasService.SUBMODEL_NAMEPLATE);
        Assert.assertNotNull(submodel);
        Property prop = submodel.getProperty(TraceToAasService.PROPERTY_APP_ID);
        Assert.assertNotNull(prop);
        Assert.assertEquals(app.getId(), prop.getValue());
        prop = submodel.getProperty(TraceToAasService.PROPERTY_APP_NAME);
        Assert.assertNotNull(prop);
        Assert.assertEquals(app.getName(), prop.getValue());

        submodel = aas.getSubmodel(TraceToAasService.SUBMODEL_TRACES);
        // initial comparison/testing
        Map<String, SubmodelElementCollection> elts = new HashMap<String, SubmodelElementCollection>();
        for (SubmodelElement e : submodel.submodelElements()) {
            if (e instanceof SubmodelElementCollection) {
                SubmodelElementCollection coll = (SubmodelElementCollection) e;
                prop = coll.getProperty(TraceToAasService.PROPERTY_SOURCE);
                Assert.assertNotNull(prop);        
                elts.put(prop.getValue().toString(), coll);

                SubmodelElementCollection pColl = coll.getSubmodelElementCollection(TraceToAasService.PROPERTY_PAYLOAD);
                Assert.assertNotNull(pColl);
                Assert.assertTrue(pColl.getElementsCount() > 0); // something is in -> MyData
                elts.put(prop.getValue().toString(), coll);
            }
        }
        Assert.assertNotNull(elts.get("source"));
        Assert.assertNotNull(elts.get("receiver"));

        service.setState(ServiceState.STOPPING);
    }

}
