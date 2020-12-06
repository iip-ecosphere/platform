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

package test.de.iip_ecosphere.platform.connectors.basyx;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.annotation.processing.Generated;
import javax.servlet.http.HttpServlet;

import org.apache.catalina.startup.Tomcat;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistryService;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.aas.registration.restapi.DirectoryModelProvider;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.VABMultiSubmodelProvider;
import org.eclipse.basyx.models.controlcomponent.ControlComponent;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetypedef.PropertyValueTypeDef;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.OperationVariable;
import org.eclipse.basyx.submodel.restapi.SubModelProvider;
import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaProviderHelper;
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.eclipse.basyx.vab.protocol.http.server.AASHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.basyx.BaSyxAasConnector;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.transport.Utils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.connectors.ConnectorTest;

/**
 * Tests {@link BaSyxAasConnector} with polling and no security.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAasConnectorTest {

    public static final String QNAME_VAR_LOTSIZE;
    public static final String QNAME_VAR_POWCONSUMPTION;
    public static final String QNAME_OP_STARTMACHINE;
    public static final String QNAME_OP_RECONFIGURE;
    public static final String QNAME_OP_STOPMACHINE;

    private static final String NAME_AAS = "aasTest";
    private static final String NAME_SUBMODEL = "machine";
    private static final String NAME_VAR_LOTSIZE = "lotSize";
    private static final String NAME_VAR_POWCONSUMPTION = "powerConsumption";
    private static final String NAME_OP_STARTMACHINE = "startMachine";
    private static final String NAME_OP_RECONFIGURE = "setLotSize";
    private static final String NAME_OP_STOPMACHINE = "stopMachine";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BaSyxAasConnectorTest.class);
    private static final String AAS_IP = "localhost";
    private static final int AAS_PORT = 4000;
    private static final int VAB_PORT = 4001;
    private static final String AAS_URN = "urn:::AAS:::testMachines#";
    private static final String REGISTRY_PATH = "registry";
    
    private static AASHTTPServer httpServer;
    
    static {
        QNAME_VAR_LOTSIZE = NAME_SUBMODEL + "/" + NAME_VAR_LOTSIZE;
        QNAME_VAR_POWCONSUMPTION = NAME_SUBMODEL + "/" + NAME_VAR_POWCONSUMPTION;
        QNAME_OP_STARTMACHINE = NAME_SUBMODEL + "/" + NAME_OP_STARTMACHINE;
        QNAME_OP_RECONFIGURE = NAME_SUBMODEL + "/" + NAME_OP_RECONFIGURE;
        QNAME_OP_STOPMACHINE = NAME_SUBMODEL + "/" + NAME_OP_STOPMACHINE;
    }

    /**
     * Sets the test up by starting an embedded OPC UA server.
     * 
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    @BeforeClass
    public static void init() throws SocketException, UnknownHostException {
        TestMachine machine = new TestMachine();
        startControlComponent(machine);
        startAAS(machine);
        LOGGER.info("AAS server started");
    }
    
    /**
     * Shuts down the test server.
     */
    @AfterClass
    public static void shutdown() {
        httpServer.shutdown();
        LOGGER.info("AAS server stopped");
    }
    
    /** 
     * This method creates a control component for the {@link TestMachine}.
     * 
     * @param machine the machine instance
     */
    private static void startControlComponent(TestMachine machine) {
        ControlComponent cc = new TestControlComponent(machine);
        // Server where the control component is reachable.
        VABMapProvider ccProvider = new VABMapProvider(cc);
        BaSyxTCPServer<VABMapProvider> server = new BaSyxTCPServer<>(ccProvider, VAB_PORT);
        server.start();
    }
    
    /**
     * This method creates and starts the Asset Administration Shell.
     * 
     * @param machine the test machine instance
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    public static void startAAS(TestMachine machine) throws SocketException, UnknownHostException {
        SubModel machineSubModel = new SubModel();
        
        Property lotSizeProperty = new Property();
        lotSizeProperty.setIdShort(NAME_VAR_LOTSIZE);
        lotSizeProperty.set(VABLambdaProviderHelper.createSimple(() -> {
            return machine.getLotSize(); 
        }, null), PropertyValueTypeDef.Integer);
        
        Property powConsumptionProperty = new Property();
        powConsumptionProperty.setIdShort(NAME_VAR_POWCONSUMPTION);
        powConsumptionProperty.set(VABLambdaProviderHelper.createSimple(() -> {
            return machine.getPowerConsumption();  
        }, null), PropertyValueTypeDef.Double);
            
        machineSubModel.addSubModelElement(lotSizeProperty);
        machineSubModel.addSubModelElement(powConsumptionProperty);
        
        // Creating the Operations
        Operation startMachineOperation = new Operation();
        startMachineOperation.setIdShort(NAME_OP_STARTMACHINE);
        startMachineOperation.setInvocable(createInvokable(TestControlComponent.OPMODE_STARTING));
        machineSubModel.addSubModelElement(startMachineOperation);

        Operation configureMachineOperation = new Operation();
        configureMachineOperation.setIdShort(NAME_OP_RECONFIGURE);
        configureMachineOperation.setInvocable(createInvokable(TestControlComponent.OPMODE_CONFIGURING));
        List<OperationVariable> configureMachineParams = new ArrayList<>();
        OperationVariable paramLotSize = new OperationVariable();
        // strange, cannot set name or type
        configureMachineParams.add(paramLotSize);
        configureMachineOperation.setInputVariables(configureMachineParams);
        machineSubModel.addSubModelElement(configureMachineOperation);

        Operation stopMachineOperation = new Operation();
        stopMachineOperation.setIdShort(NAME_OP_STOPMACHINE);
        stopMachineOperation.setInvocable(createInvokable(TestControlComponent.OPMODE_STOPPING, () -> {
            lotSizeProperty.set(VABLambdaProviderHelper.createSimple(() -> { // force reset 
                return machine.getLotSize(); 
            }, null), PropertyValueTypeDef.Integer);
        }));
        machineSubModel.addSubModelElement(stopMachineOperation);
        
        // Setting identifiers. 
        machineSubModel.setIdShort(NAME_SUBMODEL);
        machineSubModel.setIdentification(IdentifierType.CUSTOM, NAME_SUBMODEL);

        // AAS
        AssetAdministrationShell aas = new AssetAdministrationShell();
        ModelUrn aasURN = new ModelUrn(AAS_URN);
        aas.setIdentification(aasURN);
        aas.setIdShort(NAME_AAS);

        //Wrapping Submodels in IModelProvider
        AASModelProvider aasProvider = new AASModelProvider(aas);
        SubModelProvider machineSMProvider = new SubModelProvider(machineSubModel);
        VABMultiSubmodelProvider fullProvider = new VABMultiSubmodelProvider();
        fullProvider.setAssetAdministrationShell(aasProvider);
        fullProvider.addSubmodel(NAME_SUBMODEL, machineSMProvider);
        
        HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);
        IAASRegistryService registry = new InMemoryRegistry();
        IModelProvider registryProvider = new DirectoryModelProvider(registry);
        HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);
        
        // Register descriptors.
        AASDescriptor aasDescriptor = new AASDescriptor(aas, "http://" + AAS_IP + ":" 
            + AAS_PORT + "/" + NAME_AAS + "/aas");
        aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(machineSubModel, "http://" + AAS_IP + ":" 
            + AAS_PORT + "/" + NAME_AAS + "/aas/submodels/" + NAME_SUBMODEL + "/submodel"));       
        registry.register(aasDescriptor);
        
        // Deploy the AAS on a HTTP server
        BaSyxContext context = new BaSyxContext("", "", AAS_IP, AAS_PORT);
        context.addServletMapping("/" + NAME_AAS + "/*", aasServlet);
        context.addServletMapping("/" + REGISTRY_PATH + "/*", registryServlet);
        httpServer = startServer(new AASHTTPServer(context), 3000);
    }
    
    /**
     * Starts and tries to wait for the server to come up. Unfortunately, no support for this here, just an
     * unblocking call.
     * 
     * @param httpServer the server instance
     * @param minWaitingTime the minimum waiting time
     * @return {@code server}
     */
    private static AASHTTPServer startServer(AASHTTPServer httpServer, int minWaitingTime) {
        httpServer.start();
        boolean fallbackWaiting = true;
        try {
            Field tomcatField = AASHTTPServer.class.getField("tomcat");
            Tomcat tomcat = (Tomcat) tomcatField.get(httpServer);
            tomcat.wait();
            fallbackWaiting = false;
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InterruptedException e) {
        }
        if (fallbackWaiting) {
            Utils.sleep(3000); // the server does not tell us when it is ready
        }
        return httpServer;
    }

    /**
     * Creates an invokable without attachment.
     *  
     * @param opMode the operation mode to trigger
     * @return the invokable
     */
    private static Function<Object[], Object> createInvokable(String opMode) {
        return createInvokable(opMode, null);
    }

    /**
     * Creates an invokable for an AAS-VAB operation.
     * 
     * @param opMode the op mode to trigger the control component
     * @param attachment optional other function to be executed after operation
     * @return the invokable
     */
    private static Function<Object[], Object> createInvokable(String opMode, Runnable attachment) {
        return (params) -> {
            VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector(AAS_IP, VAB_PORT)));
            proxy.setModelPropertyValue("status/opMode", opMode);
            Object result = proxy.invokeOperation("/operations/service/start", params);
            while (!proxy.getModelPropertyValue("status/exState").equals(ExecutionState.COMPLETE.getValue())) {
                Utils.sleep(500);
            }
            if (null != attachment) {
                attachment.run();
            }
            proxy.invokeOperation("operations/service/reset");
            return result;
        };
    }
    
    /**
     * Blocks until a certain number of (accumulated) receptions is reached or fails after 4s.
     * 
     * @param count the counter
     * @param receptions the xecpected number of receptions 
     */
    private void block(AtomicInteger count, int receptions) {
        int max = 20; // longer than polling interval in params, 30 may be required depending on machine speed
        while (count.get() < receptions && max > 0) {
            Utils.sleep(200);
            max--;
        }
        Assert.assertTrue("Operation took too long", max > 0);
    }
    
    @Generated("Defined in IVML, generated by EASy")
    private static class MachineData {
        private int lotSize;
        private double powerConsumption;
        
        /**
         * Creates a machine data object.
         * 
         * @param lotSize the lot size
         * @param powerConsumption the power consumption
         */
        private MachineData(int lotSize, double powerConsumption) {
            this.lotSize = lotSize;
            this.powerConsumption = powerConsumption;
        }
    }
    
    @Generated("Defined in IVML, generated by EASy")
    private static class MachineCommand {
        
        private boolean start;
        private boolean stop;
        private int lotSize;
        
    }
    
    @Generated("Specified in IVML, generated by EASy")
    private static class OutputTranslator extends AbstractConnectorOutputTypeTranslator<Object, MachineData> {
        
        @SuppressWarnings("unused")
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
            // as the connector accepts Objects, also directly casting would be ok
            return new MachineData(
                (int) access.get(QNAME_VAR_LOTSIZE), 
                (double) access.get(QNAME_VAR_POWCONSUMPTION));
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
            // generated code with "semantic" from configuration model
            if (data.start) {
                getModelAccess().call(QNAME_OP_STARTMACHINE);
            }
            if (data.stop) {
                getModelAccess().call(QNAME_OP_STOPMACHINE);
            }
            if (data.lotSize > 0) {
                // as the connector accepts Objects, also data.lotSize would be ok
                getModelAccess().set(QNAME_VAR_LOTSIZE, data.lotSize);
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
        ConnectorTest.assertDescriptorRegistration(BaSyxAasConnector.Descriptor.class);
        ConnectorParameter params = ConnectorParameterBuilder
            .newBuilder(AAS_IP, AAS_PORT)
            .setApplicationInformation(AAS_URN, "")
            .setEndpointPath(REGISTRY_PATH)
            .build();
        
        AtomicReference<MachineData> md = new AtomicReference<MachineData>();
        AtomicInteger count = new AtomicInteger(0);
        
        BaSyxAasConnector<MachineData, MachineCommand> connector = new BaSyxAasConnector<>(
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
        Assert.assertEquals(1, tmp.lotSize);
        Assert.assertTrue(tmp.powerConsumption < 1);
        
        // try starting the machine
        MachineCommand cmd = new MachineCommand();
        cmd.start = true;
        connector.write(cmd);
        
        block(count, 3); // cmd changes powConsuption
        
        tmp = md.get();
        Assert.assertEquals(1, tmp.lotSize);
        Assert.assertTrue(tmp.powerConsumption > 5);
        
        cmd = new MachineCommand();
        cmd.lotSize = 5;
        connector.write(cmd);

        block(count, 4); // cmd changes lotSize

        tmp = md.get();
        Assert.assertEquals(5, tmp.lotSize);
        Assert.assertTrue(tmp.powerConsumption > 5);

        cmd = new MachineCommand();
        cmd.stop = true;
        connector.write(cmd);

        block(count, 6); // cmd changes powConsuption and lot size

        tmp = md.get();
        Assert.assertEquals(1, tmp.lotSize);
        Assert.assertTrue(tmp.powerConsumption < 1);

        ConnectorTest.assertInstance(connector, true);
        connector.disconnect();
        ConnectorTest.assertInstance(connector, false);
        LOGGER.info("AAS connector disconnected");
        connector.dispose();
    }
    
}
