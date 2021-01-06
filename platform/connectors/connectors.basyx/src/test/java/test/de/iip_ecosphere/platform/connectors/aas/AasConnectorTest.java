/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.aas;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.processing.Generated;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorRegistry;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.aas.AasConnector;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.connectors.ConnectorTest;
import test.de.iip_ecosphere.platform.connectors.MachineCommand;
import test.de.iip_ecosphere.platform.connectors.MachineData;
import test.de.iip_ecosphere.platform.support.aas.AasTest;
import test.de.iip_ecosphere.platform.support.aas.TestMachine;
import test.de.iip_ecosphere.platform.support.aas.basyx.BaSyxTest;

/**
 * Tests {@link AasConnector} with polling and no security.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasConnectorTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AasConnectorTest.class);
    private static final String AAS_IP = "localhost";
    private static final int AAS_PORT = NetUtils.getEphemeralPort();
    private static final int VAB_PORT = NetUtils.getEphemeralPort();
    private static final String AAS_URN = "urn:::AAS:::testMachines#";
    private static final String REGISTRY_PATH = "registry";
    
    private static Server platformAasServer;
    private static Server httpServer;
    private static Server ccServer;

    /**
     * Sets the test up by starting an embedded OPC UA server.
     * 
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    @BeforeClass
    public static void init() throws SocketException, UnknownHostException {
        // multiple test runs may load the same descriptor multiple times
        ConnectorRegistry.getRegisteredConnectorDescriptorsLoader().reload();
        
        AasPartRegistry.setAasEndpoint(new Endpoint(Schema.HTTP, AasPartRegistry.DEFAULT_HOST, 
            NetUtils.getEphemeralPort(), AasPartRegistry.DEFAULT_ENDPOINT));
        platformAasServer = AasPartRegistry.deploy(AasPartRegistry.build()).start();
        LOGGER.info("Platform AAS server started");
        
        TestMachine machine = new TestMachine();
        // start required here by basyx-0.1.0-SNAPSHOT
        ccServer = AasTest.createOperationsServer(VAB_PORT, machine).start(); 
        Aas aas = createAAS(machine);

        DeploymentRecipe dBuilder = AasFactory.getInstance()
            .createDeploymentRecipe(new Endpoint(Schema.HTTP, AAS_IP, AAS_PORT, ""));
        httpServer = dBuilder
            .addInMemoryRegistry(REGISTRY_PATH)
            .deploy(aas)
            .createServer();
        
        httpServer.start();

        LOGGER.info("AAS server started");
    }
    
    /**
     * Shuts down the test server.
     */
    @AfterClass
    public static void shutdown() {
        httpServer.stop(true);
        ccServer.stop(true);
        LOGGER.info("Platform/AAS server stopped");
        platformAasServer.stop(true);

        AasPartRegistry.setAasEndpoint(AasPartRegistry.DEFAULT_EP);
    }
    
    /**
     * This method creates and starts the Asset Administration Shell.
     * 
     * @param machine the test machine instance
     * @return the created AAS instance
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    public static Aas createAAS(TestMachine machine) throws SocketException, UnknownHostException {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(AasTest.NAME_AAS, AAS_URN);
        SubmodelBuilder subModelBuilder = aasBuilder.createSubmodelBuilder(AasTest.NAME_SUBMODEL, null);
        BaSyxTest.createAasOperationsElements(subModelBuilder, AAS_IP, VAB_PORT);
        
        subModelBuilder.build();
        return aasBuilder.build();
    }
    
    /**
     * Blocks until a certain number of (accumulated) receptions is reached or fails after 4s.
     * 
     * @param count the counter
     * @param receptions the expected number of receptions 
     */
    private void block(AtomicInteger count, int receptions) {
        int max = 20; // longer than polling interval in params, 30 may be required depending on machine speed
        while (count.get() < receptions && max > 0) {
            TimeUtils.sleep(200);
            max--;
        }
        Assert.assertTrue("Operation took too long", max > 0);
    }
        
    @Generated("Specified in IVML, generated by EASy")
    private static class OutputTranslator extends AbstractConnectorOutputTypeTranslator<Object, MachineData> {
        
        private boolean withNotifications;
        
        /**
         * Creates instance.
         * 
         * @param withNotifications operate with/without notifications (for testing)
         */
        private OutputTranslator(boolean withNotifications) {
            this.withNotifications = withNotifications;
        }

        @Override
        public MachineData to(Object source) throws IOException {
            ModelAccess access = getModelAccess();
            try {
                access.get("abxy"); // no submodel
                Assert.fail("Property shall not exist");
            } catch (IOException e) {
                // expected
            }
            try {
                access.get("ab" + access.getQSeparator() + "abxy"); // submodel/property does not exist
                Assert.fail("Property shall not exist");
            } catch (IOException e) {
                // expected
            }
            try {
                access.get(AasTest.NAME_SUBMODEL + access.getQSeparator() + "abxy"); // property does not exist
                Assert.fail("Property shall not exist");
            } catch (IOException e) {
                // expected
            }
            return new MachineData(
                (int) access.get(AasTest.QNAME_VAR_LOTSIZE), 
                (double) access.get(AasTest.QNAME_VAR_POWCONSUMPTION), 
                (String) access.get(AasTest.QNAME_VAR_VENDOR));
        }

        @Override
        public void initializeModelAccess() throws IOException {
            ModelAccess access = getModelAccess();
            access.useNotifications(withNotifications);
            //if (withNotifications) { // for testing
                // access.setDetailNotifiedItem(true); may be set here, then source in "to" above will receive values 
                //access.monitor("");
            //}
        }

        @Override
        public Class<? extends Object> getSourceType() {
            return Object.class;
        }

        @Override
        public Class<? extends MachineData> getTargetType() {
            return MachineData.class;
        }

    }

    @Generated("Specified in IVML, generated by EASy")
    private static class InputTranslator extends AbstractConnectorInputTypeTranslator<MachineCommand, Object> {

        @Override
        public Object from(MachineCommand data) throws IOException {
            ModelAccess access = getModelAccess();
            // generated code with "semantic" from configuration model
            if (data.isStart()) {
                access.call(AasTest.QNAME_OP_STARTMACHINE);
            }
            if (data.isStop()) {
                access.call(AasTest.QNAME_OP_STOPMACHINE);
            }
            if (data.getLotSize() > 0) {
                // as the connector accepts Objects, also data.lotSize would be ok
                access.set(AasTest.QNAME_VAR_LOTSIZE, data.getLotSize());
            }
            try {
                access.set(AasTest.NAME_SUBMODEL + access.getQSeparator() + "abxy", ""); // property does not exist
                Assert.fail("Property shall not exist");
            } catch (IOException e) {
                // expected
            }
            try {
                access.call(AasTest.NAME_SUBMODEL + access.getQSeparator() + "abxy"); // operation does not exist
                Assert.fail("Operation shall not exist");
            } catch (IOException e) {
                // expected
            }
            try {
                access.call("abc" + access.getQSeparator() + "abxy"); // submodel/operation do not exist
                Assert.fail("Operation shall not exist");
            } catch (IOException e) {
                // expected
            }
            try {
                access.call("abxy"); // no submodel
                Assert.fail("Operation shall not exist");
            } catch (IOException e) {
                // expected
            }
            return null; // irrelevant
        }

        @Override
        public Class<? extends Object> getSourceType() {
            return Object.class;
        }

        @Override
        public Class<? extends MachineCommand> getTargetType() {
            return MachineCommand.class;
        }
        
    }

    /**
     * Tests the connector in polling mode.
     * 
     * @throws IOException in case that creating the connector fails
     */
    @Test
    public void testWithPolling() throws IOException {
        ConnectorTest.assertDescriptorRegistration(AasConnector.Descriptor.class);
        ConnectorParameter params = ConnectorParameterBuilder
            .newBuilder(AAS_IP, AAS_PORT)
            .setApplicationInformation(AAS_URN, "")
            .setEndpointPath(REGISTRY_PATH)
            .build();
        
        AtomicReference<MachineData> md = new AtomicReference<MachineData>();
        AtomicInteger count = new AtomicInteger(0);
        
        AasConnector<MachineData, MachineCommand> connector = new AasConnector<>(
            new TranslatingProtocolAdapter<Object, Object, MachineData, MachineCommand>(
                 new OutputTranslator(false), new InputTranslator()));
        ConnectorTest.assertInstance(connector, false);
        ConnectorTest.assertConnectorProperties(connector);
        connector.setReceptionCallback(new ReceptionCallback<MachineData>() {
            
            @Override
            public void received(MachineData data) {
                md.set(data);
                count.incrementAndGet();
            }
            
            @Override
            public Class<MachineData> getType() {
                return MachineData.class;
            }
        });
        connector.connect(params);
        ConnectorTest.assertInstance(connector, true);
        LOGGER.info("AAS connector started");

        block(count, 2); // init changes powConsumption and lotSize

        MachineData tmp = md.get();
        Assert.assertNotNull("We shall have received some data although the machine is not running", tmp);
        Assert.assertEquals(1, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() < 1);
        
        // try starting the machine
        MachineCommand cmd = new MachineCommand();
        cmd.setStart(true);
        connector.write(cmd);
        
        block(count, 3); // cmd changes powConsuption
        
        tmp = md.get();
        Assert.assertEquals(1, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() > 5);
        
        cmd = new MachineCommand();
        cmd.setLotSize(5);
        connector.write(cmd);

        block(count, 4); // cmd changes lotSize

        tmp = md.get();
        Assert.assertEquals(5, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() > 5);

        cmd = new MachineCommand();
        cmd.setStop(true);
        connector.write(cmd);

        block(count, 6); // cmd changes powConsuption and lot size

        tmp = md.get();
        Assert.assertEquals(1, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() < 1);

        ConnectorTest.assertInstance(connector, true);
        connector.disconnect();
        ConnectorTest.assertInstance(connector, false);
        LOGGER.info("AAS connector disconnected");
        connector.dispose();
    }
    
}
