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
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.aas.AasConnector;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import test.de.iip_ecosphere.platform.connectors.AbstractInformationModelConnectorTest;
import test.de.iip_ecosphere.platform.connectors.MachineCommand;
import test.de.iip_ecosphere.platform.connectors.MachineData;
import test.de.iip_ecosphere.platform.support.aas.AasTest;
import test.de.iip_ecosphere.platform.support.aas.TestMachine;

/**
 * Tests {@link AasConnector} with polling and no security.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasConnectorTest extends AbstractInformationModelConnectorTest<Object> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AasConnectorTest.class);
    private static final String AAS_URN = "urn:::AAS:::testMachines#";
    private static final ServerAddress VAB_SERVER = new ServerAddress(Schema.HTTP); // localhost, ephemeral
    private static KeyStoreDescriptor keyDesc;
    
    private static Server platformAasServer;
    private static Server httpServer;
    private static Server ccServer;
    private static Server repository;
    private static NotificationMode oldNotificationMode;
    private static AasSetup oldSetup;
    private static ServerAddress aasServer;
    
    /**
     * Creates an instance of this test.
     */
    public AasConnectorTest() {
        super(Object.class);
    }
    
    /**
     * Changes the keystore descriptor, e.g., to go for HTTPS/TLS.
     * 
     * @param desc the keystore descriptor to use
     */
    protected static void setKeystoreDescriptor(KeyStoreDescriptor desc) {
        keyDesc = desc;
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
        oldSetup = AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        
        // we don't start all active AAS here as we do not need them for this test
        AasBuildResult bResult = AasPartRegistry.build(d -> d.getKind() != Kind.ACTIVE);
        Server implServer = bResult.getProtocolServerBuilder().build();
        platformAasServer = AasPartRegistry.deploy(bResult.getAas()).start();
        LOGGER.info("Platform AAS server started");
        
        TestMachine machine = new TestMachine();
        // start required here by basyx-0.1.0-SNAPSHOT
        ccServer = AasTest.createOperationsServer(VAB_SERVER.getPort(), machine, 
            AasFactory.DEFAULT_PROTOCOL, null).start(); 
        Aas aas = createAAS();
        if (null != keyDesc) {
            aasServer = new ServerAddress(Schema.HTTPS); // localhost, ephemeral
        } else {
            aasServer = new ServerAddress(Schema.HTTP); // localhost, ephemeral
        }
        
        Endpoint registryEndpoint = getRegistryEndpoint();
        DeploymentRecipe dBuilder = AasFactory.getInstance()
            .createDeploymentRecipe(new Endpoint(aasServer, ""), keyDesc);
        httpServer = dBuilder
            .addInMemoryRegistry(registryEndpoint)
            .deploy(aas)
            .createServer();
        httpServer.start();
        implServer.start();

        LOGGER.info("AAS server started");
    }
    
    /**
     * Returns the registry endpoint for {@link #aasServer}.
     * 
     * @return the endpoint
     */
    private static Endpoint getRegistryEndpoint() {
        return new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);
    }
        
    /**
     * Shuts down the test server.
     */
    @AfterClass
    public static void shutdown() {
        if (null != repository) {
            repository.stop(true);
        }
        httpServer.stop(true);
        ccServer.stop(true);
        LOGGER.info("Platform/AAS server stopped");
        platformAasServer.stop(true);

        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldNotificationMode);
    }
    
    /**
     * This method creates and starts the Asset Administration Shell.
     * 
     * @return the created AAS instance
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    public static Aas createAAS() throws SocketException, UnknownHostException {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(AasTest.NAME_AAS, AAS_URN);
        SubmodelBuilder subModelBuilder = aasBuilder.createSubmodelBuilder(AasTest.NAME_SUBMODEL, null);
        AasTest test = new AasTest(); // add KeyStoreDescriptor here
        test.createAasOperationsElements(subModelBuilder, VAB_SERVER, AasFactory.DEFAULT_PROTOCOL);
        
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
        Endpoint registryEndpoint = getRegistryEndpoint();
        return ConnectorParameterBuilder
            .newBuilder(registryEndpoint)
            .setApplicationInformation(AAS_URN, "")
            .setEndpointPath(aasServer.getSchema() + ":" + registryEndpoint.getEndpoint())
            // keysettings ingored, registry alyways HTTP
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
