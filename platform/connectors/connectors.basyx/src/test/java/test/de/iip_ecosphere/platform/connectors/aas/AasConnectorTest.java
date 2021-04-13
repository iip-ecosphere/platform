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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorRegistry;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor.Kind;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasBuildResult;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.aas.AasConnector;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import test.de.iip_ecosphere.platform.connectors.AbstractInformationModelConnectorTest;
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
public class AasConnectorTest extends AbstractInformationModelConnectorTest<Object> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AasConnectorTest.class);
    private static final String AAS_URN = "urn:::AAS:::testMachines#";
    private static final ServerAddress AAS_SERVER = new ServerAddress(Schema.HTTP); // localhost, ephemeral
    private static final ServerAddress VAB_SERVER = new ServerAddress(Schema.HTTP); // localhost, ephemeral
    private static final Endpoint REGISTRY = new Endpoint(AAS_SERVER, AasPartRegistry.DEFAULT_ENDPOINT);
    
    private static Server platformAasServer;
    private static Server httpServer;
    private static Server ccServer;
    private static NotificationMode oldNotificationMode;
    
    /**
     * Creates an instance of this test.
     */
    public AasConnectorTest() {
        super(Object.class);
    }
    
    /**
     * Sets the test up by starting an embedded OPC UA server.
     * 
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    @BeforeClass
    public static void init() throws SocketException, UnknownHostException {
        oldNotificationMode = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS); // deterministic testing
        // multiple test runs may load the same descriptor multiple times
        ConnectorRegistry.getRegisteredConnectorDescriptorsLoader().reload();
        
        AasPartRegistry.setAasEndpoint(new Endpoint(Schema.HTTP, AasPartRegistry.DEFAULT_HOST, 
            NetUtils.getEphemeralPort(), AasPartRegistry.DEFAULT_ENDPOINT));
        // we don't start all active AAS here as we do not need them for this test
        AasBuildResult bResult = AasPartRegistry.build(d -> d.getKind() != Kind.ACTIVE);
        Server implServer = bResult.getProtocolServerBuilder().build();
        platformAasServer = AasPartRegistry.deploy(bResult.getAas()).start();
        LOGGER.info("Platform AAS server started");
        
        TestMachine machine = new TestMachine();
        // start required here by basyx-0.1.0-SNAPSHOT
        ccServer = AasTest.createOperationsServer(VAB_SERVER.getPort(), machine).start(); 
        Aas aas = createAAS(machine);

        DeploymentRecipe dBuilder = AasFactory.getInstance()
            .createDeploymentRecipe(new Endpoint(AAS_SERVER, ""));
        httpServer = dBuilder
            .addInMemoryRegistry(REGISTRY.getEndpoint())
            .deploy(aas)
            .createServer();
        
        implServer.start();
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
        ActiveAasBase.setNotificationMode(oldNotificationMode);
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
        BaSyxTest.createAasOperationsElements(subModelBuilder, VAB_SERVER);
        
        subModelBuilder.build();
        return aasBuilder.build();
    }

    /**
     * Tests the connector in polling mode.
     * 
     * @throws IOException in case that creating the connector fails
     */
    @Test
    public void testWithPolling() throws IOException {
        testConnector(false);
    }

    @Override
    protected Class<? extends ConnectorDescriptor> getConnectorDescriptor() {
        return AasConnector.Descriptor.class;
    }

    @Override
    protected Connector<Object, Object, MachineData, MachineCommand> createConnector(
        ProtocolAdapter<Object, Object, MachineData, MachineCommand> adapter) {
        return new AasConnector<MachineData, MachineCommand>(adapter);
    }

    @Override
    protected ConnectorParameter getConnectorParameter() {
        return ConnectorParameterBuilder
            .newBuilder(AAS_SERVER)
            .setApplicationInformation(AAS_URN, "")
            .setEndpointPath(REGISTRY.getEndpoint())
            .build();
    }

    @Override
    public String getQNameOperationStartMachine() {
        return AasTest.QNAME_OP_STARTMACHINE;
    }

    @Override
    public String getQNameOperationStopMachine() {
        return AasTest.QNAME_OP_STOPMACHINE;
    }

    @Override
    public String getQNameVarLotSize() {
        return AasTest.QNAME_VAR_LOTSIZE;
    }

    @Override
    public String getTopLevelModelPartName() {
        return AasTest.NAME_SUBMODEL;
    }

    @Override
    public void additionalFromActions(ModelAccess access, MachineCommand data) throws IOException {
        try {
            access.setStruct(AasTest.NAME_SUBMODEL + access.getQSeparator() + "struct", null);
            Assert.fail("No exception raised");
        } catch (IOException e) {
            // expected
        }
    }
    
    @Override
    public void initializeModelAccess(ModelAccess access, boolean withNotifications) throws IOException {
        if (withNotifications) { // for testing
            try {
                access.monitor("");
                Assert.fail("No exception raised");
            } catch (IOException e) {
                // expected
            }
        }
        try {
            access.registerCustomType(Object.class);
            Assert.fail("No exception raised");
        } catch (IOException e) {
            // expected
        }
    }
    
    @Override
    public String getVendor(ModelAccess access) throws IOException {
        try {
            access.getStruct(AasTest.NAME_SUBMODEL + access.getQSeparator() + "struct", Object.class);
        } catch (IOException e) {
            // expected
        }
        return (String) access.get(AasTest.QNAME_VAR_VENDOR);
    }

    @Override
    public String getQNameVarPowerConsumption() {
        return AasTest.QNAME_VAR_POWCONSUMPTION;
    }

    @Override
    public void assertAdditionalProperties(Step step, MachineData received) {
    }

    @Override
    public void afterActions(Connector<Object, Object, MachineData, MachineCommand> connector) {
    }

}
